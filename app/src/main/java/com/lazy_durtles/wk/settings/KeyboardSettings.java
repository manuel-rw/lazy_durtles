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
import javax.annotation.Nullable;
import static com.lazy_durtles.wk.util.ObjectSupport.isEmpty;


import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * Keyboard settings, extracted from GlobalSettings.
 */
public class KeyboardSettings {
    /**
     * Private constructor.
     */
    protected KeyboardSettings() {
        //
    }

    /**
     * Allow auto-correct/auto-suggest in meaning input. Normally this is blocked and the input
     * is set to 'visible password' to avoid inadvertent hints being provided by Android.
     *
     * @return the value
     */
    public static boolean getEnableAutoCorrectMeaning() {
        return prefs().getBoolean("enable_auto_correct_meaning", false);
    }

    /**
     * Allow auto-correct/auto-suggest in meaning input. Normally this is blocked and the input
     * is set to 'visible password' to avoid inadvertent hints being provided by Android.
     *
     * @param value the value
     */
    public static void setEnableAutoCorrectMeaning(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("enable_auto_correct_meaning", value);
        editor.apply();
    }

    /**
     * Allow auto-correct/auto-suggest in reading input. Normally this is blocked and the input
     * is set to 'visible password' to avoid inadvertent hints being provided by Android.
     *
     * @return the value
     */
    public static boolean getEnableAutoCorrectReading() {
        return prefs().getBoolean("enable_auto_correct_reading", false);
    }

    /**
     * Allow auto-correct/auto-suggest in reading input. Normally this is blocked and the input
     * is set to 'visible password' to avoid inadvertent hints being provided by Android.
     *
     * @param value the value
     */
    public static void setEnableAutoCorrectReading(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("enable_auto_correct_reading", value);
        editor.apply();
    }

    /**
     * Force an ASCII-capable keyboard for meaning answers.
     *
     * @return the value
     */
    public static boolean getForceAsciiMeaning() {
        return prefs().getBoolean("force_ascii_meaning", false);
    }

    /**
     * Force an ASCII-capable keyboard for meaning answers.
     *
     * @param value the value
     */
    public static void setForceAsciiMeaning(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("force_ascii_meaning", value);
        editor.apply();
    }

    /**
     * Force an ASCII-capable keyboard for reading answers.
     *
     * @return the value
     */
    public static boolean getForceAsciiReading() {
        return prefs().getBoolean("force_ascii_reading", false);
    }

    /**
     * Force visual password setting for meaning answers.
     *
     * @return the value
     */
    public static boolean getForceVisiblePasswordMeaning() {
        return prefs().getBoolean("force_visible_password_meaning", false);
    }

    /**
     * Force visual password setting for meaning answers.
     *
     * @param value the value
     */
    public static void setForceVisiblePasswordMeaning(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("force_visible_password_meaning", value);
        editor.apply();
    }

    /**
     * Force visual password setting for reading answers.
     *
     * @return the value
     */
    public static boolean getForceVisiblePasswordReading() {
        return prefs().getBoolean("force_visible_password_reading", false);
    }

    /**
     * Force visual password setting for reading answers.
     *
     * @param value the value
     */
    public static void setForceVisiblePasswordReading(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("force_visible_password_reading", value);
        editor.apply();
    }

    /**
     * Set an IME language hint for meanings.
     *
     * @return the value
     */
    public static boolean getImeHintMeaning() {
        return prefs().getBoolean("ime_hint_meaning", false);
    }

    /**
     * Set an IME language hint for readings.
     *
     * @return the value
     */
    public static boolean getImeHintReading() {
        return prefs().getBoolean("ime_hint_reading", false);
    }

    /**
     * Set the "no personalized learning" option on the IME.
     *
     * @return the value
     */
    public static boolean getEnableNoLearning() {
        return prefs().getBoolean("enable_no_learning", true);
    }

    /**
     * Set the "no personalized learning" option on the IME.
     *
     * @param value the value
     */
    public static void setEnableNoLearning(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("enable_no_learning", value);
        editor.apply();
    }

    /**
     * Delay in seconds to disable the next/submit button after typing an incorrect answer.
     *
     * @return the value
     */
    public static float getNextButtonDelay() {
        try {
            final @Nullable String value = prefs().getString("next_button_delay", null);
            if (isEmpty(value)) {
                return 0;
            }
            final float f = Float.parseFloat(value);
            if (f < 1) {
                return 0;
            }
            return f;
        }
        catch (final Exception e) {
            return 0;
        }
    }
}
