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
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import static com.lazy_durtles.wk.db.Converters.getObjectMapper;
import static com.lazy_durtles.wk.util.ObjectSupport.isEmpty;
import static com.lazy_durtles.wk.util.ObjectSupport.safe;


import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * Font settings, extracted from GlobalSettings.
 */
public class FontSettings {
    /**
     * Private constructor.
     */
    protected FontSettings() {
        //
    }

    /**
     * Get a list of font IDs for quiz question display. Internal fonts have an int represented
     * as a string "1" .. "8", imported fonts have their file name as font ID.
     *
     * @return the value
     */
    public static List<String> getSelectedFonts() {
        final @Nullable String value = prefs().getString("selected_fonts", null);
        if (isEmpty(value)) {
            return new ArrayList<>();
        }

        return safe(ArrayList::new, () -> {
            if (value.charAt(0) == '[') {
                return getObjectMapper().readValue(value, new TypeReference<List<String>>() {});
            }
            else {
                final List<String> result = new ArrayList<>();
                for (final String s: value.split(" ")) {
                    if (isEmpty(s)) {
                        continue;
                    }
                    result.add(s);
                }
                return result;
            }
        });
    }

    /**
     * Test to see if a font has been selected for quiz question display.
     *
     * @param fontId the ID of the font to query
     * @return the value
     */
    public static boolean isFontSelected(final String fontId) {
        return getSelectedFonts().contains(fontId);
    }

    /**
     * Select or un-select a font for quiz question display.
     *
     * @param fontId the ID of the font to adjust
     * @param selected true if this font should be included
     */
    public static void setFontSelected(final String fontId, final boolean selected) {
        final Collection<String> ids = getSelectedFonts();
        if (selected && !ids.contains(fontId)) {
            ids.add(fontId);
        }
        else if (!selected) {
            ids.remove(fontId);
        }

        final SharedPreferences.Editor editor = prefs().edit();
        safe(() -> editor.putString("selected_fonts", getObjectMapper().writeValueAsString(ids)));
        editor.apply();
    }

    /**
     * Maximum size of the font for the subject info button when shown in lesson presentation.
     *
     * @return the value
     */
    public static int getMaxFontSizeLesson() {
        return prefs().getInt("max_font_size_lesson", 128);
    }

    /**
     * Maximum size of the font for the subject info button when shown in quizzes.
     *
     * @return the value
     */
    public static int getMaxFontSizeQuiz() {
        return prefs().getInt("max_font_size_quiz", 28);
    }

    /**
     * Maximum size of the font for the subject info button when shown while browsing the subject database.
     *
     * @return the value
     */
    public static int getMaxFontSizeBrowse() {
        return prefs().getInt("max_font_size_browse", 64);
    }

    /**
     * Maximum size of the font for the quiz question text.
     *
     * @return the value
     */
    public static int getMaxFontSizeQuizText() {
        return prefs().getInt("max_font_size_quiz_text", 100);
    }

    /**
     * Maximum size of the font for the quiz question text.
     *
     * @param value the value
     */
    public static void setMaxFontSizeQuizText(final int value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putInt("max_font_size_quiz_text", value);
        editor.apply();
    }

    /**
     * Font size for the question input text box.
     *
     * @return the value
     */
    public static int getFontSizeQuestionEdit() {
        return prefs().getInt("font_size_question_edit", 14);
    }

    /**
     * Font size for the body text in subject info.
     *
     * @return the value
     */
    public static int getFontSizeSubjectInfo() {
        return prefs().getInt("font_size_subject_info", 14);
    }

    /**
     * Font size for the dashboard subject tables (critical condition, recent unlocks, recently burned).
     *
     * @return the value
     */
    public static int getFontSizeLiveSubjectTable() {
        return prefs().getInt("font_size_live_subject_table", 14);
    }

    /**
     * Font size for the Anki mode answer display.
     *
     * @return the value
     */
    public static int getFontSizeAnkiAnswer() {
        return prefs().getInt("font_size_anki_answer", 18);
    }
}
