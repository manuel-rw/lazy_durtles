package com.lazy_durtles.wk.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.lazy_durtles.wk.BuildConfig
import com.lazy_durtles.wk.Constants
import com.lazy_durtles.wk.GlobalSettings
import com.lazy_durtles.wk.Identification
import com.lazy_durtles.wk.R
import com.lazy_durtles.wk.api.ApiState
import com.lazy_durtles.wk.jobs.FlushTasksJob
import com.lazy_durtles.wk.jobs.ResetDatabaseJob
import com.lazy_durtles.wk.livedata.LiveApiState
import com.lazy_durtles.wk.services.JobRunnerService
import com.lazy_durtles.wk.ui.icons.MaterialSymbolIcon
import com.lazy_durtles.wk.ui.icons.MaterialSymbolsIcons
import com.lazy_durtles.wk.ui.screens.settings.DataImportExportScreen
import com.lazy_durtles.wk.ui.screens.settings.DownloadAudioScreen
import com.lazy_durtles.wk.ui.screens.settings.FontImportScreen
import com.lazy_durtles.wk.ui.screens.settings.FontSelectionScreen
import com.lazy_durtles.wk.ui.screens.settings.HtmlNoticeBanner
import com.lazy_durtles.wk.ui.screens.settings.HtmlNoticeText
import com.lazy_durtles.wk.ui.screens.settings.KeyboardHelpScreen
import com.lazy_durtles.wk.ui.screens.settings.SettingHighlightShape
import com.lazy_durtles.wk.ui.screens.settings.SettingsAlertDialog
import com.lazy_durtles.wk.ui.screens.settings.SettingsDialogShape
import com.lazy_durtles.wk.ui.screens.settings.SettingsGroupCard
import com.lazy_durtles.wk.ui.screens.settings.SettingsGroupHeader
import com.lazy_durtles.wk.ui.screens.settings.SettingsRowDivider
import com.lazy_durtles.wk.ui.screens.settings.SettingsSelectDialog
import com.lazy_durtles.wk.ui.screens.settings.ThemeCustomizationScreen
import com.lazy_durtles.wk.ui.screens.settings.TonalIcon
import com.lazy_durtles.wk.ui.screens.settings.highlightContainerColor
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.delay
import java.util.Locale

// Category definitions.
//

/**
 * Login to WaniKani: API token, account extras and sync behavior.
 */
internal fun apiSettingsGroups(apiStateOk: () -> Boolean): List<SettingGroup> {
    val showPermissionNotice: (SharedPreferences) -> Boolean = { !apiStateOk() }
    return listOf(
        SettingGroup(
            null,
            listOf(
                InfoDef(
                    key = "api_permission_notice",
                    title = "Caution",
                    html = Constants.API_KEY_PERMISSION_NOTICE,
                    icon = MaterialSymbolsIcons.Warning,
                    visibleIf = showPermissionNotice,
                ),
            ),
        ),
        group(
            "WaniKani account",
            TextFieldDef(
                key = "api_key", title = "V2 API token",
                prefKey = "api_key",
                read = { GlobalSettings.Api.getApiKey() ?: "" },
                write = { value, _ ->
                    GlobalSettings.Api.setApiKey(value)
                    // Refresh the auth state so the caution notice and the
                    // category title follow the new token immediately.
                    LiveApiState.getInstance().update()
                },
            ),
            TextFieldDef(
                key = "web_password", title = "Web site password",
                summary = "This is only needed for resurrect functionality. Leave blank if you don't need it.",
                prefKey = "web_password",
                read = { GlobalSettings.Api.getWebPassword() ?: "" },
                write = { value, _ -> GlobalSettings.Api.setWebPassword(value) },
            ),
        ),
        group(
            "Sync",
            SelectDef(
                key = "network_rule", title = "Network access rule",
                options = NETWORK_RULE_OPTIONS,
                prefKey = "network_rule", default = "DOWNLOAD_WIFI_ONLY",
            ),
            SwitchDef(
                key = "enable_background_sync", title = "Allow background sync",
                summary = "Caution - this may drain your battery faster than normal",
                prefKey = "enable_background_sync",
            ),
            SwitchDef(
                key = "sync_on_open", title = "Sync when the app is opened",
                summary = "Triggers sync if the app has been closed for at least 3 min",
                prefKey = "sync_on_open",
            ),
        ),
    )
}

/**
 * Audio: playback behavior and audio file storage.
 */
internal fun audioSettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> = listOf(
    group(
        "Playback",
        SwitchDef(
            key = "autoplay_lesson_presentation",
            title = "Autoplay audio during lesson presentation",
            prefKey = "autoplay_lesson_presentation",
        ),
        SwitchDef(
            key = "autoplay_lessons", title = "Autoplay audio during lessons",
            summary = "When answering a question correctly in a lesson session",
            prefKey = "autoplay_lessons",
        ),
        SwitchDef(
            key = "autoplay_reviews", title = "Autoplay audio during reviews",
            summary = "When answering a question correctly in a review/self-study session",
            prefKey = "autoplay_reviews",
        ),
        SwitchDef(
            key = "autoplay_anki_reveal", title = "Autoplay audio when revealing in Anki mode",
            summary = "When clicking the 'Show answer' button in Anki mode",
            prefKey = "autoplay_anki_reveal",
        ),
        SelectDef(
            key = "voice_preference", title = "Voice preference",
            options = VOICE_OPTIONS,
            prefKey = "voice_preference", default = "RANDOM",
        ),
        SwitchDef(
            key = "use_audio_focus", title = "Request audio focus when playing vocab audio",
            summary = "Asks other player apps to pause or lower their volume when playing vocab audio",
            prefKey = "use_audio_focus",
        ),
    ),
    group(
        "Audio files",
        SelectDef(
            key = "audio_location", title = "Audio files location",
            options = audioLocationOptions(),
            prefKey = "audio_location", default = "Internal",
        ),
        SwitchDef(
            key = "auto_download_audio", title = "Auto-download audio for upcoming reviews",
            prefKey = "auto_download_audio",
        ),
        ActionDef(
            key = "download_audio_action", title = "Download audio files",
            summary = "Download audio for offline use, per level",
            navigates = true,
            onClick = { onOpenCategory("download_audio", null) },
        ),
    ),
)

