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
import com.lazy_durtles.wk.enums.VoicePreference;
import javax.annotation.Nullable;

import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * Audio settings, extracted from GlobalSettings.
 */
public class AudioSettings {
    /**
     * Private constructor.
     */
    protected AudioSettings() {
        //
    }

    /**
     * Autoplay audio in lesson presentation.
     *
     * @return the value
     */
    public static boolean getAutoplayLessonPresentation() {
        return prefs().getBoolean("autoplay_lesson_presentation", true);
    }

    /**
     * Autoplay audio in lesson presentation.
     *
     * @param value the value
     */
    public static void setAutoplayLessonPresentation(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("autoplay_lesson_presentation", value);
        editor.apply();
    }

    /**
     * Autoplay audio in lessons.
     *
     * @return the value
     */
    public static boolean getAutoplayLessons() {
        return prefs().getBoolean("autoplay_lessons", true);
    }

    /**
     * Autoplay audio in reviews.
     *
     * @return the value
     */
    public static boolean getAutoplayReviews() {
        return prefs().getBoolean("autoplay_reviews", true);
    }

    /**
     * Autoplay audio on Anki mode reveal.
     *
     * @return the value
     */
    public static boolean getAutoplayAnkiReveal() {
        return prefs().getBoolean("autoplay_anki_reveal", true);
    }

    /**
     * Autoplay audio in lesson presentation.
     *
     * @param value the value
     */
    public static void setAutoplayAnkiReveal(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("autoplay_anki_reveal", value);
        editor.apply();
    }

    /**
     * Preference for the voice to use for vocab audio.
     *
     * @return the value
     */
    public static VoicePreference getVoicePreference() {
        final @Nullable String value = prefs().getString("voice_preference", null);
        if (value != null) {
            try {
                return VoicePreference.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return VoicePreference.RANDOM;
    }

    /**
     * Use audio focus for playing audio.
     *
     * @return the value
     */
    public static boolean getUseAudioFocus() {
        return prefs().getBoolean("use_audio_focus", true);
    }
}
