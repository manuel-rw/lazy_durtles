/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lazy_durtles.wk.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazy_durtles.wk.BuildConfig;
import com.lazy_durtles.wk.Constants;
import com.lazy_durtles.wk.Identification;
import com.lazy_durtles.wk.WkApplication;
import com.lazy_durtles.wk.db.AppDatabase;
import com.lazy_durtles.wk.db.Converters;
import com.lazy_durtles.wk.db.model.MnemonicImage;
import com.lazy_durtles.wk.db.model.TaskDefinition;
import com.lazy_durtles.wk.livedata.LiveMnemonicDownloadProgress;
import com.lazy_durtles.wk.livedata.LiveMnemonicImageCount;
import com.lazy_durtles.wk.livedata.LiveMnemonicImageFileCount;
import com.lazy_durtles.wk.settings.MnemonicImageSettings;
import com.lazy_durtles.wk.util.Logger;
import com.lazy_durtles.wk.util.MnemonicImageDownloadNotifier;
import com.lazy_durtles.wk.util.MnemonicImageUtil;
import com.lazy_durtles.wk.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;

import static com.lazy_durtles.wk.Constants.MINUTE;
import static com.lazy_durtles.wk.Constants.SECOND;

/**
 * Task to download the optional WaniKani mnemonic image data pack.
 *
 * <p>
 *     The subject-to-image mapping is read from the public web endpoint
 *     https://www.wanikani.com/offline/items (no API token needed), and the SVG
 *     drawings themselves are fetched from https://files.wanikani.com/... (also
 *     public). Only radicals have mnemonic images. The artwork is copyright
 *     WaniKani / Tofugu; it is stored locally only because the user opted in.
 * </p>
 *
 * <p>
 *     The task only completes (and removes itself from the queue) when the
 *     feature is still enabled; if the user disables it mid-download, the task
 *     aborts and stays queued so it can be retried later.
 * </p>
 */
public final class DownloadMnemonicImagesTask extends ApiTask {
    private static final Logger LOGGER = Logger.get(DownloadMnemonicImagesTask.class);

    /**
     * Task priority. Low priority, after the regular API tasks and audio downloads.
     */
    public static final int PRIORITY = 200;

    /**
     * The web endpoint that lists all subjects including their mnemonic image URLs.
     */
    private static final String OFFLINE_ITEMS_URL =
            "https://www.wanikani.com/offline/items?updated_since=2000-01-01T00:00:00Z";

    /**
     * The constructor.
     *
     * @param taskDefinition the definition of this task in the database
     */
    public DownloadMnemonicImagesTask(final TaskDefinition taskDefinition) {
        super(taskDefinition);
    }

    /**
     * How long to wait before retrying after a failed attempt, so a persistent
     * network problem does not spin the task queue in a tight loop.
     */
    private static final long RETRY_DELAY_MS = 5 * MINUTE;

    @Override
    public boolean canRun() {
        return MnemonicImageSettings.getEnabled()
                && System.currentTimeMillis() >= MnemonicImageSettings.getRetryNotBefore()
                && WkApplication.getInstance().getOnlineStatus().canDownloadAudio();
    }

