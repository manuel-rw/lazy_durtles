package com.lazy_durtles.wk.ui

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

/**
 * Settings screen. Shows the grouped list of setting categories with a search
 * field at the top, in the Material Expressive 3 style; each category is
 * opened as its own screen on the navigation stack via [onOpenCategory]
 * (category id, optional setting key to highlight). All values are persisted
 * immediately via GlobalSettings / SharedPreferences.
 */
@Composable
fun SettingsScreen(
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
fun SettingsCategoryScreen(
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
private data class SettingsCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: Any?, // Char (Material Symbols codepoint) or drawable resource ID
)

/**
 * A visual group of related categories, rendered as its own card.
 */
private data class SettingsCategoryGroup(
    val title: String,
    val categories: List<SettingsCategory>,
)

private val CATEGORY_GROUPS = listOf(
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
private fun CategoryListScreen(
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

/**
 * The pill-shaped search field at the top of the settings list, styled like
 * the one in the Android settings app.
 */
@Composable
private fun SettingsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .onFocusChanged { state ->
                if (state.isFocused && !active) {
                    onActiveChange(true)
                }
            },
        placeholder = { Text(text = "Search settings") },
        leadingIcon = {
            if (active) {
                IconButton(onClick = {
                    onActiveChange(false)
                    focusManager.clearFocus()
                }) {
                    MaterialSymbolIcon(
                        icon = MaterialSymbolsIcons.ArrowBack,
                        contentDescription = "Stop searching",
                    )
                }
            } else {
                MaterialSymbolIcon(
                    icon = MaterialSymbolsIcons.Search,
                    contentDescription = "Search",
                )
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    MaterialSymbolIcon(
                        icon = MaterialSymbolsIcons.Close,
                        contentDescription = "Clear search",
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

/**
 * One searchable item: either a category, or an individual setting inside a
 * category.
 */
private class SearchEntry(
    val key: String?,
    val title: String,
    val summary: String,
    val breadcrumb: String,
    val categoryId: String,
    val icon: Any?,
)

/**
 * Builds the search index once: every category plus every individual setting
 * declared in [categoryDefs], with a breadcrumb of where to find it.
 */
@Composable
private fun rememberSearchIndex(): List<SearchEntry> {
    val context = LocalContext.current
    return remember(context) {
        val entries = mutableListOf<SearchEntry>()
        for (group in CATEGORY_GROUPS) {
            for (category in group.categories) {
                entries.add(
                    SearchEntry(
                        key = null,
                        title = category.title,
                        summary = category.description,
                        breadcrumb = "Settings",
                        categoryId = category.id,
                        icon = category.icon,
                    ),
                )
                for (defGroup in categoryDefs(category.id, context, { _, _ -> }, { }, { true })) {
                    for (def in defGroup.items) {
                        val breadcrumb = if (defGroup.header != null) {
                            "${category.title} › ${defGroup.header}"
                        } else {
                            category.title
                        }
                        entries.add(
                            SearchEntry(
                                key = def.key,
                                title = def.title,
                                summary = def.summary,
                                breadcrumb = breadcrumb,
                                categoryId = category.id,
                                icon = category.icon,
                            ),
                        )
                    }
                }
            }
        }
        entries
    }
}

/**
 * Live search results, like the Android settings app: matching settings with
 * their title and location breadcrumb, or all sections when the query is
 * empty.
 */
@Composable
private fun SearchResults(
    query: String,
    index: List<SearchEntry>,
    onOpen: (SearchEntry) -> Unit,
) {
    val q = query.trim()
    val matches = if (q.isEmpty()) {
        index.filter { it.key == null && it.breadcrumb == "Settings" }
    } else {
        index.filter { entry ->
            entry.title.contains(q, ignoreCase = true) ||
                entry.summary.contains(q, ignoreCase = true) ||
                entry.breadcrumb.contains(q, ignoreCase = true)
        }
    }

    if (matches.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No results for \u201C$q\u201D",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 32.dp),
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    ) {
        if (q.isEmpty()) {
            item {
                Text(
                    text = "Browse sections",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
                )
            }
        }
        items(matches, key = { "${it.categoryId}:${it.key ?: "category"}" }) { entry ->
            SearchResultRow(entry, onOpen)
        }
    }
}

/**
 * One row in the search results: the category icon, the setting title, and a
 * breadcrumb of where to find it.
 */
@Composable
private fun SearchResultRow(
    entry: SearchEntry,
    onOpen: (SearchEntry) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SettingHighlightShape)
            .clickable { onOpen(entry) }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TonalIcon(entry.icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = entry.breadcrumb,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * One category row in the top-level list.
 */
@Composable
private fun CategoryRow(
    category: SettingsCategory,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TonalIcon(category.icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = category.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        MaterialSymbolIcon(
            icon = MaterialSymbolsIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

//
// Declarative settings model.
//

/**
 * A single setting, declared as data so the category screens, the search index
 * and the search-highlight behavior all derive from one definition.
 */
private interface SettingDef {
    /** Stable id, used for search results and search highlighting. */
    val key: String?
    val title: String
    val summary: String

    /** Whether the setting is currently shown; evaluated during composition. */
    val visibleIf: (SharedPreferences) -> Boolean
}

private data class SwitchDef(
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

private data class SelectDef(
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

private data class SliderDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val range: ClosedFloatingPointRange<Float>,
    val read: (SharedPreferences) -> Int,
    val write: (Int, SharedPreferences) -> Unit,
    val valueText: (Int) -> String = { it.toString() },
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

private data class TextFieldDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val prefKey: String,
    val default: String = "",
    val keyboardType: KeyboardType = KeyboardType.Text,
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

private data class ActionDef(
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

private data class InfoDef(
    override val key: String? = null,
    override val title: String = "",
    val html: String,
    val icon: Any? = MaterialSymbolsIcons.Info,
    override val summary: String = "",
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

private data class NumberRangeDef(
    override val key: String,
    override val title: String,
    override val summary: String = "",
    val prefKey: String,
    val minKey: String,
    override val visibleIf: (SharedPreferences) -> Boolean = { true },
) : SettingDef

private data class SearchEngineDef(
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
private data class ConfirmSpec(
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
private class SettingGroup(
    val header: String?,
    val items: List<SettingDef>,
)

private fun group(header: String?, vararg items: SettingDef) = SettingGroup(header, items.toList())

/**
 * The title of a declaratively rendered category screen.
 */
private fun categoryTitle(categoryId: String): String = when (categoryId) {
    "api" -> "Login to WaniKani"
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
private fun categoryDefs(
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
// Rendering of declarative settings.
//

/**
 * The default shared preferences, plus a version counter that increments on
 * every settings change. Reading the counter's value during composition makes
 * the reading scope recompose with fresh values.
 */
@Composable
private fun rememberObservedPrefs(): Pair<SharedPreferences, MutableIntState> {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    val version = remember { mutableIntStateOf(0) }
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            version.intValue++
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    return prefs to version
}

/**
 * Renders a list of setting groups: an optional header and a rounded card per
 * group. When [highlight] names a setting, the screen scrolls to it and tints
 * it briefly, like the Android settings app does after a search.
 */
@Composable
private fun SettingGroups(
    groups: List<SettingGroup>,
    highlight: String?,
    modifier: Modifier = Modifier,
) {
    val (prefs, versionState) = rememberObservedPrefs()
    val scrollState = rememberScrollState()
    val activeHighlight = remember { mutableStateOf(highlight) }
    val rowTops = remember { mutableStateMapOf<String, Int>() }
    val containerTop = remember { mutableIntStateOf(0) }

    LaunchedEffect(highlight) {
        if (highlight == null) {
            return@LaunchedEffect
        }
        var top: Int? = null
        var attempts = 0
        while (top == null && attempts < 120) {
            top = rowTops[highlight]
            if (top == null) {
                delay(16)
                attempts++
            }
        }
        if (top != null) {
            scrollState.animateScrollTo((top - 240).coerceIn(0, scrollState.maxValue))
            delay(2400)
        }
        activeHighlight.value = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                containerTop.intValue = coords.positionInRoot().y.toInt()
            }
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
    ) {
        groups.forEach { group ->
            val items = group.items.filter { it.visibleIf(prefs) }
            if (items.isEmpty()) {
                return@forEach
            }
            if (group.header != null) {
                SettingsGroupHeader(group.header)
            }
            SettingsGroupCard {
                items.forEachIndexed { index, def ->
                    SettingRow(
                        def = def,
                        prefs = prefs,
                        prefVersion = versionState.intValue,
                        highlightKey = activeHighlight.value,
                        rowTops = rowTops,
                        containerTop = containerTop,
                    )
                    if (index < items.lastIndex) {
                        SettingsRowDivider()
                    }
                }
            }
        }
    }
}

/**
 * One setting row, wrapped in the search-highlight container.
 */
@Composable
private fun SettingRow(
    def: SettingDef,
    prefs: SharedPreferences,
    prefVersion: Int,
    highlightKey: String?,
    rowTops: MutableMap<String, Int>,
    containerTop: MutableIntState,
) {
    val defKey = def.key
    val highlighted = defKey != null && defKey == highlightKey
    val backgroundColor = highlightContainerColor(highlighted)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SettingHighlightShape)
            .background(backgroundColor)
            .then(
                if (defKey == null) {
                    Modifier
                } else {
                    Modifier.onGloballyPositioned { coords ->
                        rowTops[defKey] = coords.positionInRoot().y.toInt() - containerTop.intValue
                    }
                },
            ),
    ) {
        when (def) {
            is SwitchDef -> SwitchRowContent(def, prefs)
            is SelectDef -> SelectRowContent(def, prefs)
            is SliderDef -> SliderRowContent(def, prefs)
            is TextFieldDef -> TextFieldRowContent(def, prefs)
            is ActionDef -> ActionRowContent(def)
            is InfoDef -> InfoRowContent(def)
            is NumberRangeDef -> NumberRangeRowContent(def, prefs)
            is SearchEngineDef -> SearchEngineRowContent(def, prefs)
        }
    }
}

/**
 * A boolean setting rendered as a switch.
 */
@Composable
private fun SwitchRowContent(def: SwitchDef, prefs: SharedPreferences) {
    var confirmEnable by remember { mutableStateOf(false) }
    val checked = prefs.getBoolean(def.prefKey, def.default)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!checked && def.confirmTitle != null) {
                    confirmEnable = true
                } else {
                    prefs.edit { putBoolean(def.prefKey, !checked) }
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = def.title, style = MaterialTheme.typography.bodyLarge)
            if (def.summary.isNotEmpty()) {
                Text(
                    text = def.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { newValue ->
                if (newValue && def.confirmTitle != null) {
                    confirmEnable = true
                } else {
                    prefs.edit { putBoolean(def.prefKey, newValue) }
                }
            },
        )
    }

    if (confirmEnable) {
        SettingsAlertDialog(
            title = def.confirmTitle ?: "",
            html = def.confirmHtml,
            icon = MaterialSymbolsIcons.Warning,
            confirmLabel = "Yes",
            dismissLabel = "No",
            onConfirm = {
                confirmEnable = false
                prefs.edit { putBoolean(def.prefKey, true) }
            },
            onDismiss = { confirmEnable = false },
        )
    }
}

/**
 * A setting with a fixed set of options. Opens a dialog with radio buttons
 * when tapped; the summary shows the human-readable label of the current
 * value.
 */
@Composable
private fun SelectRowContent(def: SelectDef, prefs: SharedPreferences) {
    var open by remember { mutableStateOf(false) }
    val value = prefs.getString(def.prefKey, def.default) ?: def.default
    val label = def.options.firstOrNull { it.second == value }?.first ?: value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { open = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = def.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = label + def.summarySuffix,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (open) {
        SettingsSelectDialog(
            title = def.title,
            options = def.options,
            selectedValue = value,
            onSelect = { prefs.edit { putString(def.prefKey, it) } },
            onDismiss = { open = false },
        )
    }
}

/**
 * A numeric setting rendered as a slider, with the current value shown at the
 * end of the title row.
 */
@Composable
private fun SliderRowContent(def: SliderDef, prefs: SharedPreferences) {
    val current = def.read(prefs)
    var dragValue by remember(current) { mutableStateOf(current.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = def.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = def.valueText(dragValue.toInt()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (def.summary.isNotEmpty()) {
            Text(
                text = def.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = dragValue,
            onValueChange = { dragValue = it },
            onValueChangeFinished = { def.write(dragValue.toInt(), prefs) },
            valueRange = def.range,
        )
    }
}

/**
 * A text setting. Opens a dialog with a text field when tapped.
 */
@Composable
private fun TextFieldRowContent(def: TextFieldDef, prefs: SharedPreferences) {
    var editing by remember { mutableStateOf(false) }
    val value = prefs.getString(def.prefKey, def.default) ?: def.default

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { editing = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = def.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = if (def.summary.isNotEmpty()) def.summary else value.ifEmpty { "(not set)" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (editing) {
        var text by remember(value) { mutableStateOf(value) }
        AlertDialog(
            onDismissRequest = { editing = false },
            shape = SettingsDialogShape,
            title = { Text(text = def.title, style = MaterialTheme.typography.headlineSmall) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = def.keyboardType),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit { putString(def.prefKey, text) }
                    editing = false
                }) { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text(text = "Cancel") }
            },
        )
    }
}

/**
 * A clickable row that performs an action, optionally behind a confirmation
 * dialog.
 */
@Composable
private fun ActionRowContent(def: ActionDef) {
    var pendingConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (def.confirm != null) {
                    pendingConfirm = true
                } else {
                    def.onClick()
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = def.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (def.destructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (def.summary.isNotEmpty()) {
                Text(
                    text = def.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (def.navigates) {
            MaterialSymbolIcon(
                icon = MaterialSymbolsIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (pendingConfirm && def.confirm != null) {
        SettingsAlertDialog(
            title = def.confirm.title,
            text = def.confirm.text,
            html = def.confirm.html,
            icon = def.confirm.icon,
            confirmLabel = def.confirm.confirmLabel,
            dismissLabel = def.confirm.dismissLabel,
            destructive = def.confirm.destructive,
            onConfirm = {
                pendingConfirm = false
                def.onConfirmed()
            },
            onDismiss = { pendingConfirm = false },
        )
    }
}

/**
 * A non-clickable row that shows a block of (HTML) info text, like the legacy
 * InfoPreference, rendered as a tonal callout banner.
 */
@Composable
private fun InfoRowContent(def: InfoDef) {
    HtmlNoticeBanner(
        html = def.html,
        title = def.title,
        icon = def.icon,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

/**
 * A subject-selection range: a minimum and maximum value, each of which may be
 * empty. -1 means "not constrained". A port of the legacy NumberRangePreference.
 */
@Composable
private fun NumberRangeRowContent(def: NumberRangeDef, prefs: SharedPreferences) {
    var editing by remember { mutableStateOf(false) }
    val min = prefs.getInt(def.minKey, -1)
    val max = prefs.getInt("${def.prefKey}_max", -1)
    val summary = when {
        min == -1 && max == -1 -> "Unlimited"
        min == -1 -> "Max $max"
        max == -1 -> "Min $min"
        else -> String.format(Locale.ROOT, "%d - %d", min, max)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { editing = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = def.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (editing) {
        var minText by remember { mutableStateOf(if (min == -1) "" else min.toString()) }
        var maxText by remember { mutableStateOf(if (max == -1) "" else max.toString()) }
        AlertDialog(
            onDismissRequest = { editing = false },
            shape = SettingsDialogShape,
            title = { Text(text = def.title, style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose a minimum and maximum value for this category. " +
                            "Leave a field empty to not constrain that value.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = minText,
                            onValueChange = { minText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(text = "Min") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        Text(text = "-")
                        OutlinedTextField(
                            value = maxText,
                            onValueChange = { maxText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(text = "Max") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Empty means unconstrained; unparseable input keeps the
                    // old value, like the legacy safe() wrapper does.
                    if (minText.isEmpty()) {
                        prefs.edit { putInt(def.minKey, -1) }
                    } else {
                        minText.toIntOrNull()?.let { prefs.edit { putInt(def.minKey, it) } }
                    }
                    if (maxText.isEmpty()) {
                        prefs.edit { putInt("${def.prefKey}_max", -1) }
                    } else {
                        maxText.toIntOrNull()?.let { prefs.edit { putInt("${def.prefKey}_max", it) } }
                    }
                    editing = false
                }) { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text(text = "Cancel") }
            },
        )
    }
}

/**
 * A search engine: a name and a URL in which "%s" is replaced by the search
 * query. A port of the legacy TaggedUrlPreference.
 */
@Composable
private fun SearchEngineRowContent(def: SearchEngineDef, prefs: SharedPreferences) {
    var editing by remember { mutableStateOf(false) }
    val tag = prefs.getString("${def.prefKey}_tag", def.defaultTag) ?: def.defaultTag
    val url = prefs.getString(def.prefKey, def.defaultUrl) ?: def.defaultUrl

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { editing = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = def.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = tag.ifEmpty { "(not set)" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (editing) {
        var tagText by remember { mutableStateOf(tag) }
        var urlText by remember { mutableStateOf(url) }
        AlertDialog(
            onDismissRequest = { editing = false },
            shape = SettingsDialogShape,
            title = { Text(text = def.title, style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Search engines are shown when long-pressing on a subject title. " +
                            "In the URL, \"%s\" will be replaced with the search query.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = tagText,
                        onValueChange = { tagText = it },
                        label = { Text(text = "Name") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text(text = "URL") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit {
                        putString("${def.prefKey}_tag", tagText)
                        putString(def.prefKey, urlText)
                    }
                    editing = false
                }) { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text(text = "Cancel") }
            },
        )
    }
}

//
// Declarative category screens.
//

/**
 * A detail screen for one declaratively rendered category.
 */
@Composable
private fun DefCategoryScreen(
    categoryId: String,
    highlight: String?,
    onBack: () -> Unit,
    onOpenCategory: (String, String?) -> Unit,
    onGoHome: () -> Unit,
) {
    val context = LocalContext.current
    val apiState by LiveApiState.getInstance().observeAsState(initial = ApiState.UNKNOWN)

    val groups = categoryDefs(
        categoryId = categoryId,
        context = context,
        onOpenCategory = onOpenCategory,
        onGoHome = onGoHome,
        apiStateOk = { apiState == ApiState.OK },
    )

    Scaffold(
        topBar = {
            SettingsTopBar(title = categoryTitle(categoryId), onBack = onBack)
        },
    ) { padding ->
        SettingGroups(
            groups = groups,
            highlight = highlight,
            modifier = Modifier.padding(padding),
        )
    }
}

//
// Category definitions.
//

/**
 * Login to WaniKani: API token, account extras and sync behavior.
 */
private fun apiSettingsGroups(apiStateOk: () -> Boolean): List<SettingGroup> {
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
            ),
            TextFieldDef(
                key = "web_password", title = "Web site password",
                summary = "This is only needed for resurrect functionality. Leave blank if you don't need it.",
                prefKey = "web_password",
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
private fun audioSettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> = listOf(
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
private fun audioLocationOptions(): List<Pair<String, String>> {
    val values = com.lazy_durtles.wk.util.AudioUtil.getLocationValues()
    val labels = com.lazy_durtles.wk.util.AudioUtil.getLocations(values)
    return values.mapIndexed { index, value -> labels[index] to value }
}

/**
 * Display: themes and quiz screen appearance.
 */
private fun displaySettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> = listOf(
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
private fun dashboardSettingsGroups(): List<SettingGroup> = listOf(
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
private fun lessonReviewSettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> {
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
private fun subjectInfoSettingsGroups(): List<SettingGroup> {
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
private fun keyboardSettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> {
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
private fun fontSettingsGroups(onOpenCategory: (String, String?) -> Unit): List<SettingGroup> = listOf(
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
private fun otherSettingsGroups(context: Context): List<SettingGroup> {
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
private fun experimentalSettingsGroups(): List<SettingGroup> = listOf(
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
private fun otherActionsSettingsGroups(
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
private fun advancedLessonGroups(): List<SettingGroup> = listOf(
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
private fun advancedReviewGroups(): List<SettingGroup> = listOf(
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
private fun advancedSelfStudyGroups(): List<SettingGroup> = listOf(
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
private fun advancedOtherGroups(): List<SettingGroup> = listOf(
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
// Helpers for reading and writing settings.
//

private fun setPrefBoolean(value: Boolean, key: String, prefs: SharedPreferences) {
    prefs.edit { putBoolean(key, value) }
}

private fun setPrefString(value: String, key: String, prefs: SharedPreferences) {
    prefs.edit { putString(key, value) }
}

private fun setPrefInt(value: Int, key: String, prefs: SharedPreferences) {
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
private fun SettingsTopBar(
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

private val NETWORK_RULE_OPTIONS = listOf(
    Pair("Always allow sync and downloads", "ALWAYS"),
    Pair("Always allow sync, downloads only on unmetered connections (WiFi)", "DOWNLOAD_WIFI_ONLY"),
    Pair("Allow sync and downloads only on unmetered connections (WiFi)", "WIFI_ONLY"),
)

private val VOICE_OPTIONS = listOf(
    Pair("Random", "RANDOM"),
    Pair("Masculine", "MALE"),
    Pair("Feminine", "FEMALE"),
    Pair("Alternate between voices", "ALTERNATE"),
)

private val CLOSE_ENOUGH_OPTIONS = listOf(
    Pair("Accept silently", "SILENTLY_ACCEPT"),
    Pair("Accept and show a toast", "ACCEPT_WITH_TOAST"),
    Pair("Accept, show a toast, disable lightning mode for this question", "ACCEPT_WITH_TOAST_NO_LM"),
    Pair("Shake and retry", "SHAKE_AND_RETRY"),
    Pair("Reject", "REJECT"),
)

private val THEME_OPTIONS = listOf(
    Pair("Wanikani", "LIGHT"),
    Pair("Wanikani Dark", "DARK_WANIKANI"),
    Pair("Wanikani Black", "BLACK_WANIKANI"),
    Pair("Breeze", "DARK"),
    Pair("Black", "BLACK_BREEZE"),
    Pair("Nord", "NORD_POLAR_NIGHT"),
)

private val INFO_DUMP_OPTIONS = listOf(
    Pair("Show nothing", "NOTHING"),
    Pair("Show only answers", "ANSWERS_ONLY"),
    Pair("Hide information not quizzed yet", "HIDE_UNQUIZZED"),
    Pair("Show all information", "ALL"),
)

private val CHART_SIZE_OPTIONS = listOf(
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

private val REVIEW_ORDER_OPTIONS = listOf(
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

private val LESSON_ORDER_OPTIONS = listOf(
    Pair("Shuffle", "SHUFFLE"),
    Pair("Level, Type", "LEVEL_THEN_TYPE"),
    Pair("Radicals, Level, Type", "RADICALS_THEN_LEVEL_THEN_TYPE"),
    Pair("Type, Level", "TYPE_THEN_LEVEL"),
    Pair("Level, then shuffle", "LEVEL_THEN_SHUFFLE"),
    Pair("Type, then shuffle", "TYPE_THEN_SHUFFLE"),
)

private val SESSION_PRIORITY_OPTIONS = listOf(
    Pair("None", "NONE"),
    Pair("Radicals first", "RADICALS_FIRST"),
    Pair("Level-up items first", "LEVEL_UP_FIRST"),
    Pair("Current-level radicals and kanji first", "CURRENT_LEVEL_RADICAL_KANJI_FIRST"),
    Pair("Current-level items first", "CURRENT_LEVEL_FIRST"),
)

private val SPECIAL_BUTTON_OPTIONS = listOf(
    Pair("Disable", "DISABLE"),
    Pair("Ignore (undo and mark correct instead)", "UNDO_AND_MARK_CORRECT"),
    Pair("Undo and retry immediately", "UNDO_AND_RETRY_NOW"),
    Pair("Undo and put back into queue", "UNDO_AND_PUT_BACK"),
    Pair("Skip (put back into queue)", "SKIP"),
    Pair("Toggle lightning mode", "TOGGLE_LIGHTNING_MODE"),
)

private val SUBJECT_CARD_LAYOUT_OPTIONS = listOf(
    Pair("Normal", "NORMAL"),
    Pair("Compact with progression info", "COMPACT"),
    Pair("Compact without progression info", "COMPACT_NO_PROGRESSION"),
)

//
// About.
//

// About sections
enum class AboutSectionType {
    INFO,
    LINK,
    SUPPORT,
    OPEN_SOURCE,
    FONTS,
    INFLUENCES,
}

private const val ABOUT_URL_TAG = "URL"

/**
 * A text segment of an About paragraph: plain text, or a link.
 */
private data class AboutSegment(val text: String, val url: String? = null)

/**
 * Render a paragraph made of segments; link segments are tappable.
 */
@Composable
private fun aboutParagraph(segments: List<AboutSegment>) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary
    val text = buildAnnotatedString {
        for ((text1, url) in segments) {
            if (url == null) {
                append(text1)
            } else {
                pushStringAnnotation(ABOUT_URL_TAG, url)
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) {
                    append(text1)
                }
                pop()
            }
        }
    }
    ClickableText(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        onClick = { offset ->
            text.getStringAnnotations(ABOUT_URL_TAG, offset, offset)
                .firstOrNull()
                ?.let { uriHandler.openUri(it.item) }
        },
    )
}

/**
 * The About page, a redesign of the legacy AboutActivity content using
 * Material Expressive 3.
 */
@Composable
private fun AboutSettings() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // The welcome banner is an animated GIF; painterResource() cannot load
        // it (it casts the drawable to BitmapDrawable and crashes on API 28+),
        // so render it through Glide like the legacy AboutActivity does.
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                ImageView(context).apply {
                    adjustViewBounds = true
                    Glide.with(context)
                        .asGif()
                        .load(R.drawable.welcome)
                        .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(30, 0)))
                        .into(this)
                }
            },
            onRelease = { view ->
                try {
                    Glide.with(view).clear(view)
                } catch (ignored: IllegalArgumentException) {
                    // Glide refuses destroyed activities; it cleans up its own
                    // requests in that case anyway.
                }
            },
        )

        // App info card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Welcome to ${Identification.APP_NAME} ${BuildConfig.VERSION_NAME}!",
                    style = MaterialTheme.typography.titleLarge,
                )
                aboutParagraph(
                    listOf(
                        AboutSegment("${Identification.APP_NAME} is an app for "),
                        AboutSegment("WaniKani", "https://www.wanikani.com/"),
                        AboutSegment(", the kanji learning service created by "),
                        AboutSegment("Tofugu", "https://www.tofugu.com/"),
                        AboutSegment("."),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment("Are you new to WaniKani? Check out their "),
                        AboutSegment("knowledge base", "https://knowledge.wanikani.com/"),
                        AboutSegment(" for more information."),
                    ),
                )
            }
        }

        // Credits and development card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Credits",
                    style = MaterialTheme.typography.titleMedium,
                )
                aboutParagraph(
                    listOf(
                        AboutSegment(
                            "${Identification.APP_NAME} was re-coded by ",
                        ),
                        AboutSegment(
                            Identification.AUTHOR_NAME,
                            "https://github.com/${Identification.AUTHOR_USERNAME}",
                        ),
                        AboutSegment(" based on the original work of "),
                        AboutSegment(
                            "Ernst Jan Plugge",
                            "https://github.com/erjanp/OriginalWaniKaniAndroid",
                        ),
                        AboutSegment(" who developed the original code."),
                    ),
                )
            }
        }

        // Independence statement
        Text(
            text = "${Identification.APP_NAME} has been independently developed, and is not supported by, affiliated with, or endorsed by Tofugu.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        // Open Source components card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Open Source Components",
                    style = MaterialTheme.typography.titleMedium,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    aboutParagraph(
                        listOf(
                            AboutSegment(
                                "This app uses these Open Source components: ",
                            ),
                            AboutSegment(
                                "The Jackson Project",
                                "https://github.com/FasterXML/jackson",
                            ),
                            AboutSegment(", "),
                            AboutSegment(
                                "Lottie",
                                "https://airbnb.io/lottie/",
                            ),
                            AboutSegment(", "),
                            AboutSegment(
                                "JSoup",
                                "https://jsoup.org/",
                            ),
                            AboutSegment(", "),
                            AboutSegment(
                                "OkHttp",
                                "https://square.github.io/okhttp/",
                            ),
                            AboutSegment(", "),
                            AboutSegment(
                                "Pikolo color picker",
                                "https://github.com/Madrapps/Pikolo",
                            ),
                            AboutSegment(", and "),
                            AboutSegment(
                                "KanjiVG",
                                "https://github.com/KanjiVG/kanjivg",
                            ),
                        ),
                    )
                    aboutParagraph(
                        listOf(
                            AboutSegment(
                                "It was also heavily influenced by: ",
                            ),
                            AboutSegment(
                                "Tsurukame",
                                "https://community.wanikani.com/t/ios-tsurukame-native-app-with-offline-lessons-and-reviews/30635",
                            ),
                            AboutSegment(", and "),
                            AboutSegment(
                                "The WaniKani Developer Community",
                                "https://community.wanikani.com/c/wanikani/api-and-third-party-apps",
                            ),
                        ),
                    )
                }
            }
        }

        // Fonts card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Fonts",
                    style = MaterialTheme.typography.titleMedium,
                )
                aboutParagraph(
                    listOf(
                        AboutSegment(
                            "The following freely available fonts are included in unmodified form: ",
                        ),
                        AboutSegment(
                            "Sawarabi Mincho and Gothic, ",
                        ),
                        AboutSegment(
                            "(C) mshio",
                            "https://sawarabi-fonts.osdn.jp/en/",
                        ),
                        AboutSegment(
                            ", covered by the ",
                        ),
                        AboutSegment(
                            "CC Attribution V3.0",
                            "https://creativecommons.org/licenses/by/3.0/",
                        ),
                        AboutSegment(" license."),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment(
                            "M+ P Type-1, ",
                        ),
                        AboutSegment(
                            "(C) the M+ fonts project",
                            "https://mplus-fonts.osdn.jp/about-en.html",
                        ),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment("Kosugi, "),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment(
                            "created by ",
                        ),
                        AboutSegment(
                            "MOTOYA",
                            "https://fonts.google.com/specimen/Kosugi",
                        ),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment("Kosugi Maru, "),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment(
                            "created by ",
                        ),
                        AboutSegment(
                            "MOTOYA",
                            "https://fonts.google.com/specimen/Kosugi+Maru",
                        ),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment("Otsutome, "),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment(
                            "https://rooms.webcrow.jp/font/index.html",
                            "https://rooms.webcrow.jp/font/index.html",
                        ),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment("851 Gochikakutto, "),
                    ),
                )
                aboutParagraph(
                    listOf(
                        AboutSegment(
                            "https://pm85122.onamae.jp/851Gkktt.html",
                            "https://pm85122.onamae.jp/851Gkktt.html",
                        ),
                    ),
                )
            }
        }
    }
}



