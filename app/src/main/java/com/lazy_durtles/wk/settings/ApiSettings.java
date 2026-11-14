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

import com.lazy_durtles.wk.enums.NetworkRule;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;
import com.lazy_durtles.wk.WkApplication;

/**
 * Api settings, extracted from GlobalSettings.
 */
public class ApiSettings {
    public static final Pattern blankTrimPattern = Pattern.compile("[\\p{Z}\\s]+");

    /**
     * Private constructor.
     */
    protected ApiSettings() {
        //
    }

    /**
     * The API token used to access the WaniKani API.
     *
     * @return the value
     */
    public static @Nullable String getApiKey() {
        @Nullable String key = WkApplication.getEncryptedPreferenceDataStore().getString("api_key", null);
        if (key != null) {
            key = blankTrimPattern.matcher(key).replaceAll("");
        }
        return key;
    }

    /**
     * The API token used to access the WaniKani API.
     *
     * @param apiKey the value
     */
    public static void setApiKey(final @Nullable String apiKey) {
        WkApplication.getEncryptedPreferenceDataStore().putString("api_key", apiKey);
    }

    /**
     * The rule for accessing the network (wifi only or always).
     *
     * @return the value
     */
    public static NetworkRule getNetworkRule() {
        final @Nullable String value = prefs().getString("network_rule", null);
        if (value != null) {
            try {
                return NetworkRule.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return NetworkRule.DOWNLOAD_WIFI_ONLY;
    }

    /**
     * The preferred location where to store downloaded audio files.
     *
     * @return the value
     */
    public static String getAudioLocation() {
        return prefs().getString("audio_location", "Internal");
    }

    /**
     * Automatically trigger download of audio files for upcoming reviews
     * or the current self-study session.
     *
     * @return the value
     */
    public static boolean getAutoDownloadAudio() {
        return prefs().getBoolean("auto_download_audio", true);
    }

    /**
     * Perform sync (run ApiTasks) in the background.
     *
     * @return the value
     */
    public static boolean getEnableBackgroundSync() {
        return prefs().getBoolean("enable_background_sync", false);
    }

    /**
     * Perform sync (run ApiTasks) when the app is opened.
     *
     * @return the value
     */
    public static boolean getSyncOnOpen() {
        return prefs().getBoolean("sync_on_open", false);
    }

    /**
     * The web password used to resurrect burned subjects.
     *
     * @return the value
     */
    public static @Nullable String getWebPassword() {
        return WkApplication.getEncryptedPreferenceDataStore().getString("web_password", null);
    }

    /**
     * The web password used to resurrect burned subjects.
     *
     * @param webPassword the value
     */
    public static void setWebPassword(final @Nullable String webPassword) {
        WkApplication.getEncryptedPreferenceDataStore().putString("web_password", webPassword);
    }
}
