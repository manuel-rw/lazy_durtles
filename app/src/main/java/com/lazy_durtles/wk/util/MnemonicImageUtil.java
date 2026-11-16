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

package com.lazy_durtles.wk.util;

import androidx.core.content.ContextCompat;

import com.lazy_durtles.wk.WkApplication;
import com.lazy_durtles.wk.db.model.MnemonicImage;

import java.io.File;

import javax.annotation.Nullable;

import static com.lazy_durtles.wk.Constants.MNEMONIC_IMAGES_DIRECTORY_NAME;

/**
 * Utility class for the locally stored WaniKani mnemonic background drawings.
 *
 * <p>
 *     The drawings are SVG files served from files.wanikani.com and are
 *     copyright WaniKani / Tofugu. They are only downloaded into local storage
 *     when the user explicitly opts in, and are deleted again when the user
 *     disables the feature or deletes the data pack.
 * </p>
 */
public final class MnemonicImageUtil {
    private MnemonicImageUtil() {
        //
    }

    /**
     * Get the base directory where mnemonic image SVG files are stored.
     *
     * @return the directory, may not exist yet
     */
    public static @Nullable File getBaseDirectory() {
        @Nullable File baseDirectory = ContextCompat.getNoBackupFilesDir(WkApplication.getInstance());
        if (baseDirectory == null) {
            baseDirectory = WkApplication.getInstance().getFilesDir();
        }
        if (baseDirectory == null) {
            return null;
        }
        return new File(baseDirectory, MNEMONIC_IMAGES_DIRECTORY_NAME);
    }

    /**
     * Make sure the base directory for mnemonic images exists.
     *
     * @return the directory, or null if it could not be created
     */
    public static @Nullable File assertBaseDirectoryExists() {
        final @Nullable File baseDirectory = getBaseDirectory();
        if (baseDirectory != null && !baseDirectory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            baseDirectory.mkdirs();
        }
        return baseDirectory;
    }

    /**
     * Get the local file for a mnemonic image record.
     *
     * @param record the database record
     * @return the file, may not exist
     */
    public static @Nullable File getFile(final MnemonicImage record) {
        final @Nullable String fileName = record.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        final @Nullable File baseDirectory = getBaseDirectory();
        if (baseDirectory == null) {
            return null;
        }
        return new File(baseDirectory, fileName);
    }

    /**
     * Get the local file for a mnemonic image record, if it exists on disk.
     *
     * @param record the database record
     * @return the file if it exists, or null
     */
    public static @Nullable File getExistingFile(final MnemonicImage record) {
        final @Nullable File file = getFile(record);
        if (file != null && file.isFile() && file.length() > 0) {
            return file;
        }
        return null;
    }

    /**
     * Derive a safe local file name from a remote URL. The WaniKani CDN URLs end
     * in a 32-character alphanumeric ID, which is used as the file name.
     *
     * @param url the remote URL
     * @return the file name, or null if the URL doesn't look right
     */
    public static @Nullable String fileNameForUrl(final String url) {
        if (url == null) {
            return null;
        }
        final int slash = url.lastIndexOf('/');
        if (slash < 0 || slash == url.length() - 1) {
            return null;
        }
        final String id = url.substring(slash + 1);
        if (!id.matches("[a-z0-9]{8,64}")) {
            return null;
        }
        return id + ".svg";
    }

    /**
     * Count the mnemonic image SVG files actually present on disk.
     *
     * @return the number of downloaded files
     */
    public static int countDownloadedFiles() {
        final @Nullable File baseDirectory = getBaseDirectory();
        if (baseDirectory == null || !baseDirectory.isDirectory()) {
            return 0;
        }
        final @Nullable File[] files = baseDirectory.listFiles();
        if (files == null) {
            return 0;
        }
        int count = 0;
        for (final File file : files) {
            if (file.isFile() && file.length() > 0 && file.getName().endsWith(".svg")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Delete all downloaded mnemonic image files and the base directory.
     */
    public static void deleteAllImages() {
        final @Nullable File baseDirectory = getBaseDirectory();
        if (baseDirectory != null) {
            deleteDirectory(baseDirectory);
        }
    }

    /**
     * Recursively delete a directory.
     *
     * @param directory the directory to delete
     */
    private static void deleteDirectory(final File directory) {
        try {
            final @Nullable File[] files = directory.listFiles();
            if (files != null) {
                for (final File file : files) {
                    deleteDirectory(file);
                }
            }
            //noinspection ResultOfMethodCallIgnored
            directory.delete();
        }
        catch (final Exception e) {
            //
        }
    }
}
