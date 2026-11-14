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
import com.lazy_durtles.wk.ui.BackupRestoreScreen
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

@Composable
fun SettingsMainScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenCategory: (String, String?) -> Unit = { _, _ -> },
) {
    CategoryListScreen(
        onBack = onBack,
        onCategoryClick = onOpenCategory,
        modifier = modifier,
    )
}

/**
 * One settings category: either a full sub-screen (backup, font selection,
 * ...) or a declaratively rendered detail panel. Every level is its own entry
 * on the navigation stack, so system back goes one level out at a time.
 */
@Composable
fun SettingsCategoryScreenImpl(
    categoryId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenCategory: (String, String?) -> Unit = { _, _ -> },
    onGoHome: () -> Unit = {},
    highlight: String? = null,
) {
    when (categoryId) {
        "backup" -> BackupRestoreScreen(onBack = onBack)
        "data_import_export" -> DataImportExportScreen(onBack = onBack)
        "font_selection" -> FontSelectionScreen(onBack = onBack)
        "font_import" -> FontImportScreen(onBack = onBack)
        "download_audio" -> DownloadAudioScreen(onBack = onBack)
        "keyboard_help" -> KeyboardHelpScreen(
            onOpenKeyboardSettings = { onOpenCategory("keyboard", null) },
            onBack = onBack,
            modifier = modifier,
        )
        "theme_customization" -> ThemeCustomizationScreen(onBack = onBack)
        "about" -> AboutCategoryScreen(onBack = onBack)
        else -> DefCategoryScreen(
            categoryId = categoryId,
            highlight = highlight,
            onBack = onBack,
            onOpenCategory = onOpenCategory,
            onGoHome = onGoHome,
        )
    }
}

//
// Top-level category list.
//

/**
 * A category of settings shown in the top-level list.
 */
internal data class SettingsCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: Any?, // Char (Material Symbols codepoint) or drawable resource ID
)

/**
 * A visual group of related categories, rendered as its own card.
 */
internal data class SettingsCategoryGroup(
    val title: String,
    val categories: List<SettingsCategory>,
)

internal val CATEGORY_GROUPS = listOf(
    SettingsCategoryGroup(
        title = "Account",
        categories = listOf(
            SettingsCategory(
                "api", "Login to WaniKani",
                "API token, sync and network rules",
                MaterialSymbolsIcons.Person,
            ),
        ),
    ),
    SettingsCategoryGroup(
        title = "Audio",
        categories = listOf(
            SettingsCategory(
                "audio", "Audio",
                "Autoplay, voice and audio files",
                MaterialSymbolsIcons.VolumeUp,
            ),
            SettingsCategory(
                "download_audio", "Download audio",
                "Download audio files for offline use",
                MaterialSymbolsIcons.CloudDownload,
            ),
        ),
    ),
    SettingsCategoryGroup(
        title = "Appearance",
        categories = listOf(
            SettingsCategory(
                "display", "Display",
                "Themes and quiz screen appearance",
                MaterialSymbolsIcons.Palette,
            ),
            SettingsCategory(
                "font", "Fonts",
                "Font selection and sizes",
                MaterialSymbolsIcons.FontDownload,
            ),
            SettingsCategory(
                "dashboard", "Dashboard",
                "What to show on the dashboard",
                MaterialSymbolsIcons.SpaceDashboard,
            ),
        ),
    ),
    SettingsCategoryGroup(
        title = "Study",
        categories = listOf(
            SettingsCategory(
                "lesson_review", "Lessons and reviews",
                "Session size, question behavior and advanced options",
                MaterialSymbolsIcons.School,
            ),
            SettingsCategory(
                "subject_info", "Subject info",
                "Mnemonics, info dump and stroke order",
                MaterialSymbolsIcons.MenuBook,
            ),
            SettingsCategory(
                "keyboard", "Keyboard input",
                "Input method related settings",
                MaterialSymbolsIcons.Keyboard,
            ),
        ),
    ),
    SettingsCategoryGroup(
        title = "General",
        categories = listOf(
            SettingsCategory(
                "other", "Notifications and more",
                "Notifications, search engines and misc",
                MaterialSymbolsIcons.Notifications,
            ),
            SettingsCategory(
                "experimental", "Experimental",
                "Short-term experiments, these will go away",
                MaterialSymbolsIcons.Science,
            ),
        ),
    ),
    SettingsCategoryGroup(
        title = "Data",
        categories = listOf(
            SettingsCategory(
                "backup", "Backup and restore",
                "Export settings to a file, or import a backup",
                MaterialSymbolsIcons.Backup,
            ),
            SettingsCategory(
                "data_import_export", "Data import / export",
                "Search presets and star ratings",
                MaterialSymbolsIcons.SyncAlt,
            ),
            SettingsCategory(
                "other_actions", "Maintenance",
                "Flush background tasks and resets",
                MaterialSymbolsIcons.Construction,
            ),
        ),
    ),
    SettingsCategoryGroup(
        title = "About",
        categories = listOf(
            SettingsCategory(
                "about", "About",
                "About this app",
                MaterialSymbolsIcons.Info,
            ),
        ),
    ),
)

/**
 * The top-level list of setting categories, with an Android-settings-style
 * search field at the top. While the search field has focus, the category
 * list is replaced by live search results with breadcrumbs.
 */
@Composable
internal fun CategoryListScreen(
    onBack: () -> Unit,
    onCategoryClick: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val searchIndex = rememberSearchIndex()

    BackHandler(enabled = searchActive) {
        searchActive = false
        focusManager.clearFocus()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SettingsTopBar(title = "Settings", onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SettingsSearchField(
                query = query,
                onQueryChange = { query = it },
                active = searchActive,
                onActiveChange = { searchActive = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (searchActive) {
                SearchResults(
                    query = query,
                    index = searchIndex,
                    onOpen = { entry ->
                        searchActive = false
                        focusManager.clearFocus()
                        onCategoryClick(entry.categoryId, entry.key)
                    },
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    CATEGORY_GROUPS.forEach { group ->
                        SettingsGroupHeader(group.title)
                        SettingsGroupCard {
                            group.categories.forEachIndexed { index, category ->
                                CategoryRow(category) { onCategoryClick(category.id, null) }
                                if (index < group.categories.lastIndex) {
                                    SettingsRowDivider()
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
