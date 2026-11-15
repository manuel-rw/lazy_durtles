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

package com.lazy_durtles.wk;

import android.content.SharedPreferences;
import android.util.Log;

import com.lazy_durtles.wk.db.model.Subject;
import com.lazy_durtles.wk.enums.QuestionType;
import com.lazy_durtles.wk.enums.SessionPriority;
import com.lazy_durtles.wk.enums.SessionType;
import com.lazy_durtles.wk.model.SubjectSelectionRules;
import com.lazy_durtles.wk.settings.AdvancedLessonSettings;
import com.lazy_durtles.wk.settings.AdvancedOtherSettings;
import com.lazy_durtles.wk.settings.AdvancedReviewSettings;
import com.lazy_durtles.wk.settings.AdvancedSelfStudySettings;
import com.lazy_durtles.wk.settings.ApiSettings;
import com.lazy_durtles.wk.settings.AudioSettings;
import com.lazy_durtles.wk.settings.DashboardSettings;
import com.lazy_durtles.wk.settings.DisplaySettings;
import com.lazy_durtles.wk.settings.FontSettings;
import com.lazy_durtles.wk.settings.KeyboardSettings;
import com.lazy_durtles.wk.settings.MnemonicImageSettings;
import com.lazy_durtles.wk.settings.OtherSettings;
import com.lazy_durtles.wk.settings.ReviewSettings;
import com.lazy_durtles.wk.settings.SettingsPrefs;
import com.lazy_durtles.wk.settings.SubjectInfoSettings;
import com.lazy_durtles.wk.settings.TutorialsSettings;
import com.lazy_durtles.wk.settings.UiConfirmationsSettings;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.lazy_durtles.wk.enums.SessionType.LESSON;
import static com.lazy_durtles.wk.enums.SessionType.REVIEW;
import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * A class with a bunch of static accessors for app settings.
 *
 * <p>
 *     Most settings only have getters, since most settings can only be modified
 *     via the settings screen. These accessors also handle default values and
 *     format conversions transparently.
 * </p>
 *
 * <p>
 *     The per-category settings live in the {@code settings} subpackage
 *     (e.g. {@link ApiSettings}, {@link DisplaySettings}, etc.). This class
 *     re-exports them as nested classes for backwards compatibility and
 *     provides the cross-category convenience methods.
 * </p>
 */
public final class GlobalSettings {

    /**
     * Set the application instance.
     *
     * @param application the application
     */
    public static void setApplication(final WkApplication application) {
        SettingsPrefs.setApplication(application);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private GlobalSettings() {
        //
    }

    // -----------------------------------------------------------------------
    // Backwards-compatible nested class aliases
    // -----------------------------------------------------------------------

    public static final class Api extends ApiSettings {}
    public static final class Display extends DisplaySettings {}
    public static final class Dashboard extends DashboardSettings {}
    public static final class SubjectInfo extends SubjectInfoSettings {}
    public static final class MnemonicImages extends MnemonicImageSettings {}
    public static final class Audio extends AudioSettings {}
    public static final class Review extends ReviewSettings {}
    public static final class Keyboard extends KeyboardSettings {}
    public static final class Font extends FontSettings {}
    public static final class Other extends OtherSettings {}
    public static final class Experimental extends com.lazy_durtles.wk.settings.ExperimentalSettings {}
    public static final class AdvancedLesson extends AdvancedLessonSettings {}
    public static final class AdvancedReview extends AdvancedReviewSettings {}
    public static final class AdvancedSelfStudy extends AdvancedSelfStudySettings {}
    public static final class AdvancedOther extends AdvancedOtherSettings {}
    public static final class UiConfirmations extends UiConfirmationsSettings {}
    public static final class Tutorials extends TutorialsSettings {}

    // -----------------------------------------------------------------------
    // Top-level settings
    // -----------------------------------------------------------------------

    /**
     * This is 0 if the user in in the first-time setup phase.
     *
     * @return the value
     */
    public static int getFirstTimeSetup() {
        return prefs().getInt("first_time_setup", 0);
    }

    /**
     * This is 0 if the user in in the first-time setup phase.
     *
     * @param value the value
     */
    public static void setFirstTimeSetup(final int value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putInt("first_time_setup", value);
        editor.apply();
    }

    /**
     * This is the handler for user settings backup
     *
     */
    public static Map<String, ?> getAllSettings() {
        Map<String, ?> entries = prefs().getAll();
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Log.d("Settings", "Key: " + key + " Value: " + value.toString() + " Type: " + value.getClass().getSimpleName());
            result.put(key, value);
        }
        return result;
    }