    @Override
    protected void runLocal() {
        final AppDatabase db = WkApplication.getDatabase();

        if (!MnemonicImageSettings.getEnabled()) {
            // Disabled while queued; keep the task so it can run again if re-enabled.
            return;
        }

        MnemonicImageDownloadNotifier.showStarted();
        LiveMnemonicDownloadProgress.reportStarted();

        // Step 1: fetch the subject list with mnemonic image URLs. The endpoint
        // returns an object of the form {"items": [...], "last_updated_at": ...}.
        final @Nullable JsonNode root = fetchJson(OFFLINE_ITEMS_URL);
        final @Nullable JsonNode items = root != null && root.isObject() ? root.get("items") : root;
        if (items == null || !items.isArray()) {
            // Network problem; keep the task queued for a retry, but not immediately.
            LOGGER.error(new IOException("Unexpected response from offline items endpoint"),
                    "Could not read mnemonic image mapping");
            MnemonicImageSettings.setRetryNotBefore(System.currentTimeMillis() + RETRY_DELAY_MS);
            MnemonicImageDownloadNotifier.cancel();
            LiveMnemonicDownloadProgress.clear();
            return;
        }

        // Step 2: store the mapping in the database.
        int numImages = 0;
        for (final JsonNode entry : items) {
            final JsonNode imageNode = entry.get("meaning_mnemonic_image");
            if (imageNode == null || !imageNode.isObject()) {
                continue;
            }
            final JsonNode urlNode = imageNode.get("url");
            if (urlNode == null || !urlNode.isTextual()) {
                continue;
            }
            final String url = urlNode.asText();
            final @Nullable String fileName = MnemonicImageUtil.fileNameForUrl(url);
            if (fileName == null) {
                continue;
            }
            final long subjectId = entry.path("id").asLong(0);
            if (subjectId <= 0) {
                continue;
            }
            final MnemonicImage record = new MnemonicImage();
            record.setSubjectId(subjectId);
            record.setUrl(url);
            final JsonNode descriptionNode = imageNode.get("description");
            record.setDescription(descriptionNode != null && descriptionNode.isTextual() ? descriptionNode.asText() : null);
            record.setFileName(fileName);
            db.mnemonicImageDao().insert(record);
            numImages++;
        }
        LOGGER.info("Mnemonic image mapping stored for %d subjects", numImages);
        LiveMnemonicImageCount.getInstance().update();

        // Step 3: download the SVG files that are not on disk yet.
        final @Nullable File baseDirectory = MnemonicImageUtil.assertBaseDirectoryExists();
        if (baseDirectory == null) {
            LOGGER.error(new IOException("Could not create mnemonic image directory"), "Mnemonic image error");
            MnemonicImageDownloadNotifier.cancel();
            LiveMnemonicDownloadProgress.clear();
            return;
        }
        final java.util.List<MnemonicImage> records = db.mnemonicImageDao().getAll();
        final int total = records.size();
        int numDownloaded = 0;
        int processed = 0;
        for (final MnemonicImage record : records) {
            if (!MnemonicImageSettings.getEnabled()) {
                // Disabled mid-download; keep the task queued for a retry.
                MnemonicImageDownloadNotifier.cancel();
                LiveMnemonicDownloadProgress.clear();
                return;
            }
            processed++;
            final @Nullable String url = record.getUrl();
            final @Nullable String fileName = record.getFileName();
            if (url != null && fileName != null) {
                final File output = new File(baseDirectory, fileName);
                if (!(output.isFile() && output.length() > 0)) {
                    if (downloadSvg(url, output)) {
                        numDownloaded++;
                    }
                }
            }
            // The notifier throttles updates internally, so this is cheap to
            // call for every image.
            MnemonicImageDownloadNotifier.showProgress(processed, total);
            LiveMnemonicDownloadProgress.report(processed, total);
        }
        LOGGER.info("Downloaded %d mnemonic images", numDownloaded);

        // Done: remove this task from the queue.
        db.taskDefinitionDao().deleteTaskDefinition(taskDefinition);
        LiveMnemonicImageCount.getInstance().update();
        LiveMnemonicImageFileCount.getInstance().update();
        LiveMnemonicDownloadProgress.clear();
        MnemonicImageDownloadNotifier.showDone(MnemonicImageUtil.countDownloadedFiles());
    }

    /**
     * Fetch a JSON document from an absolute URL, without API authorization. This
     * endpoint is public and does not need the user's API token.
     *
     * @param urlString the absolute URL
     * @return the parsed JSON, or null on failure
     */
    private static @Nullable JsonNode fetchJson(final String urlString) {
        final ObjectMapper mapper = Converters.getObjectMapper();
        @Nullable HttpsURLConnection connection = null;
        try {
            final URL url = new URL(urlString);
            LOGGER.info("Fetching: %s", url);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Identification.APP_NAME_UA + "/" + BuildConfig.VERSION_NAME);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout((int) (30 * SECOND));
            connection.setReadTimeout((int) (5 * MINUTE));
            connection.getHeaderFields();
            LOGGER.info("Response code: %d %s", connection.getResponseCode(), connection.getResponseMessage());
            try (final InputStream is = connection.getInputStream()) {
                return mapper.readTree(is);
            }
        }
        catch (final Exception e) {
            LOGGER.error(e, "Fetch error for %s", urlString);
            return null;
        }
    }

    /**
     * Download one SVG file to a temp file first, then rename it into place.
     *
     * @param urlString the absolute URL
     * @param output the final output file
     * @return true if the file was downloaded successfully
     */
    private static boolean downloadSvg(final String urlString, final File output) {
        @Nullable HttpsURLConnection connection = null;
        final File tempFile = new File(output.getParentFile(), output.getName() + ".tmp");
        try {
            final URL url = new URL(urlString);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "image/svg+xml");
            connection.setRequestProperty("User-Agent", Identification.APP_NAME_UA + "/" + BuildConfig.VERSION_NAME);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout((int) (10 * SECOND));
            connection.setReadTimeout((int) MINUTE);
            connection.getHeaderFields();
            final int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                LOGGER.error(new IOException("HTTP " + code), "Download failed for %s", url);
                return false;
            }
            try (final InputStream is = connection.getInputStream();
                 final java.io.FileOutputStream os = new java.io.FileOutputStream(tempFile)) {
                final byte[] body = StreamUtil.slurp(is);
                // Sanity check: it should look like an SVG.
                final String head = new String(body, 0, Math.min(body.length, 512), StandardCharsets.UTF_8);
                if (!head.contains("<svg")) {
                    LOGGER.error(new IOException("Not an SVG"), "Downloaded file for %s does not look like an SVG", url);
                    return false;
                }
                os.write(body);
            }
            //noinspection ResultOfMethodCallIgnored
            tempFile.renameTo(output);
            return true;
        }
        catch (final Exception e) {
            LOGGER.error(e, "Download error for %s", urlString);
            //noinspection ResultOfMethodCallIgnored
            tempFile.delete();
            return false;
        }
    }
}
