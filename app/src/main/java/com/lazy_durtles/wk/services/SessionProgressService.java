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

package com.lazy_durtles.wk.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.lazy_durtles.wk.R;
import com.lazy_durtles.wk.WkApplication;
import com.lazy_durtles.wk.model.Session;
import com.lazy_durtles.wk.util.Logger;

import javax.annotation.Nullable;

/**
 * A foreground service that keeps a persistent notification showing the
 * current session progress (e.g. "12/30" with a progress bar) while a
 * lesson or review session is active. On Android 13+ (API 33+) this uses
 * the dataSync foreground service type, which is allowed without extra
 * runtime permissions.
 */
public final class SessionProgressService extends Service {
    private static final Logger LOGGER = Logger.get(SessionProgressService.class);

    public static final String CHANNEL_ID = "SessionProgressChannel";
    public static final int NOTIFICATION_ID = 2;

    private static final String ACTION_START = "com.lazy_durtles.wk.action.START_SESSION_PROGRESS";
    private static final String ACTION_UPDATE = "com.lazy_durtles.wk.action.UPDATE_SESSION_PROGRESS";
    private static final String ACTION_STOP = "com.lazy_durtles.wk.action.STOP_SESSION_PROGRESS";

    /**
     * Start the foreground service if a session is active, posting the
     * initial progress notification.
     */
    public static void start(final Context context) {
        final Intent intent = new Intent(context, SessionProgressService.class);
        intent.setAction(ACTION_START);
        startForegroundServiceCompat(context, intent);
    }

    /**
     * Ask the running service to refresh its notification from the current
     * session state. Safe to call when the service isn't running; it will
     * be a no-op.
     */
    public static void update(final Context context) {
        final Intent intent = new Intent(context, SessionProgressService.class);
        intent.setAction(ACTION_UPDATE);
        try {
            context.startService(intent);
        } catch (final IllegalStateException e) {
            // Background start restrictions: the service may not be running,
            // which is fine — nothing to update.
            LOGGER.info("SessionProgressService.update: service not running, skipping");
        }
    }

    /**
     * Stop the foreground service and remove the progress notification.
     */
    public static void stop(final Context context) {
        final Intent intent = new Intent(context, SessionProgressService.class);
        intent.setAction(ACTION_STOP);
        try {
            context.startService(intent);
        } catch (final IllegalStateException e) {
            // Service not running; make sure the notification is gone anyway.
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
        }
        context.stopService(new Intent(context, SessionProgressService.class));
    }

    private static void startForegroundServiceCompat(final Context context, final Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(final @Nullable Intent intent, final int flags, final int startId) {
        final String action = intent != null ? intent.getAction() : null;
        if (ACTION_STOP.equals(action)) {
            stopForegroundCompat();
            stopSelf();
            return START_NOT_STICKY;
        }

        final Session session = Session.getInstance();
        if (!session.isActive() && session.getType() == com.lazy_durtles.wk.enums.SessionType.NONE) {
            // No active session; don't show a notification.
            stopForegroundCompat();
            stopSelf();
            return START_NOT_STICKY;
        }

        final Notification notification = buildNotification(session);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        if (ACTION_UPDATE.equals(action)) {
            // Update-only intent: refresh and keep running.
            updateNotification(session);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForegroundCompat();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private void stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }

    @Override
    public @Nullable IBinder onBind(final Intent intent) {
        return null;
    }

    /**
     * Refresh the notification from the current session state. Call this
     * whenever the session progress changes.
     */
    @SuppressLint("MissingPermission") // guarded by hasNotificationPermission()
    public void updateNotification(final Session session) {
        if (!hasNotificationPermission()) {
            return;
        }
        final Notification notification = buildNotification(session);
        final NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification(final Session session) {
        final String title = session.getType().getDescription() + " session";
        final String progressText = session.getProgressText();

        final int total = session.getItems().size();
        final int done = session.getItems().stream()
                .filter(item -> item.isReported() || item.isPending())
                .mapToInt(item -> 1)
                .sum();
        final int max = Math.max(total, 1);
        final int progress = Math.min(done, max);

        final Intent contentIntent = new Intent(this, com.lazy_durtles.wk.activities.MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final int piFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? android.app.PendingIntent.FLAG_IMMUTABLE
                : 0;
        final android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, contentIntent, piFlags);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_stat_name);
        builder.setContentTitle(title);
        builder.setContentText(progressText);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setOnlyAlertOnce(true);
        builder.setCategory(NotificationCompat.CATEGORY_PROGRESS);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setProgress(max, progress, false);
        builder.setSubText(progressText);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Session Progress",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows progress during an active lesson or review session");
            channel.setShowBadge(false);
            final NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private boolean hasNotificationPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
    }
}
