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
 * Settings for the first-run onboarding flow. The onboarding activity runs
 * once, right after install, and guides the user through the API token and a
 * few setup questions before landing on the dashboard.
 */
public class OnboardingSettings {
    protected OnboardingSettings() {
        //
    }

    /**
     * Whether the user has completed the onboarding flow. Until this is true,
     * the app opens the onboarding activity instead of the main overview.
     *
     * @return the value
     */
    public static boolean getCompleted() {
        return prefs().getBoolean("onboarding_completed", false);
    }

    /**
     * Mark the onboarding flow as completed (or not).
     *
     * @param value the value
     */
    public static void setCompleted(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("onboarding_completed", value);
        editor.apply();
    }
}
