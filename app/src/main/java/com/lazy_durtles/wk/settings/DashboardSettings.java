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

import com.lazy_durtles.wk.enums.TimeLineBarChartGridStyle;
import com.lazy_durtles.wk.enums.TimeLineBarChartStyle;
import javax.annotation.Nullable;

import static com.lazy_durtles.wk.settings.SettingsPrefs.prefs;

/**
 * Dashboard settings, extracted from GlobalSettings.
 */
public class DashboardSettings {
    /**
     * Private constructor.
     */
    protected DashboardSettings() {
        //
    }

    /**
     * Show the lesson/review breakdown on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowLessonReviewBreakdown() {
        return prefs().getBoolean("show_lesson_review_breakdown", false);
    }

    /**
     * Show the timeline bar chart on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowTimeLine() {
        return prefs().getBoolean("show_timeline", true);
    }

    /**
     * The style of the bars in the timeline bar chart.
     *
     * @return the value
     */
    public static TimeLineBarChartStyle getTimeLineChartStyle() {
        final @Nullable String value = prefs().getString("timeline_chart_style", null);
        if (value != null) {
            try {
                return TimeLineBarChartStyle.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return TimeLineBarChartStyle.SRS_STAGE;
    }

    /**
     * Show the waterfall line for cumulative review count.
     *
     * @return the value
     */
    public static boolean getShowWaterfallLine() {
        return prefs().getBoolean("show_waterfall_line", false);
    }

    /**
     * The style of the Y-axis grid in the timeline bar chart.
     *
     * @return the value
     */
    public static TimeLineBarChartGridStyle getTimeLineChartGridStyle() {
        final @Nullable String value = prefs().getString("timeline_chart_grid_style", null);
        if (value != null) {
            try {
                return TimeLineBarChartGridStyle.valueOf(value);
            }
            catch (final Exception e) {
                //
            }
        }
        return TimeLineBarChartGridStyle.FOR_BARS;
    }

    /**
     * The size of the timeline bar chart.
     *
     * @return the value in hours
     */
    public static int getTimeLineChartSize() {
        final String value = prefs().getString("timeline_chart_size", "24");
        try {
            return Integer.parseInt(value, 10);
        }
        catch (final Exception e) {
            return 24;
        }
    }

    /**
     * The size of the shown part of the timeline bar chart.
     *
     * @return the value in hours
     */
    public static int getTimeLineChartSizeShown() {
        final String value = prefs().getString("timeline_chart_size_shown", "24");
        try {
            return Math.min(Integer.parseInt(value, 10), getTimeLineChartSize());
        }
        catch (final Exception e) {
            return 24;
        }
    }

    /**
     * Show the SRS breakdown on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowSrsBreakdown() {
        return prefs().getBoolean("show_srs_breakdown", true);
    }

    /**
     * Show the level progression bars on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowLevelProgression() {
        return prefs().getBoolean("show_level_progression", true);
    }

    /**
     * Show the level progression bars on the dashboard for levels beyond the user's level.
     *
     * @return the value
     */
    public static boolean getShowOverLevelProgression() {
        return prefs().getBoolean("show_over_level_progression", false);
    }

    /**
     * Limit the level progression bars on the dashboard for previously complete levels.
     *
     * @return the value
     */
    public static boolean getLimitPreviousLevelProgression() {
        return prefs().getBoolean("previous_level_progression_limit", false);
    }

    /**
     * Limit the amount of levels previous to show progression bars for on the dashboard.
     *
     * @return the value
     */
    public static int getPreviousLevelProgressionLimitedAmount() {
        try {
            return Integer.parseInt(prefs().getString("previous_level_progression_limit_amount", "1"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Show the post-60 progression bar on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowPost60Progression() {
        return prefs().getBoolean("show_post60_progression", false);
    }

    /**
     * Show apprentice and guru subsections in the post-60 progression bar.
     *
     * @return the value
     */
    public static boolean getShowPost60Subsections() {
        return prefs().getBoolean("show_post60_subsections", false);
    }

    /**
     * Show the post-60 progression bar reversed.
     *
     * @return the value
     */
    public static boolean getShowPost60Reverse() {
        return prefs().getBoolean("show_post60_reverse", false);
    }

    /**
     * Show the Joyo progress on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowJoyoProgress() {
        return prefs().getBoolean("show_joyo_progress", false);
    }

    /**
     * Show the JLPT progress on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowJlptProgress() {
        return prefs().getBoolean("show_jlpt_progress", false);
    }

    /**
     * Show the recently unlocked items on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowRecentUnlocks() {
        return prefs().getBoolean("show_recent_unlocks", true);
    }

    /**
     * Show the critical condition items on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowCriticalCondition() {
        return prefs().getBoolean("show_critical_condition", true);
    }

    /**
     * Show the recently burned items on the dashboard.
     *
     * @return the value
     */
    public static boolean getShowBurnedItems() {
        return prefs().getBoolean("show_burned_items", true);
    }
}
