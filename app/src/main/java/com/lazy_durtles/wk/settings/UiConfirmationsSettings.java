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
 * UiConfirmations settings, extracted from GlobalSettings.
 */
public class UiConfirmationsSettings {
    protected UiConfirmationsSettings() {
        //
    }

    /**
     * Ask for UI confirmation before abandoning a session.
     *
     * @return the value
     */
    public static boolean getUiConfirmAbandonSession() {
        return prefs().getBoolean("ui_confirm_abandon_session", true);
    }

    /**
     * Ask for UI confirmation before abandoning a session.
     *
     * @param value the value
     */
    public static void setUiConfirmAbandonSession(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("ui_confirm_abandon_session", value);
        editor.apply();
    }

    /**
     * Ask for UI confirmation before wrapping up a session.
     *
     * @return the value
     */
    public static boolean getUiConfirmWrapupSession() {
        return prefs().getBoolean("ui_confirm_wrapup_session", true);
    }

    /**
     * Ask for UI confirmation before wrapping up a session.
     *
     * @param value the value
     */
    public static void setUiConfirmWrapupSession(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("ui_confirm_wrapup_session", value);
        editor.apply();
    }
}
