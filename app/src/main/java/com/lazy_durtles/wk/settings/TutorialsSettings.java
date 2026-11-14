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

package com.lazy_durtles.wk.settings;

import android.content.SharedPreferences;

import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * Tutorials settings, extracted from GlobalSettings.
 */
public class TutorialsSettings {
    protected TutorialsSettings() {
        //
    }

    /**
     * Dismiss the dashboard keyboard help popup.
     *
     * @return the value
     */
    public static boolean getKeyboardHelpDismissed() {
        return prefs().getBoolean("keyboard_help_dismissed", false);
    }

    /**
     * Dismiss the dashboard keyboard help popup.
     *
     * @param value the value
     */
    public static void setKeyboardHelpDismissed(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("keyboard_help_dismissed", value);
        editor.apply();
    }

    /**
     * Dismiss the tutorial on the browse overview screen.
     *
     * @return the value
     */
    public static boolean getBrowseOverviewDismissed() {
        return prefs().getBoolean("browse_overview_dismissed", false);
    }

    /**
     * Dismiss the dashboard keyboard help popup.
     *
     * @param value the value
     */
    public static void setBrowseOverviewDismissed(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("browse_overview_dismissed", value);
        editor.apply();
    }

    /**
     * Dismiss the tutorial on the browse overview screen.
     *
     * @return the value
     */
    public static boolean getSearchResultDismissed() {
        return prefs().getBoolean("search_result_dismissed", false);
    }

    /**
     * Dismiss the dashboard keyboard help popup.
     *
     * @param value the value
     */
    public static void setSearchResultDismissed(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("search_result_dismissed", value);
        editor.apply();
    }

    /**
     * Dismiss the tutorial on the browse overview screen.
     *
     * @return the value
     */
    public static boolean getStartSelfStudyDismissed() {
        return prefs().getBoolean("start_self_study_dismissed", false);
    }

    /**
     * Dismiss the dashboard keyboard help popup.
     *
     * @param value the value
     */
    public static void setStartSelfStudyDismissed(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("start_self_study_dismissed", value);
        editor.apply();
    }

    /**
     * Dismiss the tutorial on the browse overview screen.
     *
     * @return the value
     */
    public static boolean getSessionLogDismissed() {
        return prefs().getBoolean("session_log_dismissed", false);
    }

    /**
     * Dismiss the dashboard keyboard help popup.
     *
     * @param value the value
     */
    public static void setSessionLogDismissed(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("session_log_dismissed", value);
        editor.apply();
    }

    public static boolean getNotificationPermissionAsked() {
        return prefs().getBoolean("notification_permission_asked", false);
    }

    public static void setNotificationPermissionAsked(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("notification_permission_asked", value);
        editor.apply();
    }
}