/**
 * The available audio storage locations: internal storage plus the external
 * files dirs, with the package-specific part stripped for display.
 */
internal fun audioLocationOptions(): List<Pair<String, String>> {
    val values = com.lazy_durtles.wk.util.AudioUtil.getLocationValues()
    val labels = com.lazy_durtles.wk.util.AudioUtil.getLocations(values)
    return values.mapIndexed { index, value -> labels[index] to value }
}

/**
 * Display: themes and quiz screen appearance.
 */
internal fun displaySettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> = listOf(
    group(
        "Themes",
        SelectDef(
            key = "theme", title = "Light theme",
            options = THEME_OPTIONS,
            prefKey = "theme", default = "LIGHT",
            summarySuffix = " theme is currently selected",
        ),
        SelectDef(
            key = "nightTheme", title = "Dark theme",
            options = THEME_OPTIONS,
            prefKey = "nightTheme", default = "DARK",
            summarySuffix = " is used in system wide dark mode",
        ),
        ActionDef(
            key = "theme_customization", title = "Theme customisation",
            summary = "Customise colors in the current theme",
            navigates = true,
            onClick = { onOpenCategory("theme_customization", null) },
        ),
    ),
    group(
        "Quiz screen",
        SwitchDef(
            key = "center_caret", title = "Center caret in quiz answer box",
            prefKey = "center_caret",
        ),
        SliderDef(
            key = "anki_buttons_height", title = "Vertical size of the Anki mode buttons",
            range = 10f..250f,
            read = { it.getInt("anki_buttons_height", 50) },
            write = { v, p -> setPrefInt(v, "anki_buttons_height", p) },
        ),
        SliderDef(
            key = "quiz_question_view_height", title = "Fix height of main quiz question view",
            range = 0f..500f,
            summary = "Set to 0 to not force a fixed size",
            read = { it.getInt("quiz_question_view_height", 0) },
            write = { v, p -> setPrefInt(v, "quiz_question_view_height", p) },
            valueText = { if (it == 0) "Auto" else "$it" },
        ),
        SwitchDef(
            key = "swap_anki_buttons", title = "Swap the correct/incorrect Anki mode buttons",
            prefKey = "swap_anki_buttons",
        ),
        SwitchDef(
            key = "slide_animations",
            title = "Use slide animations to transition between quiz questions",
            prefKey = "slide_animations", default = true,
        ),
        SwitchDef(
            key = "stretch_quiz_question_view",
            title = "Stretch the question view to fill the screen when keyboard input is active",
            prefKey = "stretch_quiz_question_view",
        ),
    ),
)

/**
 * Dashboard: what to show on the dashboard.
 */
internal fun dashboardSettingsGroups(): List<SettingGroup> = listOf(
    group(
        "Overview",
        SwitchDef(
            key = "show_lesson_review_breakdown", title = "Show lesson/review breakdown",
            prefKey = "show_lesson_review_breakdown",
        ),
    ),
    group(
        "Review timeline",
        SwitchDef(
            key = "show_timeline", title = "Show 24h review timeline",
            prefKey = "show_timeline", default = true,
        ),
        SelectDef(
            key = "timeline_chart_style", title = "Timeline chart style",
            options = listOf(
                Pair("SRS Stage", "SRS_STAGE"),
                Pair("Next SRS Stage", "NEXT_SRS_STAGE"),
                Pair("Item type", "ITEM_TYPE"),
            ),
            prefKey = "timeline_chart_style", default = "SRS_STAGE",
        ),
        SwitchDef(
            key = "show_waterfall_line", title = "Show timeline waterfall line",
            summary = "Show cumulative amount of reviews over time in the timeline chart",
            prefKey = "show_waterfall_line",
        ),
        SelectDef(
            key = "timeline_chart_grid_style", title = "Timeline chart Y-axis grid style",
            options = listOf(
                Pair("Off", "OFF"),
                Pair("Scaled for bars", "FOR_BARS"),
                Pair("Scaled for cumulative review count", "FOR_WATERFALL"),
            ),
            prefKey = "timeline_chart_grid_style", default = "FOR_BARS",
        ),
        SelectDef(
            key = "timeline_chart_size", title = "Timeline chart size",
            options = CHART_SIZE_OPTIONS,
            prefKey = "timeline_chart_size", default = "24",
        ),
        SelectDef(
            key = "timeline_chart_size_shown", title = "Part of timeline chart shown at once",
            options = CHART_SIZE_OPTIONS,
            prefKey = "timeline_chart_size_shown", default = "24",
        ),
    ),
    group(
        "SRS and level progression",
        SwitchDef(
            key = "show_srs_breakdown", title = "Show SRS breakdown",
            prefKey = "show_srs_breakdown", default = true,
        ),
        SwitchDef(
            key = "show_level_progression", title = "Show level progression bars",
            prefKey = "show_level_progression", default = true,
        ),
        SwitchDef(
            key = "show_over_level_progression",
            title = "Show level progression bars for levels higher than your own",
            summary = "This can happen if you unlocked subjects in the past that have since been moved above your level.",
            prefKey = "show_over_level_progression",
        ),
        SwitchDef(
            key = "previous_level_progression_limit",
            title = "Limit progression bars for the completed levels to only the most recent levels",
            summary = "This can help if many new items were added to previous levels and you now have all those levels on your dashboard.",
            prefKey = "previous_level_progression_limit",
        ),
        SliderDef(
            key = "previous_level_progression_limit_amount",
            title = "Number of previous levels to show progression bars for",
            range = 0f..60f,
            read = { it.getString("previous_level_progression_limit_amount", "1")?.toIntOrNull() ?: 1 },
            write = { v, p -> setPrefString(v.toString(), "previous_level_progression_limit_amount", p) },
            valueText = { if (it == 0) "All" else "$it" },
            visibleIf = { it.getBoolean("previous_level_progression_limit", false) },
        ),
        SwitchDef(
            key = "show_post60_progression", title = "Show post-60 progression bar",
            summary = "Can be used at any level, but intended for level 60! Does NOT include a legend, " +
                "so only enable this if you're cool with that.",
            prefKey = "show_post60_progression",
        ),
        SwitchDef(
            key = "show_post60_subsections", title = "Show subsections in post-60 progression bar",
            summary = "Show separate bar segments for the different Apprentice and Guru levels",
            prefKey = "show_post60_subsections",
            visibleIf = { it.getBoolean("show_post60_progression", false) },
        ),
        SwitchDef(
            key = "show_post60_reverse", title = "Reverse post-60 progression bar",
            prefKey = "show_post60_reverse",
            visibleIf = { it.getBoolean("show_post60_progression", false) },
        ),
    ),
    group(
        "Subject tables",
        SwitchDef(
            key = "show_joyo_progress", title = "Show Jōyō progress",
            prefKey = "show_joyo_progress",
        ),
        SwitchDef(
            key = "show_jlpt_progress", title = "Show JLPT progress",
            prefKey = "show_jlpt_progress",
        ),
        SwitchDef(
            key = "show_recent_unlocks", title = "Show recently unlocked items",
            prefKey = "show_recent_unlocks", default = true,
        ),
        SwitchDef(
            key = "show_critical_condition", title = "Show critical condition items",
            prefKey = "show_critical_condition", default = true,
        ),
        SwitchDef(
            key = "show_burned_items", title = "Show recently burned items",
            prefKey = "show_burned_items", default = true,
        ),
    ),
)

