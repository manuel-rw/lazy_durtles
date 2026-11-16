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

import com.lazy_durtles.wk.enums.DontKnowButtonBehavior;
import com.lazy_durtles.wk.enums.SpecialButtonBehavior;
import javax.annotation.Nullable;
import static com.lazy_durtles.wk.Constants.DEFAULT_OVERDUE_THRESHOLD;
import static com.lazy_durtles.wk.util.ObjectSupport.isEmpty;


import com.lazy_durtles.wk.GlobalSettings;
import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * AdvancedOther settings, extracted from GlobalSettings.
 */
public class AdvancedOtherSettings {
    /**
     * Private constructor.
     */
    protected AdvancedOtherSettings() {
        //
    }

    /**
     * Quiz on'yomi and kun'yomi separately for kanji items.
     *
     * @return the value
     */
    public static boolean getKanjiModeOnKun() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("kanji_mode_onkun", false);
    }

    /**
     * Threshold for when to consider an item to be overdue.
     *
     * @return the value
     */
    public static float getOverdueThreshold() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return DEFAULT_OVERDUE_THRESHOLD;
        }
        try {
            final @Nullable String value = prefs().getString("overdue_threshold", null);
            if (isEmpty(value)) {
                return DEFAULT_OVERDUE_THRESHOLD;
            }
            final int n = Integer.parseInt(value);
            if (n < 1) {
                return DEFAULT_OVERDUE_THRESHOLD;
            }
            return n / 100.0f;
        }
        catch (final Exception e) {
            return DEFAULT_OVERDUE_THRESHOLD;
        }
    }

    /**
     * The action to perform when the special button 1 is pressed.
     *
     * @return the value
     */
    public static SpecialButtonBehavior getSpecialButton1Behavior() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return SpecialButtonBehavior.DISABLE;
        }
        final @Nullable String value = prefs().getString("undo_button", null);
        if (value != null) {
            try {
                return SpecialButtonBehavior.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return SpecialButtonBehavior.DISABLE;
    }

    /**
     * The action to perform when the special button 2 is pressed.
     *
     * @return the value
     */
    public static SpecialButtonBehavior getSpecialButton2Behavior() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return SpecialButtonBehavior.DISABLE;
        }
        final @Nullable String value = prefs().getString("ignore_button", null);
        if (value != null) {
            try {
                return SpecialButtonBehavior.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return SpecialButtonBehavior.DISABLE;
    }

    /**
     * The action to perform when the special button 3 is pressed.
     *
     * @return the value
     */
    public static SpecialButtonBehavior getSpecialButton3Behavior() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return SpecialButtonBehavior.DISABLE;
        }
        final @Nullable String value = prefs().getString("special_button_3", null);
        if (value != null) {
            try {
                return SpecialButtonBehavior.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return SpecialButtonBehavior.DISABLE;
    }

    /**
     * Whether to show the don't know button or not.
     *
     * @return the value
     */
    public static DontKnowButtonBehavior getDontKnowButtonBehavior() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return DontKnowButtonBehavior.ONLY_IF_IGNORE_HIDDEN;
        }
        final @Nullable String value = prefs().getString("dont_know_button", null);
        if (value != null) {
            try {
                return DontKnowButtonBehavior.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return DontKnowButtonBehavior.ONLY_IF_IGNORE_HIDDEN;
    }

    /**
     * Shake-and-retry if a reading answer for a single-kanji vocab is incorrect, but
     * the supplied answer is a known reading for the kanji that vocab is based on.
     *
     * @return the value
     */
    public static boolean getShakeOnMatchingKanji() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("shake_on_matching_kanji", true);
    }

    /**
     * Show the full, unlimited session log, which may contain inappropriate hints or answers.
     *
     * @return the value
     */
    public static boolean getFullSessionLog() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("full_session_log", false);
    }

    /**
     * For a kanji reading question, indicate if the question requires an on'yomi or kun'yomi answer.
     *
     * @return the value
     */
    public static boolean getIndicateKanjiReadingType() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("indicate_kanji_reading_type", false);
    }
}
