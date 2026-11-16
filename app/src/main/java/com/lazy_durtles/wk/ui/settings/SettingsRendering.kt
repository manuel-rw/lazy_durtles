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
import androidx.compose.material3.LinearProgressIndicator
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.lazy_durtles.wk.livedata.LiveMnemonicDownloadProgress
import com.lazy_durtles.wk.livedata.LiveMnemonicImageDownloadQueued
import com.lazy_durtles.wk.livedata.LiveMnemonicImageFileCount
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

// Rendering of declarative settings.
//

/**
 * The default shared preferences, plus a version counter that increments on
 * every settings change. Reading the counter's value during composition makes
 * the reading scope recompose with fresh values.
 */
@Composable
internal fun rememberObservedPrefs(): Pair<SharedPreferences, MutableIntState> {
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
internal fun SettingGroups(
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
internal fun SettingRow(
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
            is ProgressDef -> ProgressRowContent(def)
            is StatusDef -> StatusRowContent(def)
            is NumberRangeDef -> NumberRangeRowContent(def, prefs)
            is SearchEngineDef -> SearchEngineRowContent(def, prefs)
        }
    }
}

/**
 * A boolean setting rendered as a switch.
 */
@Composable
internal fun SwitchRowContent(def: SwitchDef, prefs: SharedPreferences) {
    var confirmEnable by remember { mutableStateOf(false) }
    val checked = prefs.getBoolean(def.prefKey, def.default)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!checked && def.confirmTitle != null) {
                    confirmEnable = true
                } else {
                    val newValue = !checked
                    prefs.edit { putBoolean(def.prefKey, newValue) }
                    def.onToggled?.invoke(newValue)
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
                    def.onToggled?.invoke(newValue)
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
                def.onToggled?.invoke(true)
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
internal fun SelectRowContent(def: SelectDef, prefs: SharedPreferences) {
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
internal fun SliderRowContent(def: SliderDef, prefs: SharedPreferences) {
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
internal fun TextFieldRowContent(def: TextFieldDef, prefs: SharedPreferences) {
    var editing by remember { mutableStateOf(false) }
    val value = def.read?.invoke(prefs)
        ?: (prefs.getString(def.prefKey, def.default) ?: def.default)

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
                    val write = def.write
                    if (write != null) {
                        write(text, prefs)
                    } else {
                        prefs.edit { putString(def.prefKey, text) }
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
 * A clickable row that performs an action, optionally behind a confirmation
 * dialog.
 */
@Composable
internal fun ActionRowContent(def: ActionDef) {
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
internal fun InfoRowContent(def: InfoDef) {
    HtmlNoticeBanner(
        html = def.html,
        title = def.title,
        icon = def.icon,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

/**
 * A non-interactive progress row for the mnemonic image download. Shows an
 * indeterminate bar while preparing and a determinate bar with "done / total"
 * while downloading. Hidden when no download is queued or running.
 */
@Composable
internal fun ProgressRowContent(def: ProgressDef) {
    val queued by LiveMnemonicImageDownloadQueued.getInstance().observeAsState(initial = 0)
    val progress by LiveMnemonicDownloadProgress.getInstance().observeAsState()
    if (queued <= 0 && progress == null) {
        return
    }
    val p = progress
    val fraction = if (p != null && !p.indeterminate && p.total > 0) {
        p.done.toFloat() / p.total.toFloat()
    } else {
        0f
    }
    val label = if (p == null || p.indeterminate) {
        "Preparing download..."
    } else {
        String.format(Locale.ROOT, "%d / %d images", p.done, p.total)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = def.title, style = MaterialTheme.typography.bodyLarge)
        if (p == null || p.indeterminate) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        } else {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A non-interactive status row showing the number of mnemonic image files
 * currently stored on this device.
 */
@Composable
internal fun StatusRowContent(def: StatusDef) {
    val count by LiveMnemonicImageFileCount.getInstance().observeAsState(initial = null)
    val c = count ?: LiveMnemonicImageFileCount.getInstance().get()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = def.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = String.format(Locale.ROOT, "%d images stored on this device", c),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A subject-selection range: a minimum and maximum value, each of which may be
 * empty. -1 means "not constrained". A port of the legacy NumberRangePreference.
 */
@Composable
internal fun NumberRangeRowContent(def: NumberRangeDef, prefs: SharedPreferences) {
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
internal fun SearchEngineRowContent(def: SearchEngineDef, prefs: SharedPreferences) {
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
internal fun DefCategoryScreen(
    categoryId: String,
    highlight: String?,
    onBack: () -> Unit,
    onOpenCategory: (String, String?) -> Unit,
    onGoHome: () -> Unit,
) {
    val context = LocalContext.current
    val apiState by LiveApiState.getInstance().observeAsState(initial = ApiState.UNKNOWN)

    // Keep the locally downloaded mnemonic image count fresh while this screen
    // is open; the count lives on disk, not in the database.
    LaunchedEffect(categoryId) {
        if (categoryId == "mnemonic_images") {
            withContext(Dispatchers.IO) {
                LiveMnemonicImageFileCount.getInstance().update()
            }
        }
    }

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