/**
 * Lessons and reviews: session size, question behavior, feedback, and the
 * advanced sub-settings, which live here instead of in a separate top-level
 * category.
 */
internal fun lessonReviewSettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> {
    val advancedEnabled: (SharedPreferences) -> Boolean = { it.getBoolean("enable_advanced", false) }
    return listOf(
        group(
            "Session size",
            TextFieldDef(
                key = "max_lesson_session_size", title = "Max. # of lessons per session",
                prefKey = "max_lesson_session_size", default = "5",
                keyboardType = KeyboardType.Number,
            ),
            TextFieldDef(
                key = "max_review_session_size", title = "Max. # of reviews per session",
                prefKey = "max_review_session_size", default = "100",
                keyboardType = KeyboardType.Number,
            ),
            TextFieldDef(
                key = "max_self_study_session_size", title = "Max. # of self-study items per session",
                prefKey = "max_self_study_session_size", default = "100",
                keyboardType = KeyboardType.Number,
            ),
        ),
        group(
            "Questions",
            SwitchDef(
                key = "enable_lightning_mode", title = "Enable lightning mode",
                summary = "Auto-advance to next question on a correct answer",
                prefKey = "enable_lightning_mode", default = true,
            ),
            SwitchDef(
                key = "delay_result_upload", title = "Delay processing quiz results",
                summary = "Doesn't process results until the session is finished. Allows unlimited undo during sessions.",
                prefKey = "delay_result_upload",
            ),
            SwitchDef(
                key = "show_answer_toast",
                title = "Enable a check mark / X animation for a correct / incorrect answer",
                prefKey = "show_answer_toast", default = true,
            ),
            SwitchDef(
                key = "require_onyomi_in_katakana", title = "Require on'yomi answers in Katakana",
                prefKey = "require_onyomi_in_katakana",
            ),
            SelectDef(
                key = "close_enough_action", title = "Action for \"close enough\" answer",
                options = CLOSE_ENOUGH_OPTIONS,
                prefKey = "close_enough_action", default = "SILENTLY_ACCEPT",
            ),
        ),
        group(
            "Feedback",
            SwitchDef(
                key = "enable_srs_indicator", title = "Enable SRS indicator",
                summary = "Show current SRS stage during reviews",
                prefKey = "enable_srs_indicator",
            ),
            SwitchDef(
                key = "enable_srs_toast", title = "Enable SRS up/downgrade toast",
                summary = "Show a brief notification when an item goes up or down in SRS stage during reviews",
                prefKey = "enable_srs_toast", default = true,
            ),
            SwitchDef(
                key = "enable_alternatives_toast", title = "Enable alternative readings/meanings toast",
                summary = "Show a toast with alternative readings or meanings upon correct answer",
                prefKey = "enable_alternatives_toast",
            ),
            SwitchDef(
                key = "enable_haptic_feedback_success", title = "Enable haptic feedback on correct answer",
                summary = "The device will give a short buzz to indicate a correct answer",
                prefKey = "enable_haptic_feedback_success",
            ),
            SwitchDef(
                key = "enable_haptic_feedback_failure", title = "Enable haptic feedback on incorrect answer",
                summary = "The device will give a long buzz to indicate an incorrect answer",
                prefKey = "enable_haptic_feedback_failure",
            ),
        ),
        group(
            "Advanced",
            SwitchDef(
                key = "enable_advanced", title = "Enable advanced settings",
                summary = "Only for experienced users!",
                prefKey = "enable_advanced",
                confirmTitle = "Enable advanced settings?",
                confirmHtml = Constants.ENABLE_ADVANCED_WARNING,
            ),
            ActionDef(
                key = "advanced_lesson_settings", title = "Lesson advanced settings",
                summary = "Ordering, Anki mode and subject selection for lessons",
                navigates = true,
                onClick = { onOpenCategory("advanced_lesson_settings", null) },
                visibleIf = advancedEnabled,
            ),
            ActionDef(
                key = "advanced_review_settings", title = "Review advanced settings",
                summary = "Ordering, Anki mode and subject selection for reviews",
                navigates = true,
                onClick = { onOpenCategory("advanced_review_settings", null) },
                visibleIf = advancedEnabled,
            ),
            ActionDef(
                key = "advanced_self_study_settings", title = "Self-study advanced settings",
                summary = "Ordering, Anki mode and subject selection for self-study",
                navigates = true,
                onClick = { onOpenCategory("advanced_self_study_settings", null) },
                visibleIf = advancedEnabled,
            ),
            ActionDef(
                key = "advanced_other_settings", title = "Other advanced settings",
                summary = "Buttons, overdue threshold and session log",
                navigates = true,
                onClick = { onOpenCategory("advanced_other_settings", null) },
                visibleIf = advancedEnabled,
            ),
        ),
    )
}

