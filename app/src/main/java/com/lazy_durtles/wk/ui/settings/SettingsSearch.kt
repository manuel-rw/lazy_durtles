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

@Composable
internal fun SettingsSearchField(
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
internal class SearchEntry(
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
internal fun rememberSearchIndex(): List<SearchEntry> {
    val context = LocalContext.current
    return remember(context) {
        val entries = mutableListOf<SearchEntry>()
        for (group in CATEGORY_GROUPS) {
            for (category in group.categories) {
                val title = if (category.id == "api") apiCategoryTitle() else category.title
                entries.add(
                    SearchEntry(
                        key = null,
                        title = title,
                        summary = category.description,
                        breadcrumb = "Settings",
                        categoryId = category.id,
                        icon = category.icon,
                    ),
                )
                for (defGroup in categoryDefs(category.id, context, { _, _ -> }, { }, { true })) {
                    for (def in defGroup.items) {
                        val breadcrumb = if (defGroup.header != null) {
                            "$title › ${defGroup.header}"
                        } else {
                            title
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
internal fun SearchResults(
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
internal fun SearchResultRow(
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
internal fun CategoryRow(
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
            Text(
                text = if (category.id == "api") apiCategoryTitle() else category.title,
                style = MaterialTheme.typography.bodyLarge,
            )
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
