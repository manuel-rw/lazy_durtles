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

// Helpers for reading and writing settings.
//

internal fun setPrefBoolean(value: Boolean, key: String, prefs: SharedPreferences) {
    prefs.edit { putBoolean(key, value) }
}

internal fun setPrefString(value: String, key: String, prefs: SharedPreferences) {
    prefs.edit { putString(key, value) }
}

internal fun setPrefInt(value: Int, key: String, prefs: SharedPreferences) {
    prefs.edit { putInt(key, value) }
}

//
// Shared option lists.
//

/**
 * The top app bar used by the settings screens, with a back button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsTopBar(
    title: String,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                MaterialSymbolIcon(
                    icon = MaterialSymbolsIcons.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
    )
}

internal val NETWORK_RULE_OPTIONS = listOf(
    Pair("Always allow sync and downloads", "ALWAYS"),
    Pair("Always allow sync, downloads only on unmetered connections (WiFi)", "DOWNLOAD_WIFI_ONLY"),
    Pair("Allow sync and downloads only on unmetered connections (WiFi)", "WIFI_ONLY"),
)

internal val VOICE_OPTIONS = listOf(
    Pair("Random", "RANDOM"),
    Pair("Masculine", "MALE"),
    Pair("Feminine", "FEMALE"),
    Pair("Alternate between voices", "ALTERNATE"),
)

internal val CLOSE_ENOUGH_OPTIONS = listOf(
    Pair("Accept silently", "SILENTLY_ACCEPT"),
    Pair("Accept and show a toast", "ACCEPT_WITH_TOAST"),
    Pair("Accept, show a toast, disable lightning mode for this question", "ACCEPT_WITH_TOAST_NO_LM"),
    Pair("Shake and retry", "SHAKE_AND_RETRY"),
    Pair("Reject", "REJECT"),
)

internal val THEME_OPTIONS = listOf(
    Pair("Wanikani", "LIGHT"),
    Pair("Wanikani Dark", "DARK_WANIKANI"),
    Pair("Wanikani Black", "BLACK_WANIKANI"),
    Pair("Breeze", "DARK"),
    Pair("Black", "BLACK_BREEZE"),
    Pair("Nord", "NORD_POLAR_NIGHT"),
)

internal val INFO_DUMP_OPTIONS = listOf(
    Pair("Show nothing", "NOTHING"),
    Pair("Show only answers", "ANSWERS_ONLY"),
    Pair("Hide information not quizzed yet", "HIDE_UNQUIZZED"),
    Pair("Show all information", "ALL"),
)

internal val CHART_SIZE_OPTIONS = listOf(
    Pair("12 hours", "12"),
    Pair("24 hours", "24"),
    Pair("36 hours", "36"),
    Pair("48 hours", "48"),
    Pair("3 days", "72"),
    Pair("5 days", "120"),
    Pair("7 days", "168"),
    Pair("10 days", "240"),
    Pair("14 days", "336"),
)

internal val REVIEW_ORDER_OPTIONS = listOf(
    Pair("Shuffle", "SHUFFLE"),
    Pair("Level", "LEVEL"),
    Pair("Type", "TYPE"),
    Pair("SRS stage", "SRS"),
    Pair("Level, Type", "LEVEL_THEN_TYPE"),
    Pair("Level, SRS stage", "LEVEL_THEN_SRS"),
    Pair("Type, Level", "TYPE_THEN_LEVEL"),
    Pair("Type, SRS stage", "TYPE_THEN_SRS"),
    Pair("SRS stage, Type", "SRS_THEN_TYPE"),
    Pair("SRS stage, Level", "SRS_THEN_LEVEL"),
    Pair("Level, Type, SRS stage", "LEVEL_THEN_TYPE_THEN_SRS"),
    Pair("Level, SRS stage, Type", "LEVEL_THEN_SRS_THEN_TYPE"),
    Pair("Type, Level, SRS stage", "TYPE_THEN_LEVEL_THEN_SRS"),
    Pair("Type, SRS stage, Level", "TYPE_THEN_SRS_THEN_LEVEL"),
    Pair("SRS stage, Type, Level", "SRS_THEN_TYPE_THEN_LEVEL"),
    Pair("SRS stage, Level, Type", "SRS_THEN_LEVEL_THEN_TYPE"),
)

internal val LESSON_ORDER_OPTIONS = listOf(
    Pair("Shuffle", "SHUFFLE"),
    Pair("Level, Type", "LEVEL_THEN_TYPE"),
    Pair("Radicals, Level, Type", "RADICALS_THEN_LEVEL_THEN_TYPE"),
    Pair("Type, Level", "TYPE_THEN_LEVEL"),
    Pair("Level, then shuffle", "LEVEL_THEN_SHUFFLE"),
    Pair("Type, then shuffle", "TYPE_THEN_SHUFFLE"),
)

internal val SESSION_PRIORITY_OPTIONS = listOf(
    Pair("None", "NONE"),
    Pair("Radicals first", "RADICALS_FIRST"),
    Pair("Level-up items first", "LEVEL_UP_FIRST"),
    Pair("Current-level radicals and kanji first", "CURRENT_LEVEL_RADICAL_KANJI_FIRST"),
    Pair("Current-level items first", "CURRENT_LEVEL_FIRST"),
)

internal val SPECIAL_BUTTON_OPTIONS = listOf(
    Pair("Disable", "DISABLE"),
    Pair("Ignore (undo and mark correct instead)", "UNDO_AND_MARK_CORRECT"),
    Pair("Undo and retry immediately", "UNDO_AND_RETRY_NOW"),
    Pair("Undo and put back into queue", "UNDO_AND_PUT_BACK"),
    Pair("Skip (put back into queue)", "SKIP"),
    Pair("Toggle lightning mode", "TOGGLE_LIGHTNING_MODE"),
)

internal val SUBJECT_CARD_LAYOUT_OPTIONS = listOf(
    Pair("Normal", "NORMAL"),
    Pair("Compact with progression info", "COMPACT"),
    Pair("Compact without progression info", "COMPACT_NO_PROGRESSION"),
)