/**
 * Subject info: mnemonics, subject details, info dump and stroke order.
 */
internal fun subjectInfoSettingsGroups(): List<SettingGroup> {
    val wkMnemonicsVisible: (SharedPreferences) -> Boolean = {
        !it.getBoolean("always_hide_wk_mnemonic", false)
    }
    val strokeOrderVisible: (SharedPreferences) -> Boolean = {
        it.getBoolean("show_stroke_order", false)
    }
    return listOf(
        group(
            "Lessons",
            SwitchDef(
                key = "hide_lesson_readings", title = "Hide readings in lessons until tapped",
                prefKey = "hide_lesson_readings",
            ),
        ),
        group(
            "Mnemonics",
            SwitchDef(
                key = "highlight_subject_tags",
                title = "Highlight subject tags in mnemonic texts with colours",
                summary = "This replaces tags such as <kanji> with appropriate colouring instead of just making the text bold",
                prefKey = "highlight_subject_tags", default = true,
            ),
            SwitchDef(
                key = "always_hide_wk_mnemonic", title = "Always hide WaniKani's own mnemonics",
                prefKey = "always_hide_wk_mnemonic",
            ),
            SwitchDef(
                key = "hide_meaning_mnemonic",
                title = "Hide WaniKani's meaning mnemonic if a user note is present",
                prefKey = "hide_meaning_mnemonic",
                visibleIf = wkMnemonicsVisible,
            ),
            SwitchDef(
                key = "hide_reading_mnemonic",
                title = "Hide WaniKani's reading mnemonic if a user note is present",
                prefKey = "hide_reading_mnemonic",
                visibleIf = wkMnemonicsVisible,
            ),
            SwitchDef(
                key = "show_legacy",
                title = "Show old names and mnemonics for radicals from before the December 2018 content update",
                prefKey = "show_legacy",
            ),
        ),
        group(
            "Subject details",
            SwitchDef(
                key = "hide_sentence_translations",
                title = "Hide context sentence translations until tapped",
                prefKey = "hide_sentence_translations", default = true,
            ),
            SwitchDef(
                key = "show_pitch_info", title = "Show pitch information on vocabulary",
                prefKey = "show_pitch_info",
            ),
            SwitchDef(
                key = "swap_similar_amalgamations",
                title = "Swap 'visually similar' and 'used in' in subject info dump",
                prefKey = "swap_similar_amalgamations",
            ),
            SwitchDef(
                key = "show_onyomi_in_katakana", title = "Show on'yomi readings in Katakana",
                prefKey = "show_onyomi_in_katakana",
            ),
        ),
        group(
            "Info dump",
            SelectDef(
                key = "meaning_info_dump", title = "Show info dump after a correct meaning answer",
                options = INFO_DUMP_OPTIONS,
                prefKey = "meaning_info_dump", default = "ANSWERS_ONLY",
            ),
            SelectDef(
                key = "reading_info_dump", title = "Show info dump after a correct reading answer",
                options = INFO_DUMP_OPTIONS,
                prefKey = "reading_info_dump", default = "ANSWERS_ONLY",
            ),
            SelectDef(
                key = "meaning_info_dump_incorrect",
                title = "Show info dump after an incorrect meaning answer",
                options = INFO_DUMP_OPTIONS,
                prefKey = "meaning_info_dump_incorrect", default = "ANSWERS_ONLY",
            ),
            SelectDef(
                key = "reading_info_dump_incorrect",
                title = "Show info dump after an incorrect reading answer",
                options = INFO_DUMP_OPTIONS,
                prefKey = "reading_info_dump_incorrect", default = "ANSWERS_ONLY",
            ),
        ),
        group(
            "Stroke order",
            SwitchDef(
                key = "show_stroke_order", title = "Show stroke order diagrams on radicals and kanji",
                prefKey = "show_stroke_order",
            ),
            SwitchDef(
                key = "stroke_order_animated", title = "Animate the stroke order diagrams",
                summary = "Tap the diagram to toggle animation",
                prefKey = "stroke_order_animated", default = true,
                visibleIf = strokeOrderVisible,
            ),
            SliderDef(
                key = "stroke_order_size", title = "Size of the stroke order diagrams",
                range = 20f..500f,
                read = { it.getInt("stroke_order_size", 250) },
                write = { v, p -> setPrefInt(v, "stroke_order_size", p) },
                visibleIf = strokeOrderVisible,
            ),
        ),
    )
}

/**
 * Keyboard input: auto-correct, keyboard layout and answer buttons.
 */
