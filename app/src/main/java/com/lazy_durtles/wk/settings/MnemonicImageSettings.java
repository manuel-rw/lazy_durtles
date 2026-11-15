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
 * Settings for the optional WaniKani mnemonic image data pack. The drawings
 * themselves are copyright WaniKani / Tofugu; this app only caches them
 * locally for display during sessions.
 */
public class MnemonicImageSettings {
    /**
     * Private constructor.
     */
    protected MnemonicImageSettings() {
        //
    }

    /**
     * Whether the optional mnemonic image data pack is enabled. When enabled and
     * downloaded, mnemonic background drawings are shown in sessions.
     *
     * @return the value
     */
    public static boolean getEnabled() {
        return prefs().getBoolean("mnemonic_images_enabled", false);
    }

    /**
     * Whether the optional mnemonic image data pack is enabled.
     *
     * @param value the value
     */
    public static void setEnabled(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("mnemonic_images_enabled", value);
        editor.apply();
    }

    /**
     * Whether the user has already been asked about the optional data pack during
     * onboarding. Used to show the prompt only once.
     *
     * @return the value
     */
    public static boolean getOnboardingAsked() {
        return prefs().getBoolean("mnemonic_images_onboarding_asked", false);
    }

    /**
     * Whether the user has already been asked about the optional data pack.
     *
     * @param value the value
     */
    public static void setOnboardingAsked(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("mnemonic_images_onboarding_asked", value);
        editor.apply();
    }

    /**
     * Timestamp (millis) before which the download task must not run again after a
     * failed attempt. Prevents a tight retry loop when the network is down.
     *
     * @return the value
     */
    public static long getRetryNotBefore() {
        return prefs().getLong("mnemonic_images_retry_not_before", 0L);
    }

    /**
     * Timestamp (millis) before which the download task must not run again.
     *
     * @param value the value
     */
    public static void setRetryNotBefore(final long value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putLong("mnemonic_images_retry_not_before", value);
        editor.apply();
    }
}
