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
import com.lazy_durtles.wk.enums.QuestionType;
import com.lazy_durtles.wk.enums.SubjectInfoDump;
import javax.annotation.Nullable;

import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * SubjectInfo settings, extracted from GlobalSettings.
 */
public class SubjectInfoSettings {
    /**
     * Private constructor.
     */
    protected SubjectInfoSettings() {
        //
    }

    /**
     * Hide readings in lesson presentation until they are tapped in the subject info dump.
     *
     * @return the value
     */
    public static boolean getHideLessonReadings() {
        return prefs().getBoolean("hide_lesson_readings", false);
    }

    /**
     * Hide context sentence translations until they are tapped in the subject info dump.
     *
     * @return the value
     */
    public static boolean getHideSentenceTranslations() {
        return prefs().getBoolean("hide_sentence_translations", false);
    }

    /**
     * Show pitch info.
     *
     * @return the value
     */
    public static boolean getShowPitchInfo() {
        return prefs().getBoolean("show_pitch_info", false);
    }

    /**
     * Swap the 'visually similar' and 'used in' tables in the subject info dump.
     *
     * @return the value
     */
    public static boolean getSwapSimilarAndAmalgamations() {
        return prefs().getBoolean("swap_similar_amalgamations", false);
    }

    /**
     * Highlight subject type tags in mnemonic texts.
     *
     * @return the value
     */
    public static boolean getHighlightSubjectTags() {
        return prefs().getBoolean("highlight_subject_tags", true);
    }

    /**
     * Always hide WaniKani's mnemonics regardless of user notes
     *
     * @return the value
     */
    public static boolean getAlwaysHideWKMnemonic() {
        return prefs().getBoolean("always_hide_wk_mnemonic", false);
    }

    /**
     * Helper method to invert the always_hide_wk_mnemonic preference.
     * Used for enabling/disabling conditional mnemonic hiding preferences.
     *
     * @return true if mnemonics should not always be hidden
     */
    public static boolean getNotAlwaysHideWKMnemonic() {
        return !prefs().getBoolean("always_hide_wk_mnemonic", false);
    }

    /**
     * Helper method to invert the always_hide_wk_mnemonic preference.
     * Used for enabling/disabling conditional mnemonic hiding preferences.
     *
     * @param value true if mnemonics should not always be hidden
     */
    public static void setNotAlwaysHideWKMnemonic(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("not_always_hide_wk_mnemonic", value);
        editor.apply();
    }

    /**
     * Hide WaniKani's meaning mnemonic if a user note is present
     *
     * @return the value
     */
    public static boolean getHideMeaningMnemonic(final boolean hasMeaningNote) {
        return prefs().getBoolean("always_hide_wk_mnemonic", false) ||
               prefs().getBoolean("hide_meaning_mnemonic", false) && hasMeaningNote;
    }

    /**
     * Hide WaniKani's reading mnemonic if a user note is present
     *
     * @return the value
     */
    public static boolean getHideReadingMnemonic(final boolean hasReadingNote) {
        return prefs().getBoolean("always_hide_wk_mnemonic", false) ||
               prefs().getBoolean("hide_reading_mnemonic", false) && hasReadingNote;
    }

    /**
     * Show the legacy names and mnemonics for old radicals.
     *
     * @return the value
     */
    public static boolean getShowLegacy() {
        return prefs().getBoolean("show_legacy", false);
    }

    /**
     * The parts of the subject info to show after a correct answer with lightning mode off (meaning questions).
     *
     * @return the value
     */
    public static SubjectInfoDump getMeaningInfoDump() {
        final @Nullable String value = prefs().getString("meaning_info_dump", null);
        if (value != null) {
            try {
                return SubjectInfoDump.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return SubjectInfoDump.ANSWERS_ONLY;
    }

    /**
     * The parts of the subject info to show after a correct answer with lightning mode off (reading questions).
     *
     * @return the value
     */
    public static SubjectInfoDump getReadingInfoDump() {
        final @Nullable String value = prefs().getString("reading_info_dump", null);
        if (value != null) {
            try {
                return SubjectInfoDump.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return SubjectInfoDump.ANSWERS_ONLY;
    }

    /**
     * The parts of the subject info to show after an incorrect answer (meaning questions).
     *
     * @return the value
     */
    public static SubjectInfoDump getMeaningInfoDumpIncorrect() {
        final @Nullable String value = prefs().getString("meaning_info_dump_incorrect", null);
        if (value != null) {
            try {
                return SubjectInfoDump.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return SubjectInfoDump.ANSWERS_ONLY;
    }

    /**
     * The parts of the subject info to show after an incorrect answer (meaning questions).
     *
     * @param value the value
     */
    public static void setMeaningInfoDumpIncorrect(final SubjectInfoDump value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putString("meaning_info_dump_incorrect", value.name());
        editor.apply();
    }

    /**
     * The parts of the subject info to show after an incorrect answer (reading questions).
     *
     * @return the value
     */
    public static SubjectInfoDump getReadingInfoDumpIncorrect() {
        final @Nullable String value = prefs().getString("reading_info_dump_incorrect", null);
        if (value != null) {
            try {
                return SubjectInfoDump.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return SubjectInfoDump.ANSWERS_ONLY;
    }

    /**
     * The parts of the subject info to show after an incorrect answer (reading questions).
     *
     * @param value the value
     */
    public static void setReadingInfoDumpIncorrect(final SubjectInfoDump value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putString("reading_info_dump_incorrect", value.name());
        editor.apply();
    }

    /**
     * The parts of the subject info to show after an answer.
     *
     * @param questionType the type of the current question
     * @param correct was the answer correct?
     * @return the dump enum instance
     */
    public static SubjectInfoDump getInfoDump(final QuestionType questionType, final boolean correct) {
        if (questionType.isMeaning()) {
            return correct ? getMeaningInfoDump() : getMeaningInfoDumpIncorrect();
        }
        else {
            return correct ? getReadingInfoDump() : getReadingInfoDumpIncorrect();
        }
    }

    /**
     * Show stroke order diagrams.
     *
     * @return the value
     */
    public static boolean getShowStrokeOrder() {
        return prefs().getBoolean("show_stroke_order", false);
    }

    /**
     * Animate the stroke order diagrams.
     *
     * @return the value
     */
    public static boolean getStrokeOrderAnimated() {
        return prefs().getBoolean("stroke_order_animated", true);
    }

    /**
     * Size of the stroke order diagrams.
     *
     * @return the value
     */
    public static int getStrokeOrderSize() {
        return prefs().getInt("stroke_order_size", 250);
    }
}