internal fun keyboardSettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> {
    val api26Plus: (SharedPreferences) -> Boolean = {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }
    return listOf(
        SettingGroup(
            null,
            listOf(
                ActionDef(
                    key = "keyboard_help", title = "Keyboard help",
                    summary = "Tap here to read some hints for keyboard settings",
                    navigates = true,
                    onClick = { onOpenCategory("keyboard_help", null) },
                ),
            ),
        ),
        group(
            "Auto-correct and suggestions",
            SwitchDef(
                key = "enable_auto_correct_meaning", title = "Allow keyboard auto-correct for meanings",
                prefKey = "enable_auto_correct_meaning",
            ),
            SwitchDef(
                key = "enable_auto_correct_reading", title = "Allow keyboard auto-correct for readings",
                prefKey = "enable_auto_correct_reading",
            ),
            SwitchDef(
                key = "force_visible_password_meaning",
                title = "Force 'visible password' setting for meanings",
                summary = "This may be necessary to prevent auto-suggest on some keyboards",
                prefKey = "force_visible_password_meaning",
            ),
            SwitchDef(
                key = "force_visible_password_reading",
                title = "Force 'visible password' setting for readings",
                summary = "This may be necessary to prevent auto-suggest on some keyboards",
                prefKey = "force_visible_password_reading",
            ),
            SwitchDef(
                key = "enable_no_learning", title = "Enable 'no personalized learning' option",
                summary = "Disable this to get rid of the annoying incognito icon in Swiftkey",
                prefKey = "enable_no_learning", default = true,
            ),
        ),
        group(
            "Keyboard layout",
            SwitchDef(
                key = "force_ascii_meaning", title = "Force ASCII keyboard for meanings",
                prefKey = "force_ascii_meaning", default = true,
            ),
            SwitchDef(
                key = "force_ascii_reading", title = "Force ASCII keyboard for readings",
                prefKey = "force_ascii_reading",
            ),
            SwitchDef(
                key = "ime_hint_meaning",
                title = "Set keyboard language hint to English for meanings",
                summary = "Enable this to suggest a keyboard language for meaning questions",
                prefKey = "ime_hint_meaning",
                visibleIf = api26Plus,
            ),
            SwitchDef(
                key = "ime_hint_reading",
                title = "Set keyboard language hint to Japanese for readings",
                summary = "Enable this to suggest a keyboard language for reading questions",
                prefKey = "ime_hint_reading",
                visibleIf = api26Plus,
            ),
        ),
        group(
            "Answer buttons",
            TextFieldDef(
                key = "next_button_delay",
                title = "Delay in seconds to disable the next/submit button after typing an incorrect answer",
                prefKey = "next_button_delay", default = "0",
                keyboardType = KeyboardType.Decimal,
            ),
        ),
    )
}

/**
 * Fonts: font selection and text sizes, nested into sub-groups.
 */
internal fun fontSettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> = listOf(
    group(
        "Fonts for questions",
        ActionDef(
            key = "font_selection", title = "Select fonts for quiz questions",
            summary = "Choose which fonts are used to render question text",
            navigates = true,
            onClick = { onOpenCategory("font_selection", null) },
        ),
        ActionDef(
            key = "font_import", title = "Import fonts for quiz questions",
            summary = "Add your own TTF font files",
            navigates = true,
            onClick = { onOpenCategory("font_import", null) },
        ),
    ),
    group(
        "Subject title size",
        SliderDef(
            key = "max_font_size_lesson",
            title = "Max font size of subject title in lesson presentation",
            range = 0f..128f,
            summary = "Set to 0 to hide the title",
            read = { it.getInt("max_font_size_lesson", 128) },
            write = { v, p -> setPrefInt(v, "max_font_size_lesson", p) },
            valueText = { if (it == 0) "Hidden" else "$it" },
        ),
        SliderDef(
            key = "max_font_size_quiz",
            title = "Max font size of subject title on the quiz screen",
            range = 0f..128f,
            summary = "Set to 0 to hide the title",
            read = { it.getInt("max_font_size_quiz", 28) },
            write = { v, p -> setPrefInt(v, "max_font_size_quiz", p) },
            valueText = { if (it == 0) "Hidden" else "$it" },
        ),
        SliderDef(
            key = "max_font_size_browse",
            title = "Max font size of subject title on the browse screen",
            range = 0f..128f,
            summary = "Set to 0 to hide the title",
            read = { it.getInt("max_font_size_browse", 64) },
            write = { v, p -> setPrefInt(v, "max_font_size_browse", p) },
            valueText = { if (it == 0) "Hidden" else "$it" },
        ),
    ),
    group(
        "Text size",
        SliderDef(
            key = "max_font_size_quiz_text",
            title = "Max font size of main quiz question text",
            range = 0f..500f,
            summary = "This is the large question text in the center of the quiz screen",
            read = { it.getInt("max_font_size_quiz_text", 100) },
            write = { v, p -> setPrefInt(v, "max_font_size_quiz_text", p) },
        ),
        SliderDef(
            key = "font_size_question_edit", title = "Font size of answer input text box",
            range = 10f..32f,
            read = { it.getInt("font_size_question_edit", 14) },
            write = { v, p -> setPrefInt(v, "font_size_question_edit", p) },
        ),
        SliderDef(
            key = "font_size_subject_info", title = "Font size of subject info dump",
            range = 10f..32f,
            read = { it.getInt("font_size_subject_info", 14) },
            write = { v, p -> setPrefInt(v, "font_size_subject_info", p) },
        ),
        SliderDef(
            key = "font_size_live_subject_table",
            title = "Font size of dashboard subject tables",
            range = 10f..32f,
            summary = "Critical condition items, recent unlocks, and recently burned",
            read = { it.getInt("font_size_live_subject_table", 14) },
            write = { v, p -> setPrefInt(v, "font_size_live_subject_table", p) },
        ),
        SliderDef(
            key = "font_size_anki_answer", title = "Font size of Anki mode answers",
            range = 10f..48f,
            read = { it.getInt("font_size_anki_answer", 18) },
            write = { v, p -> setPrefInt(v, "font_size_anki_answer", p) },
        ),
    ),
)

/**
 * Notifications and more: notifications, search engines and misc.
 */