    public static void setAllSettings(Map<String, Object> settings) {
        SharedPreferences.Editor editor = prefs().edit();

        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Log.d("Settings", "Key: " + key + " Value: " + value.toString());

            if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof String) {
                editor.putString(key, (String) value);
            } else {
                throw new IllegalArgumentException("Unsupported setting type: " + value.getClass().getName());
            }
        }
        editor.apply();
    }

    /**
     * Global unlock of all of the advanced settings.
     *
     * @return the value
     */
    public static boolean getAdvancedEnabled() {
        return prefs().getBoolean("enable_advanced", false);
    }

    /**
     * Global unlock of all of the advanced settings.
     *
     * @param value the value
     */
    public static void setAdvancedEnabled(final boolean value) {
        final SharedPreferences.Editor editor = prefs().edit();
        editor.putBoolean("enable_advanced", value);
        editor.apply();
    }

    // -----------------------------------------------------------------------
    // Cross-category convenience methods
    // -----------------------------------------------------------------------

    /**
     * Get the Anki mode setting for the given combination of session type and question type.
     */
    public static boolean getAnkiMode(final SessionType sessionType, final QuestionType questionType) {
        if (sessionType == LESSON) {
            return questionType.isMeaning()
                    ? AdvancedLessonSettings.getAnkiModeMeaning()
                    : AdvancedLessonSettings.getAnkiModeReading();
        } else if (sessionType == REVIEW) {
            return questionType.isMeaning()
                    ? AdvancedReviewSettings.getAnkiModeMeaning()
                    : AdvancedReviewSettings.getAnkiModeReading();
        } else {
            return questionType.isMeaning()
                    ? AdvancedSelfStudySettings.getAnkiModeMeaning()
                    : AdvancedSelfStudySettings.getAnkiModeReading();
        }
    }

    /**
     * Get the randomize inflection setting for the given combination of session type.
     */
    public static boolean getRandomizeInflections(final SessionType sessionType) {
        if (sessionType == LESSON) {
            return false;
        } else if (sessionType == REVIEW) {
            return AdvancedReviewSettings.getRandomizeInflections();
        } else {
            return AdvancedSelfStudySettings.getRandomizeInflections();
        }
    }

    /**
     * Get the autoplay setting for the given session type.
     */
    public static boolean getAutoPlay(final SessionType sessionType) {
        return sessionType == LESSON ? AudioSettings.getAutoplayLessons() : AudioSettings.getAutoplayReviews();
    }

    /**
     * Get the shuffle-after-selection setting for the given session type.
     */
    public static boolean getShuffleAfterSelection(final SessionType sessionType) {
        switch (sessionType) {
            case LESSON: return AdvancedLessonSettings.getShuffleAfterSelection();
            case REVIEW: return AdvancedReviewSettings.getShuffleAfterSelection();
            case SELF_STUDY: return AdvancedSelfStudySettings.getShuffleAfterSelection();
            default: return false;
        }
    }

    /**
     * Get the back-to-back setting for the given session type.
     */
    public static boolean getBackToBack(final SessionType sessionType) {
        switch (sessionType) {
            case LESSON: return AdvancedLessonSettings.getBackToBack();
            case REVIEW: return AdvancedReviewSettings.getBackToBack();
            case SELF_STUDY: return AdvancedSelfStudySettings.getBackToBack();
            default: return false;
        }
    }

    /**
     * Get the reading-before-meaning setting for the given session type.
     */
    public static boolean getReadingFirst(final SessionType sessionType) {
        switch (sessionType) {
            case LESSON: return AdvancedLessonSettings.getReadingFirst();
            case REVIEW: return AdvancedReviewSettings.getReadingFirst();
            case SELF_STUDY: return AdvancedSelfStudySettings.getReadingFirst();
            default: return false;
        }
    }

    /**
     * Get the meaning-before-reading setting for the given session type.
     */
    public static boolean getMeaningFirst(final SessionType sessionType) {
        switch (sessionType) {
            case LESSON: return AdvancedLessonSettings.getMeaningFirst();
            case REVIEW: return AdvancedReviewSettings.getMeaningFirst();
            case SELF_STUDY: return AdvancedSelfStudySettings.getMeaningFirst();
            default: return false;
        }
    }

    /**
     * Get the subject comparator (for session ordering) for the given session type.
     */
    public static Comparator<Subject> getSubjectComparator(final SessionType sessionType) {
        switch (sessionType) {
            case LESSON: return AdvancedLessonSettings.getOrder().getComparator();
            case REVIEW: return AdvancedReviewSettings.getOrder().getComparator();
            case SELF_STUDY:
            default: return AdvancedSelfStudySettings.getOrder().getComparator();
        }
    }

    /**
     * Get the order-reversed setting for the given session type.
     */
    public static boolean getOrderReversed(final SessionType sessionType) {
        switch (sessionType) {
            case LESSON: return AdvancedLessonSettings.getOrderReversed();
            case REVIEW: return AdvancedReviewSettings.getOrderReversed();
            case SELF_STUDY: return AdvancedSelfStudySettings.getOrderReversed();
            default: return false;
        }
    }

    /**
     * Get the order-overdue-first setting for the given session type.
     */
    public static boolean getOrderOverdueFirst(final SessionType sessionType) {
        switch (sessionType) {
            case REVIEW: return AdvancedReviewSettings.getOrderOverdueFirst();
            case SELF_STUDY: return AdvancedSelfStudySettings.getOrderOverdueFirst();
            case LESSON:
            default: return false;
        }
    }

    /**
     * Get the session order priority for the given session type.
     */
    public static SessionPriority getOrderPriority(final SessionType sessionType) {
        switch (sessionType) {
            case LESSON: return AdvancedLessonSettings.getOrderPriority();
            case REVIEW: return AdvancedReviewSettings.getOrderPriority();
            case SELF_STUDY:
            default: return AdvancedSelfStudySettings.getOrderPriority();
        }
    }

    /**
     * Get the combined subject selection settings for a specific session type.
     */
    public static SubjectSelectionRules getSubjectSelectionRules(final SessionType type) {
        return new SubjectSelectionRules(
                getSubjectsCurrentMin(type), getSubjectsCurrentMax(type),
                getSubjectsEarlierMin(type), getSubjectsEarlierMax(type),
                getSubjectsRadicalMin(type), getSubjectsRadicalMax(type),
                getSubjectsKanjiMin(type), getSubjectsKanjiMax(type),
                getSubjectsVocabularyMin(type), getSubjectsVocabularyMax(type),
                getSubjectsKanaVocabularyMin(type), getSubjectsKanaVocabularyMax(type));
    }

    private static int getSubjectsCurrentMin(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsCurrentMin();
            case REVIEW: return AdvancedReviewSettings.getSubjectsCurrentMin();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsCurrentMin();
            default: return -1;
        }
    }

    private static int getSubjectsCurrentMax(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsCurrentMax();
            case REVIEW: return AdvancedReviewSettings.getSubjectsCurrentMax();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsCurrentMax();
            default: return -1;
        }
    }

    private static int getSubjectsEarlierMin(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsEarlierMin();
            case REVIEW: return AdvancedReviewSettings.getSubjectsEarlierMin();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsEarlierMin();
            default: return -1;
        }
    }

    private static int getSubjectsEarlierMax(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsEarlierMax();
            case REVIEW: return AdvancedReviewSettings.getSubjectsEarlierMax();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsEarlierMax();
            default: return -1;
        }
    }

    private static int getSubjectsRadicalMin(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsRadicalMin();
            case REVIEW: return AdvancedReviewSettings.getSubjectsRadicalMin();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsRadicalMin();
            default: return -1;
        }
    }

    private static int getSubjectsRadicalMax(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsRadicalMax();
            case REVIEW: return AdvancedReviewSettings.getSubjectsRadicalMax();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsRadicalMax();
            default: return -1;
        }
    }

    private static int getSubjectsKanjiMin(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsKanjiMin();
            case REVIEW: return AdvancedReviewSettings.getSubjectsKanjiMin();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsKanjiMin();
            default: return -1;
        }
    }

    private static int getSubjectsKanjiMax(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsKanjiMax();
            case REVIEW: return AdvancedReviewSettings.getSubjectsKanjiMax();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsKanjiMax();
            default: return -1;
        }
    }

    private static int getSubjectsVocabularyMin(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsVocabularyMin();
            case REVIEW: return AdvancedReviewSettings.getSubjectsVocabularyMin();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsVocabularyMin();
            default: return -1;
        }
    }

    private static int getSubjectsVocabularyMax(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsVocabularyMax();
            case REVIEW: return AdvancedReviewSettings.getSubjectsVocabularyMax();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsVocabularyMax();
            default: return -1;
        }
    }

    private static int getSubjectsKanaVocabularyMin(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsKanaVocabularyMin();
            case REVIEW: return AdvancedReviewSettings.getSubjectsKanaVocabularyMin();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsKanaVocabularyMin();
            default: return -1;
        }
    }

    private static int getSubjectsKanaVocabularyMax(final SessionType type) {
        switch (type) {
            case LESSON: return AdvancedLessonSettings.getSubjectsKanaVocabularyMax();
            case REVIEW: return AdvancedReviewSettings.getSubjectsKanaVocabularyMax();
            case SELF_STUDY: return AdvancedSelfStudySettings.getSubjectsKanaVocabularyMax();
            default: return -1;
        }
    }

    /**
     * Reset the various UI confirmations and tutorials.
     */
    public static void resetConfirmationsAndTutorials() {
        UiConfirmationsSettings.setUiConfirmAbandonSession(true);
        UiConfirmationsSettings.setUiConfirmWrapupSession(true);
        TutorialsSettings.setKeyboardHelpDismissed(false);
        TutorialsSettings.setBrowseOverviewDismissed(false);
        TutorialsSettings.setSearchResultDismissed(false);
        TutorialsSettings.setStartSelfStudyDismissed(false);
        TutorialsSettings.setSessionLogDismissed(false);
        TutorialsSettings.setNotificationPermissionAsked(false);
    }

    /**
     * Test mode on or off.
     *
     * @return true if test mode is on
     */
    public static boolean getTestMode() {
        return prefs().getBoolean("test_mode", false);
    }

    /**
     * Test mode on or off.
     *
     * @param value true if test mode should be turned on
     */
    public static void setTestMode(final boolean value) {
        try {
            final SharedPreferences.Editor editor = prefs().edit();
            editor.putBoolean("test_mode", value);
            editor.apply();
        } catch (final Exception e) {
            //
        }
    }
}
