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

import com.lazy_durtles.wk.WkApplication;
import com.lazy_durtles.wk.model.Session;
import com.lazy_durtles.wk.util.Logger;

import static com.lazy_durtles.wk.util.ObjectSupport.safe;

/**
 * Thin facade over {@link SessionProgressService} so that the Session
 * singleton can start, update, and stop the progress notification without
 * depending on Android Context details directly.
 */
public final class SessionNotificationHelper {
    private static final Logger LOGGER = Logger.get(SessionNotificationHelper.class);

    private SessionNotificationHelper() {
        //
    }

    /**
     * Start the foreground progress notification for the current session.
     * No-op if the session is inactive.
     */
    public static void startProgressNotification() {
        safe(() -> {
            final Session session = Session.getInstance();
            if (session.isInactive()) {
                return;
            }
            LOGGER.info("Starting session progress notification");
            SessionProgressService.start(WkApplication.getInstance());
        });
    }

    /**
     * Refresh the progress notification to reflect the current session state.
     * No-op if the session is inactive.
     */
    public static void updateProgressNotification() {
        safe(() -> {
            final Session session = Session.getInstance();
            if (session.isInactive()) {
                return;
            }
            SessionProgressService.update(WkApplication.getInstance());
        });
    }

    /**
     * Stop the foreground progress notification and remove it.
     */
    public static void stopProgressNotification() {
        safe(() -> {
            LOGGER.info("Stopping session progress notification");
            SessionProgressService.stop(WkApplication.getInstance());
        });
    }
}
