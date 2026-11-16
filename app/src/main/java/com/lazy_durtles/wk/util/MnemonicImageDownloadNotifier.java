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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.lazy_durtles.wk.R;
import com.lazy_durtles.wk.WkApplication;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Shows a silent, low-priority progress notification while the optional
 * WaniKani mnemonic image data pack is being downloaded. The notification
 * never makes a sound and does not peek; it only updates the progress bar.
 */
public final class MnemonicImageDownloadNotifier {
    private static final Logger LOGGER = Logger.get(MnemonicImageDownloadNotifier.class);

    public static final String CHANNEL_ID = "MnemonicImageDownloadChannel";
    public static final int NOTIFICATION_ID = 3;

    private MnemonicImageDownloadNotifier() {
        //
    }

    private static boolean channelCreated = false;

    private static void createChannel() {
        if (channelCreated) {
            return;
        }
        channelCreated = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Mnemonic image downloads", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Progress of the optional WaniKani mnemonic image download");
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            final @Nullable NotificationManager notificationManager =
                    WkApplication.getInstance().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private static boolean canPost() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(WkApplication.getInstance(),
                        android.Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private static void post(final NotificationCompat.Builder builder) {
        if (!canPost()) {
            return;
        }
        try {
            NotificationManagerCompat.from(WkApplication.getInstance()).notify(NOTIFICATION_ID, builder.build());
        }
        catch (final SecurityException e) {
            LOGGER.error(e, "Could not post mnemonic download notification");
        }
    }

    /**
     * Post a progress update only when it would actually change what the user
     * sees: only when the integer percentage moved, and at most once per
     * [MIN_UPDATE_INTERVAL_MS] milliseconds. Re-posting an identical
     * notification makes SystemUI re-render the row, which reads as flicker.
     */
    private static long lastProgressPostMs = 0L;
    private static int lastProgressPercent = -1;
    private static final long MIN_UPDATE_INTERVAL_MS = 500L;

    private static void postProgressThrottled(final int percent, final NotificationCompat.Builder builder) {
        final long now = System.currentTimeMillis();
        synchronized (MnemonicImageDownloadNotifier.class) {
            if (percent == lastProgressPercent || now - lastProgressPostMs < MIN_UPDATE_INTERVAL_MS) {
                return;
            }
            lastProgressPercent = percent;
            lastProgressPostMs = now;
        }
        post(builder);
    }

    /**
     * Show the initial indeterminate "preparing" notification.
     */
    public static void showStarted() {
        createChannel();
        synchronized (MnemonicImageDownloadNotifier.class) {
            lastProgressPostMs = System.currentTimeMillis();
            lastProgressPercent = -1;
        }
        final NotificationCompat.Builder builder = baseBuilder()
                .setContentTitle("Downloading mnemonic images")
                .setContentText("Preparing download...")
                .setProgress(0, 0, true)
                .setOngoing(true);
        post(builder);
    }

    /**
     * Update the progress notification.
     *
     * @param done  number of images processed so far
     * @param total total number of images to process
     */
    public static void showProgress(final int done, final int total) {
        createChannel();
        final int percent = total > 0 ? (int) (100L * done / total) : 0;
        final NotificationCompat.Builder builder = baseBuilder()
                .setContentTitle("Downloading mnemonic images")
                .setContentText(String.format(Locale.ROOT, "%d / %d", done, total))
                .setProgress(100, percent, false)
                .setOngoing(true);
        postProgressThrottled(percent, builder);
    }

    /**
     * Replace the progress notification with a final "done" notification that
     * the user can swipe away.
     *
     * @param count number of images downloaded
     */
    public static void showDone(final int count) {
        createChannel();
        synchronized (MnemonicImageDownloadNotifier.class) {
            lastProgressPostMs = 0L;
            lastProgressPercent = -1;
        }
        final NotificationCompat.Builder builder = baseBuilder()
                .setContentTitle("Mnemonic images downloaded")
                .setContentText(String.format(Locale.ROOT, "%d images are now available in sessions", count))
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setAutoCancel(true);
        post(builder);
    }

    /**
     * Remove the progress notification (download aborted or cancelled).
     */
    public static void cancel() {
        try {
            NotificationManagerCompat.from(WkApplication.getInstance()).cancel(NOTIFICATION_ID);
        }
        catch (final SecurityException e) {
            LOGGER.error(e, "Could not cancel mnemonic download notification");
        }
    }

    private static NotificationCompat.Builder baseBuilder() {
        return new NotificationCompat.Builder(WkApplication.getInstance(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true);
    }
}