internal fun otherSettingsGroups(context: Context): List<SettingGroup> {
    val showPermissionAction = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    return listOf(
        SettingGroup(
            null,
            listOf(
                ActionDef(
                    key = "notification_permission", title = "Grant notification permission",
                    summary = "Open system settings to allow this app to send notifications",
                    visibleIf = { showPermissionAction },
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        context.startActivity(intent)
                    },
                ),
            ),
        ),
        group(
            "Notifications",
            SwitchDef(
                key = "enable_notifications", title = "Enable notifications for new reviews",
                prefKey = "enable_notifications", default = true,
            ),
            SelectDef(
                key = "notification_update_frequency", title = "Notification update frequency",
                options = listOf(
                    Pair("Only for new reviews", "ONLY_NEW_REVIEWS"),
                    Pair("Once per hour", "ONCE_PER_HOUR"),
                    Pair("Continuously", "CONTINUOUSLY"),
                ),
                prefKey = "notification_update_frequency", default = "ONLY_NEW_REVIEWS",
            ),
            SelectDef(
                key = "notification_priority", title = "Notification priority",
                options = listOf(
                    Pair("Minimum", "MIN"),
                    Pair("Low", "LOW"),
                    Pair("Default", "DEFAULT"),
                    Pair("High", "HIGH"),
                    Pair("Maximum", "MAX"),
                ),
                prefKey = "notification_priority", default = "DEFAULT",
            ),
            SelectDef(
                key = "notification_category", title = "Notification category",
                options = listOf(
                    Pair("Incoming call", "CALL"),
                    Pair("Navigation", "NAVIGATION"),
                    Pair("Incoming message", "MESSAGE"),
                    Pair("Email", "EMAIL"),
                    Pair("Calendar event", "EVENT"),
                    Pair("Promotion / Ad", "PROMO"),
                    Pair("Alarm", "ALARM"),
                    Pair("Progress", "PROGRESS"),
                    Pair("Social media", "SOCIAL"),
                    Pair("Error", "ERROR"),
                    Pair("Media transport", "TRANSPORT"),
                    Pair("System", "SYSTEM"),
                    Pair("Service", "SERVICE"),
                    Pair("Reminder", "REMINDER"),
                    Pair("Recommendation (default)", "RECOMMENDATION"),
                    Pair("Status", "STATUS"),
                ),
                prefKey = "notification_category", default = "RECOMMENDATION",
            ),
        ),
        group(
            "Search engines",
            SearchEngineDef(
                key = "search_engine_1", title = "Search engine 1",
                prefKey = "search_engine_1",
                defaultUrl = "https://ja.wiktionary.org/w/index.php?search=%s",
                defaultTag = "Wiktionary",
            ),
            SearchEngineDef(
                key = "search_engine_2", title = "Search engine 2",
                prefKey = "search_engine_2",
                defaultUrl = "https://jisho.org/search/%s",
                defaultTag = "Jisho",
            ),
            SearchEngineDef(key = "search_engine_3", title = "Search engine 3", prefKey = "search_engine_3"),
            SearchEngineDef(key = "search_engine_4", title = "Search engine 4", prefKey = "search_engine_4"),
            SearchEngineDef(key = "search_engine_5", title = "Search engine 5", prefKey = "search_engine_5"),
        ),
        group(
            "Miscellaneous",
            SwitchDef(
                key = "enable_star_ratings", title = "Enable star ratings for subjects",
                summary = "If enabled you can give star ratings to subjects and use those ratings as search filters",
                prefKey = "enable_star_ratings",
            ),
        ),
    )
}

/**
 * Experimental (temporary) settings.
 */
internal fun experimentalSettingsGroups(): List<SettingGroup> = listOf(
    SettingGroup(
        null,
        listOf(
            InfoDef(
                key = "experimental_notice", title = "Caution",
                html = Constants.EXPERIMENTAL_PREFERENCE_STATUS_NOTICE,
                icon = MaterialSymbolsIcons.Science,
            ),
        ),
    ),
    group(
        "Subject cards",
        SelectDef(
            key = "exp_subject_card_layout_search",
            title = "Subject card layout in search results",
            options = SUBJECT_CARD_LAYOUT_OPTIONS,
            prefKey = "exp_subject_card_layout_search", default = "NORMAL",
        ),
        SelectDef(
            key = "exp_subject_card_layout_grid",
            title = "Subject card layout in other places",
            options = SUBJECT_CARD_LAYOUT_OPTIONS,
            prefKey = "exp_subject_card_layout_grid", default = "NORMAL",
        ),
    ),
)

/**
 * Maintenance: data import/export and destructive reset actions.
 */
internal fun otherActionsSettingsGroups(
    context: Context,
    onOpenCategory: (String, String?) -> Unit,
    onGoHome: () -> Unit,
): List<SettingGroup> = listOf(
    group(
        "Data",
        ActionDef(
            key = "data_import_export", title = "Data import / export",
            summary = "Import or export search presets and star ratings",
            navigates = true,
            onClick = { onOpenCategory("data_import_export", null) },
        ),
    ),
    group(
        "Maintenance",
        ActionDef(
            key = "flush_tasks", title = "Flush background tasks",
            summary = "Clear stuck background tasks",
            confirm = ConfirmSpec(
                title = "Flush background tasks?",
                html = Constants.FLUSH_TASKS_WARNING,
            ),
            onConfirmed = {
                JobRunnerService.schedule(FlushTasksJob::class.java, "")
                Toast.makeText(context, "Background tasks flushed!", Toast.LENGTH_LONG).show()
            },
        ),
        ActionDef(
            key = "reset_tutorials", title = "Reset UI confirmations and tutorials",
            confirm = ConfirmSpec(
                title = "Reset confirmations and tutorials?",
                text = Constants.RESET_TUTORIALS_WARNING,
            ),
            onConfirmed = {
                GlobalSettings.resetConfirmationsAndTutorials()
                Toast.makeText(context, "Confirmations and tutorials reset", Toast.LENGTH_LONG).show()
            },
        ),
        ActionDef(
            key = "reset_database", title = "Reset database",
            summary = "Clear all data in the app",
            confirm = ConfirmSpec(
                title = "Reset database?",
                html = Constants.RESET_DATABASE_WARNING,
                destructive = true,
            ),
            onConfirmed = {
                JobRunnerService.schedule(ResetDatabaseJob::class.java, "")
                onGoHome()
            },
        ),
    ),
)

/**
 * Advanced lesson settings.
 */
