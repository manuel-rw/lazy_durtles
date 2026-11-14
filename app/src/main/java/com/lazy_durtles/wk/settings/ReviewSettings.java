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

import com.lazy_durtles.wk.enums.CloseEnoughAction;
import javax.annotation.Nullable;
import static com.lazy_durtles.wk.util.ObjectSupport.isEmpty;


import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * Review settings, extracted from GlobalSettings.
 */
public class ReviewSettings {
    /**
     * Private constructor.
     */
    protected ReviewSettings() {
        //
    }

    /**
     * Maximum size of a lesson session.
     *
     * @return the value
     */
    public static int gexMaxLessonSessionSize() {
        try {
            final @Nullable String value = prefs().getString("max_lesson_session_size", null);
            if (isEmpty(value)) {
                return 5;
            }
            final int n = Integer.parseInt(value);
            if (n < 1) {
                return 5;
            }
            return n;
        }
        catch (final Exception e) {
            return 5;
        }
    }

    /**
     * Maximum size of a review session.
     *
     * @return the value
     */
    public static int gexMaxReviewSessionSize() {
        try {
            final @Nullable String value = prefs().getString("max_review_session_size", null);
            if (isEmpty(value)) {
                return 100;
            }
            final int n = Integer.parseInt(value);
            if (n < 1) {
                return 100;
            }
            return n;
        }
        catch (final Exception e) {
            return 100;
        }
    }

    /**
     * Maximum size of a review session.
     *
     * @return the value
     */
    public static int getMaxSelfStudySessionSize() {
        try {
            final @Nullable String value = prefs().getString("max_self_study_session_size", null);
            if (isEmpty(value)) {
                return 100;
            }
            final int n = Integer.parseInt(value);
            if (n < 1) {
                return 100;
            }
            return n;
        }
        catch (final Exception e) {
            return 100;
        }
    }

    /**
     * Delay processing the results of a review session until after the session is finished.
     *
     * @return the value
     */
    public static boolean getDelayResultUpload() {
        return prefs().getBoolean("delay_result_upload", false);
    }

    /**
     * Lightning mode, i.e. auto-advance to the next question on a correct answer.
     *
     * @return the value
     */
    public static boolean getEnableLightningMode() {
        return prefs().getBoolean("enable_lightning_mode", true);
    }

    /**
     * Show a brief check mark / X animation for a correct / incorrect answer.
     *
     * @return the value
     */
    public static boolean getShowAnswerToast() {
        return prefs().getBoolean("show_answer_toast", true);
    }

    /**
     * Show the current SRS stage of an item in a quiz.
     *
     * @return the value
     */
    public static boolean getEnableSrsIndicator() {
        return prefs().getBoolean("enable_srs_indicator", false);
    }

    /**
     * Show a toast when an item changes in SRS stage.
     *
     * @return the value
     */
    public static boolean getEnableSrsToast() {
        return prefs().getBoolean("enable_srs_toast", true);
    }
    /**
     * Show a toast when an item has alternative answers
     *
     * @return the value
     */
    public static boolean getEnableAlternativesToast() {
        return prefs().getBoolean("enable_alternatives_toast", false);
    }
    /**
     * Action to perform when a meaning answer is not quite correct but acceptable within the typo lenience margin.
     *
     * @return the value
     */
    public static boolean enable_haptic_feedback_success() {
        return prefs().getBoolean("enable_haptic_feedback_success", false);
    }

    public static boolean enable_haptic_feedback_failure() {
        return prefs().getBoolean("enable_haptic_feedback_failure", false);
    }

    public static CloseEnoughAction getCloseEnoughAction() {
        final @Nullable String value = prefs().getString("close_enough_action", null);
        if (value != null) {
            try {
                return CloseEnoughAction.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return CloseEnoughAction.SILENTLY_ACCEPT;
    }
}
