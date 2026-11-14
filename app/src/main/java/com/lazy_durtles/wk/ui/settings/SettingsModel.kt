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

// Declarative settings model.
//

/**
 * A single setting, declared as data so the category screens, the search index
 * and the search-highlight behavior all derive from one definition.
 */
internal interface SettingDef {
    /** Stable id, used for search results and search highlighting. */
    val key: String?
    val title: String
    val summary: String

    /** Whether the setting is currently shown; evaluated during composition. */
    val visibleIf: (SharedPreferences) -> Boolean
}

internal data class SwitchDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val prefKey: String,
    val default: Boolean = false,
    /** If set, turning the switch ON first asks for confirmation. */
    val confirmTitle: String? = null,
    val confirmHtml: String? = null,
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

internal data class SelectDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val prefKey: String,
    val default: String,
    /** (label, value) pairs. */
    val options: List<Pair<String, String>>,
    val summarySuffix: String = "",
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

internal data class SliderDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val range: ClosedFloatingPointRange<Float>,
    val read: (SharedPreferences) -> Int,
    val write: (Int, SharedPreferences) -> Unit,
    val valueText: (Int) -> String = { it.toString() },
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

internal data class TextFieldDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val prefKey: String,
    val default: String = "",
    val keyboardType: KeyboardType = KeyboardType.Text,
    /**
     * Optional custom accessor for values kept outside the shared prefs,
     * e.g. the API token in the encrypted store. When set, it replaces the
     * prefKey-based read.
     */
    val read: ((SharedPreferences) -> String)? = null,
    /** Optional custom writer matching [read]; replaces the prefKey-based write. */
    val write: ((String, SharedPreferences) -> Unit)? = null,
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

internal data class ActionDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val navigates: Boolean = false,
    val destructive: Boolean = false,
    val onClick: () -> Unit = {},
    /** If set, tapping the row first asks for confirmation via a dialog. */
    val confirm: ConfirmSpec? = null,
    val onConfirmed: () -> Unit = {},
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

internal data class InfoDef(
    override val key: String? = null,
    override val title: String = "",
    val html: String,
    val icon: Any? = MaterialSymbolsIcons.Info,
    override val summary: String = "",
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

internal data class NumberRangeDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val prefKey: String,
    val minKey: String,
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

internal data class SearchEngineDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val prefKey: String,
    val defaultUrl: String = "",
    val defaultTag: String = "",
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

/**
 * A confirmation dialog attached to an [ActionDef].
 */
internal data class ConfirmSpec(
    val title: String,
    val text: String? = null,
    val html: String? = null,
    val icon: Any? = MaterialSymbolsIcons.Warning,
    val confirmLabel: String = "Yes",
    val dismissLabel: String = "No",
    val destructive: Boolean = false,
)

/**
 * A group of related settings, rendered as one rounded card with an optional
 * header.
 */
internal class SettingGroup(
    val header: String?,
    val items: List<SettingDef>,
)

internal fun group(header: String?, vararg items: SettingDef) = SettingGroup(header, items.toList())

/**
 * The title of the "api" category depends on the auth state: a login prompt
 * while no token is configured, connection management otherwise.
 */
internal fun apiCategoryTitle(): String =
    if (GlobalSettings.Api.getApiKey().isNullOrEmpty()) "Login to WaniKani"
    else "Manage WaniKani Connection"

/**
 * The title of a declaratively rendered category screen.
 */
internal fun categoryTitle(categoryId: String): String = when (categoryId) {
    "api" -> apiCategoryTitle()
    "audio" -> "Audio"
    "display" -> "Display"
    "dashboard" -> "Dashboard"
    "lesson_review", "advanced" -> "Lessons and reviews"
    "subject_info" -> "Subject info"
    "keyboard" -> "Keyboard input"
    "font" -> "Fonts"
    "other" -> "Notifications and more"
    "experimental" -> "Experimental"
    "other_actions" -> "Maintenance"
    "advanced_lesson_settings" -> "Advanced lesson settings"
    "advanced_review_settings" -> "Advanced review settings"
    "advanced_self_study_settings" -> "Advanced self-study settings"
    "advanced_other_settings" -> "Other advanced settings"
    else -> "Settings"
}

/**
 * The declarative settings for one category id.
 */
internal fun categoryDefs(
    categoryId: String,
    context: Context,
    onOpenCategory: (String, String?) -> Unit,
    onGoHome: () -> Unit,
    apiStateOk: () -> Boolean,
): List<SettingGroup> = when (categoryId) {
    "api" -> apiSettingsGroups(apiStateOk)
    "audio" -> audioSettingsGroups(onOpenCategory)
    "display" -> displaySettingsGroups(onOpenCategory)
    "dashboard" -> dashboardSettingsGroups()
    "lesson_review", "advanced" -> lessonReviewSettingsGroups(onOpenCategory)
    "subject_info" -> subjectInfoSettingsGroups()
    "keyboard" -> keyboardSettingsGroups(onOpenCategory)
    "font" -> fontSettingsGroups(onOpenCategory)
    "other" -> otherSettingsGroups(context)
    "experimental" -> experimentalSettingsGroups()
    "other_actions" -> otherActionsSettingsGroups(context, onOpenCategory, onGoHome)
    "advanced_lesson_settings" -> advancedLessonGroups()
    "advanced_review_settings" -> advancedReviewGroups()
    "advanced_self_study_settings" -> advancedSelfStudyGroups()
    "advanced_other_settings" -> advancedOtherGroups()
    else -> emptyList()
}

//