internal fun advancedLessonGroups(): List<SettingGroup> = listOf(
    group(
        "Ordering",
        SelectDef(
            key = "lesson_order", title = "Order",
            options = LESSON_ORDER_OPTIONS,
            prefKey = "lesson_order", default = "LEVEL_THEN_TYPE",
        ),
        SwitchDef(
            key = "lesson_order_reversed", title = "Reverse order",
            prefKey = "lesson_order_reversed",
        ),
        SelectDef(
            key = "lesson_order_priority", title = "Priority",
            options = SESSION_PRIORITY_OPTIONS,
            prefKey = "lesson_order_priority", default = "NONE",
        ),
        SwitchDef(
            key = "lesson_shuffle_after_selection", title = "Shuffle after selecting items",
            summary = "Items are selected for the session according to ordering settings, " +
                "but after that they are shuffled randomly.",
            prefKey = "lesson_shuffle_after_selection",
        ),
    ),
    group(
        "Question order",
        SwitchDef(
            key = "lesson_back_to_back", title = "Reading/meaning back-to-back",
            prefKey = "lesson_back_to_back",
        ),
        SwitchDef(
            key = "lesson_reading_first", title = "Reading before meaning",
            prefKey = "lesson_reading_first",
        ),
        SwitchDef(
            key = "lesson_meaning_first", title = "Meaning before reading",
            prefKey = "lesson_meaning_first",
        ),
    ),
    group(
        "Anki mode",
        SwitchDef(
            key = "anki_mode_lesson_meaning", title = "Anki mode for meanings",
            prefKey = "anki_mode_lesson_meaning",
        ),
        SwitchDef(
            key = "anki_mode_lesson_reading", title = "Anki mode for readings",
            prefKey = "anki_mode_lesson_reading",
        ),
    ),
    group(
        "Subject selection",
        InfoDef(title = "", html = Constants.SUBJECT_SELECTION_NOTICE),
        NumberRangeDef(
            key = "lesson_subjects_current", title = "Current-level items",
            prefKey = "lesson_subjects_current", minKey = "lesson_subjects_current_min",
        ),
        NumberRangeDef(
            key = "lesson_subjects_earlier", title = "Earlier-level items",
            prefKey = "lesson_subjects_earlier", minKey = "lesson_subjects_earlier_min",
        ),
        NumberRangeDef(
            key = "lesson_subjects_radical", title = "Radicals",
            prefKey = "lesson_subjects_radical", minKey = "lesson_subjects_radical_min",
        ),
        NumberRangeDef(
            key = "lesson_subjects_kanji", title = "Kanji",
            prefKey = "lesson_subjects_kanji", minKey = "lesson_subjects_kanji_min",
        ),
        NumberRangeDef(
            key = "lesson_subjects_vocabulary", title = "Vocabulary",
            prefKey = "lesson_subjects_vocabulary", minKey = "lesson_subjects_vocabulary_min",
        ),
        // Legacy quirk: GlobalSettings reads the lesson kana-vocabulary min
        // from a key with a capital K, so write that key.
        NumberRangeDef(
            key = "lesson_subjects_kana_vocabulary", title = "Kana Vocabulary",
            prefKey = "lesson_subjects_kana_vocabulary",
            minKey = "lesson_subjects_Kana_vocabulary_min",
        ),
    ),
)

/**
 * Advanced review settings.
 */
internal fun advancedReviewGroups(): List<SettingGroup> = listOf(
    group(
        "Ordering",
        SelectDef(
            key = "review_order", title = "Order",
            options = REVIEW_ORDER_OPTIONS,
            prefKey = "review_order", default = "SHUFFLE",
        ),
        SwitchDef(
            key = "review_order_reversed", title = "Reverse order",
            prefKey = "review_order_reversed",
        ),
        SwitchDef(
            key = "review_order_overdue_first", title = "Overdue items first",
            prefKey = "review_order_overdue_first",
        ),
        SelectDef(
            key = "review_order_priority", title = "Priority",
            options = SESSION_PRIORITY_OPTIONS,
            prefKey = "review_order_priority", default = "NONE",
        ),
        SwitchDef(
            key = "review_shuffle_after_selection", title = "Shuffle after selecting items",
            summary = "Items are selected for the session according to ordering settings, " +
                "but after that they are shuffled randomly.",
            prefKey = "review_shuffle_after_selection",
        ),
    ),
    group(
        "Question order",
        SwitchDef(
            key = "review_back_to_back", title = "Reading/meaning back-to-back",
            prefKey = "review_back_to_back",
        ),
        SwitchDef(
            key = "review_reading_first", title = "Reading before meaning",
            prefKey = "review_reading_first",
        ),
        SwitchDef(
            key = "review_meaning_first", title = "Meaning before reading",
            prefKey = "review_meaning_first",
        ),
    ),
    group(
        "Anki mode",
        SwitchDef(
            key = "anki_mode_review_meaning", title = "Anki mode for meanings",
            prefKey = "anki_mode_review_meaning",
        ),
        SwitchDef(
            key = "anki_mode_review_reading", title = "Anki mode for readings",
            prefKey = "anki_mode_review_reading",
        ),
        SwitchDef(
            key = "review_randomize_inflections", title = "Randomise inflections in review",
            summary = "Works best with Anki mode. Random inflections will be added to adjectives and verbs. " +
                "Note that answers and audio will use only dictionary forms.",
            prefKey = "review_randomize_inflections",
        ),
    ),
    group(
        "Subject selection",
        InfoDef(title = "", html = Constants.SUBJECT_SELECTION_NOTICE),
        NumberRangeDef(
            key = "review_subjects_current", title = "Current-level items",
            prefKey = "review_subjects_current", minKey = "review_subjects_current_min",
        ),
        NumberRangeDef(
            key = "review_subjects_earlier", title = "Earlier-level items",
            prefKey = "review_subjects_earlier", minKey = "review_subjects_earlier_min",
        ),
        NumberRangeDef(
            key = "review_subjects_radical", title = "Radicals",
            prefKey = "review_subjects_radical", minKey = "review_subjects_radical_min",
        ),
        NumberRangeDef(
            key = "review_subjects_kanji", title = "Kanji",
            prefKey = "review_subjects_kanji", minKey = "review_subjects_kanji_min",
        ),
        NumberRangeDef(
            key = "review_subjects_vocabulary", title = "Vocabulary",
            prefKey = "review_subjects_vocabulary", minKey = "review_subjects_vocabulary_min",
        ),
        NumberRangeDef(
            key = "review_subjects_kana_vocabulary", title = "Kana Vocabulary",
            prefKey = "review_subjects_kana_vocabulary", minKey = "review_subjects_kana_vocabulary_min",
        ),
    ),
)

