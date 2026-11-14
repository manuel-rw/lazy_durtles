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
import android.content.res.Configuration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.lazy_durtles.wk.WkApplication;
import com.lazy_durtles.wk.enums.ActiveTheme;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import static com.lazy_durtles.wk.Constants.NUM_THEME_CUSTOMIZATION_OPTIONS;
import static com.lazy_durtles.wk.db.Converters.getObjectMapper;
import static com.lazy_durtles.wk.settings.SettingsPrefs.application;
import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * Display settings, extracted from GlobalSettings.
 */
public class DisplaySettings {
    /**
     * Private constructor.
     */
    protected DisplaySettings() {
        //
    }

    /**
     * The user's selected theme.
     *
     * @return the value
     */
    public static ActiveTheme getTheme() {
        final @Nullable String value = prefs().getString("theme", "LIGHT");
        ActiveTheme theme = ActiveTheme.LIGHT;
        try {
            theme = ActiveTheme.valueOf(value);
        } catch (final Exception e) {
            //
        }

        assert application() != null;
        int uiMode = application().getResources().getConfiguration().uiMode;
        uiMode &= Configuration.UI_MODE_NIGHT_MASK;
        if  (uiMode == Configuration.UI_MODE_NIGHT_YES) {
            try {
                theme = ActiveTheme.valueOf(prefs().getString("nightTheme", "DARK"));
            } catch (Exception e) {
                //
            }
        }

        return theme;
    }

    /**
     * The theme customizations for a given theme.
     *
     * @param theme the theme to customize
     * @return the value
     */
    public static List<Integer> getThemeCustomizations(final ActiveTheme theme) {
        final @Nullable String value = prefs().getString("theme_customizations_" + theme.name(), null);
        @Nullable List<Integer> list = null;
        if (value != null) {
            try {
                list = getObjectMapper().readValue(value, new TypeReference<List<Integer>>() {});
            }
            catch (final Exception e) {
                //
            }
        }

        if (list == null) {
            list = new ArrayList<>();
        }
        while (list.size() < NUM_THEME_CUSTOMIZATION_OPTIONS) {
            list.add(0);
        }

        return list;
    }

    /**
     * The theme customizations for a given theme.
     *
     * @param theme the theme to customize
     * @param value the value
     */
    public static void setThemeCustomizations(final ActiveTheme theme, final int[] value) {
        try {
            final SharedPreferences.Editor editor = prefs().edit();
            editor.putString("theme_customizations_" + theme.name(), getObjectMapper().writeValueAsString(value));
            editor.apply();
            theme.setDirty(true);
        }
        catch (final Exception e) {
            //
        }
    }

    /**
     * Center the caret in the quiz answer edit box.
     *
     * @return the value
     */
    public static boolean getCenterCaret() {
        return prefs().getBoolean("center_caret", false);
    }

    /**
     * Vertical size of the Anki mode buttons.
     *
     * @return the value
     */
    public static int getAnkiButtonsHeight() {
        return prefs().getInt("anki_buttons_height", 50);
    }

    /**
     * Vertical size of the main quiz question view.
     *
     * @return the value
     */
    public static int getQuizQuestionViewHeight() {
        return prefs().getInt("quiz_question_view_height", 0);
    }

    /**
     * Swap the correct/incorrect Anki mode buttons.
     *
     * @return the value
     */
    public static boolean getSwapAnkiButtons() {
        return prefs().getBoolean("swap_anki_buttons", false);
    }

    /**
     * Use slide animations for fragment transitions.
     *
     * @return the value
     */
    public static boolean getSlideAnimations() {
        return prefs().getBoolean("slide_animations", true);
    }

    /**
     * Stretch the question view to fill the screen when keyboard input is active.
     *
     * @return the value
     */
    public static boolean getStretchQuestionView() {
        return prefs().getBoolean("stretch_quiz_question_view", false);
    }
}
