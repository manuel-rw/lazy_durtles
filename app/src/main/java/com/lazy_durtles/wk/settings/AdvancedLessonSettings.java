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
import com.lazy_durtles.wk.db.model.Subject;
import com.lazy_durtles.wk.enums.LessonOrder;
import com.lazy_durtles.wk.enums.SessionPriority;
import javax.annotation.Nullable;

import com.lazy_durtles.wk.GlobalSettings;
import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * AdvancedLesson settings, extracted from GlobalSettings.
 */
public class AdvancedLessonSettings {
    /**
     * Private constructor.
     */
    protected AdvancedLessonSettings() {
        //
    }

    /**
     * Order of items in lesson sessions.
     *
     * @return the value
     */
    public static LessonOrder getOrder() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return LessonOrder.LEVEL_THEN_TYPE;
        }
        final @Nullable String value = prefs().getString("lesson_order", null);
        if (value != null) {
            try {
                return LessonOrder.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return LessonOrder.LEVEL_THEN_TYPE;
    }

    /**
     * Reverse order of items in lesson sessions.
     *
     * @return the value
     */
    public static boolean getOrderReversed() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("lesson_order_reversed", false);
    }

    /**
     * Select items to prioritize over others in lesson sessions.
     *
     * @return the value
     */
    public static SessionPriority getOrderPriority() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return SessionPriority.NONE;
        }
        final @Nullable String value = prefs().getString("lesson_order_priority", null);
        if (value != null) {
            try {
                return SessionPriority.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return SessionPriority.NONE;
    }

    /**
     * Shuffle session items after the items have been selected for the session.
     *
     * @return the value
     */
    public static boolean getShuffleAfterSelection() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("lesson_shuffle_after_selection", false);
    }

    /**
     * Run meaning and reading questions back-to-back for each item in lesson sessions.
     *
     * @return the value
     */
    public static boolean getBackToBack() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("lesson_back_to_back", false);
    }

    /**
     * Always quiz reading before meaning for each item in lesson sessions.
     *
     * @return the value
     */
    public static boolean getReadingFirst() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("lesson_reading_first", false);
    }

    /**
     * Always quiz meaning before reading for each item in lesson sessions.
     *
     * @return the value
     */
    public static boolean getMeaningFirst() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("lesson_meaning_first", false);
    }

    /**
     * Use Anki mode for lesson sessions.
     *
     * @return the value
     */
    public static boolean getAnkiMode() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("anki_mode_lesson", false);
    }

    /**
     * Use Anki mode for meanings in lesson sessions.
     *
     * @return the value
     */
    public static boolean getAnkiModeMeaning() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("anki_mode_lesson_meaning", false);
    }

    /**
     * Use Anki mode for meanings in lesson sessions.
     *
     * @param value the value
     */
    public static void setAnkiModeMeaning(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("anki_mode_lesson_meaning", value);
        editor.apply();
    }

    /**
     * Use Anki mode for readings in lesson sessions.
     *
     * @return the value
     */
    public static boolean getAnkiModeReading() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return false;
        }
        return prefs().getBoolean("anki_mode_lesson_reading", false);
    }

    /**
     * Use Anki mode for readings in lesson sessions.
     *
     * @param value the value
     */
    public static void setAnkiModeReading(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("anki_mode_lesson_reading", value);
        editor.apply();
    }

    /**
     * Subject selection: min of current-level subjects.
     *
     * @return the value
     */
    public static int getSubjectsCurrentMin() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_current_min", -1);
    }

    /**
     * Subject selection: max of current-level subjects.
     *
     * @return the value
     */
    public static int getSubjectsCurrentMax() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_current_max", -1);
    }

    /**
     * Subject selection: min of earlier-level subjects.
     *
     * @return the value
     */
    public static int getSubjectsEarlierMin() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_earlier_min", -1);
    }

    /**
     * Subject selection: max of earlier-level subjects.
     *
     * @return the value
     */
    public static int getSubjectsEarlierMax() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_earlier_max", -1);
    }

    /**
     * Subject selection: min of radical subjects.
     *
     * @return the value
     */
    public static int getSubjectsRadicalMin() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_radical_min", -1);
    }

    /**
     * Subject selection: max of radical subjects.
     *
     * @return the value
     */
    public static int getSubjectsRadicalMax() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_radical_max", -1);
    }

    /**
     * Subject selection: min of kanji subjects.
     *
     * @return the value
     */
    public static int getSubjectsKanjiMin() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_kanji_min", -1);
    }

    /**
     * Subject selection: max of kanji subjects.
     *
     * @return the value
     */
    public static int getSubjectsKanjiMax() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_kanji_max", -1);
    }

    /**
     * Subject selection: min of vocabulary subjects.
     *
     * @return the value
     */
    public static int getSubjectsVocabularyMin() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_vocabulary_min", -1);
    }

    /**
     * Subject selection: max of vocabulary subjects.
     *
     * @return the value
     */
    public static int getSubjectsVocabularyMax() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_vocabulary_max", -1);
    }
    /**
     * Subject selection: min of kana vocabulary subjects.
     *
     * @return the value
     */
    public static int getSubjectsKanaVocabularyMin() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_Kana_vocabulary_min", -1);
    }

    /**
     * Subject selection: max of vocabulary subjects.
     *
     * @return the value
     */
    public static int getSubjectsKanaVocabularyMax() {
        if (!GlobalSettings.getAdvancedEnabled()) {
            return -1;
        }
        return prefs().getInt("lesson_subjects_kana_vocabulary_max", -1);
    }
}