/**
 * Advanced self-study settings.
 */
internal fun advancedSelfStudyGroups(): List<SettingGroup> = listOf(
    group(
        "Ordering",
        SelectDef(
            key = "self_study_order", title = "Order",
            options = REVIEW_ORDER_OPTIONS,
            prefKey = "self_study_order", default = "SHUFFLE",
        ),
        SwitchDef(
            key = "self_study_order_reversed", title = "Reverse order",
            prefKey = "self_study_order_reversed",
        ),
        SwitchDef(
            key = "self_study_order_overdue_first", title = "Overdue items first",
            prefKey = "self_study_order_overdue_first",
        ),
        SelectDef(
            key = "self_study_order_priority", title = "Priority",
            options = SESSION_PRIORITY_OPTIONS,
            prefKey = "self_study_order_priority", default = "NONE",
        ),
        SwitchDef(
            key = "self_study_shuffle_after_selection", title = "Shuffle after selecting items",
            summary = "Items are selected for the session according to ordering settings, " +
                "but after that they are shuffled randomly.",
            prefKey = "self_study_shuffle_after_selection",
        ),
    ),
    group(
        "Question order",
        SwitchDef(
            key = "self_study_back_to_back", title = "Reading/meaning back-to-back",
            prefKey = "self_study_back_to_back",
        ),
        SwitchDef(
            key = "self_study_reading_first", title = "Reading before meaning",
            prefKey = "self_study_reading_first",
        ),
        SwitchDef(
            key = "self_study_meaning_first", title = "Meaning before reading",
            prefKey = "self_study_meaning_first",
        ),
    ),
    group(
        "Anki mode",
        SwitchDef(
            key = "anki_mode_self_study_meaning", title = "Anki mode for meanings",
            prefKey = "anki_mode_self_study_meaning",
        ),
        SwitchDef(
            key = "anki_mode_self_study_reading", title = "Anki mode for readings",
            prefKey = "anki_mode_self_study_reading",
        ),
        SwitchDef(
            key = "self_study_randomize_inflections", title = "Randomize inflections in self-study",
            summary = "Works best with Anki mode. Random inflections will be added to adjectives and verbs. " +
                "Note that answers and audio will use only dictionary forms.",
            prefKey = "self_study_randomize_inflections",
        ),
    ),
    group(
        "Subject selection",
        InfoDef(title = "", html = Constants.SUBJECT_SELECTION_NOTICE),
        NumberRangeDef(
            key = "self_study_subjects_current", title = "Current-level items",
            prefKey = "self_study_subjects_current", minKey = "self_study_subjects_current_min",
        ),
        NumberRangeDef(
            key = "self_study_subjects_earlier", title = "Earlier-level items",
            prefKey = "self_study_subjects_earlier", minKey = "self_study_subjects_earlier_min",
        ),
        NumberRangeDef(
            key = "self_study_subjects_radical", title = "Radicals",
            prefKey = "self_study_subjects_radical", minKey = "self_study_subjects_radical_min",
        ),
        NumberRangeDef(
            key = "self_study_subjects_kanji", title = "Kanji",
            prefKey = "self_study_subjects_kanji", minKey = "self_study_subjects_kanji_min",
        ),
        NumberRangeDef(
            key = "self_study_subjects_vocabulary", title = "Vocabulary",
            prefKey = "self_study_subjects_vocabulary", minKey = "self_study_subjects_vocabulary_min",
        ),
        NumberRangeDef(
            key = "self_study_subjects_kana_vocabulary", title = "Kana Vocabulary",
            prefKey = "self_study_subjects_kana_vocabulary", minKey = "self_study_subjects_kana_vocabulary_min",
        ),
    ),
)

/**
 * Other advanced settings.
 */
internal fun advancedOtherGroups(): List<SettingGroup> = listOf(
    group(
        "Questions",
        TextFieldDef(
            key = "overdue_threshold", title = "Overdue threshold percentage",
            prefKey = "overdue_threshold", default = "20",
            keyboardType = KeyboardType.Number,
        ),
        SwitchDef(
            key = "kanji_mode_onkun", title = "Quiz on/kun'yomi separately",
            prefKey = "kanji_mode_onkun",
        ),
        SwitchDef(
            key = "shake_on_matching_kanji", title = "Soft-reject on single-kanji vocabulary",
            summary = "If an incorrect reading is an alternative reading for the kanji, do a shake-and-retry instead of failing",
            prefKey = "shake_on_matching_kanji", default = true,
        ),
        SwitchDef(
            key = "indicate_kanji_reading_type", title = "Indicate type of reading for kanji quiz",
            summary = "For a kanji reading question, indicate if the question requires an on'yomi or kun'yomi answer",
            prefKey = "indicate_kanji_reading_type",
        ),
    ),
    group(
        "Buttons",
        SelectDef(
            key = "undo_button", title = "Special button 1 behavior (top right)",
            options = SPECIAL_BUTTON_OPTIONS,
            prefKey = "undo_button", default = "DISABLE",
        ),
        SelectDef(
            key = "ignore_button", title = "Special button 2 behavior (bottom left)",
            options = SPECIAL_BUTTON_OPTIONS,
            prefKey = "ignore_button", default = "DISABLE",
        ),
        SelectDef(
            key = "special_button_3", title = "Special button 3 behavior (top left)",
            options = SPECIAL_BUTTON_OPTIONS,
            prefKey = "special_button_3", default = "DISABLE",
        ),
        SelectDef(
            key = "dont_know_button", title = "Don't know button behavior",
            options = listOf(
                Pair("Disable", "DISABLE"),
                Pair("Enable", "ENABLE"),
                Pair("Hide if Special button 2 is visible", "ONLY_IF_IGNORE_HIDDEN"),
            ),
            prefKey = "dont_know_button", default = "ONLY_IF_IGNORE_HIDDEN",
        ),
    ),
    group(
        "Session",
        SwitchDef(
            key = "full_session_log", title = "Show full session log",
            summary = "This may reveal inappropriate hints and answers",
            prefKey = "full_session_log",
        ),
    ),
)

//
