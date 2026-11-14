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
import com.lazy_durtles.wk.enums.NotificationCategory;
import com.lazy_durtles.wk.enums.NotificationPriority;
import com.lazy_durtles.wk.enums.NotificationUpdateFrequency;
import javax.annotation.Nullable;
import static com.lazy_durtles.wk.util.ObjectSupport.isEmpty;


import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * Other settings, extracted from GlobalSettings.
 */
public class OtherSettings {
    /**
     * Private constructor.
     */
    protected OtherSettings() {
        //
    }

    /**
     * Enable notifications for new lessons and reviews.
     *
     * @return the value
     */
    public static boolean getEnableNotifications() {
        return prefs().getBoolean("enable_notifications", true);
    }

    /**
     * Notification update frequency.
     *
     * @return the value
     */
    public static NotificationUpdateFrequency getNotificationUpdateFrequency() {
        final @Nullable String value = prefs().getString("notification_update_frequency", null);
        if (value != null) {
            try {
                return NotificationUpdateFrequency.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return NotificationUpdateFrequency.ONLY_NEW_REVIEWS;
    }

    /**
     * Notification priority.
     *
     * @return the value
     */
    public static NotificationPriority getNotificationPriority() {
        final @Nullable String value = prefs().getString("notification_priority", null);
        if (value != null) {
            try {
                return NotificationPriority.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return NotificationPriority.DEFAULT;
    }

    /**
     * Notification priority.
     *
     * @param value the value
     */
    public static void setNotificationPriority(final NotificationPriority value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putString("notification_priority", value.name());
        editor.apply();
    }

    /**
     * Notification category.
     *
     * @return the value
     */
    public static NotificationCategory getNotificationCategory() {
        final @Nullable String value = prefs().getString("notification_category", null);
        if (value != null) {
            try {
                return NotificationCategory.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return NotificationCategory.RECOMMENDATION;
    }

    /**
     * Trigger notifications with a low priority, i.e. silently.
     *
     * @return the value
     */
    public static boolean getNotificationLowPriority() {
        return prefs().getBoolean("notification_low_priority", false);
    }

    /**
     * Show on'yomi readings in katakana instead of hiragana.
     *
     * @return the value
     */
    public static boolean getShowOnInKatakana() {
        return prefs().getBoolean("show_onyomi_in_katakana", false);
    }

    /**
     * When asking for an on'yomi answer, require the answer to be given in katakana instead of hiragana.
     *
     * @return the value
     */
    public static boolean getRequireOnInKatakana() {
        return prefs().getBoolean("require_onyomi_in_katakana", false);
    }

    /**
     * Is there a search engine configured for the given slot?
     *
     * @param index the search engine slot 1..5
     * @return true if a name and URL are configured
     */
    public static boolean hasSearchEngine(final int index) {
        return !isEmpty(getSearchEngineName(index)) && !isEmpty(getSearchEngineUrl(index));
    }

    /**
     * The name for the search engine in the given slot.
     *
     * @param index the search engine slot 1..5
     * @return the name, or an empty string if not configured
     */
    public static String getSearchEngineName(final int index) {
        final @Nullable String value = prefs().getString("search_engine_" + index + "_tag", null);
        if (value != null) {
            return value;
        }
        if (index == 1) {
            return "Wiktionary";
        }
        if (index == 2) {
            return "Jisho";
        }
        return "";
    }

    /**
     * The URL for the search engine in the given slot. In the URL, "%s" is a placeholder for the search query.
     *
     * @param index the search engine slot 1..5
     * @return the URL, or an empty string if not configured
     */
    public static String getSearchEngineUrl(final int index) {
        final @Nullable String value = prefs().getString("search_engine_" + index, null);
        if (value != null) {
            return value;
        }
        if (index == 1) {
            return "https://ja.wiktionary.org/w/index.php?search=%s";
        }
        if (index == 2) {
            return "https://jisho.org/search/%s";
        }
        return "";
    }

    /**
     * Show star ratings for subjects.
     *
     * @return the value
     */
    public static boolean getEnableStarRatings() {
        return prefs().getBoolean("enable_star_ratings", false);
    }
}
