package com.lazy_durtles.wk.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lazy_durtles.wk.ui.icons.MaterialSymbolIcon
import com.lazy_durtles.wk.ui.icons.MaterialSymbolsIcons

/**
 * Shared Material Expressive 3 building blocks for the settings screens: grouped
 * cards, tonal icon tiles, and the expressive dialogs that replace the default
 * Material alerts across all settings pages.
 */

/** Corner radii used across the expressive settings components. */
val SettingsGroupShape = RoundedCornerShape(24.dp)
val SettingsDialogShape = RoundedCornerShape(28.dp)
val SettingHighlightShape = RoundedCornerShape(16.dp)

/**
 * An icon inside a circular tonal container, used for category rows and card
 * headers. The icon is either a Material Symbols codepoint (Char) or a
 * drawable resource ID (Int).
 */
@Composable
fun TonalIcon(
    icon: Any?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        when (icon) {
            is Char -> MaterialSymbolIcon(
                icon = icon,
                contentDescription = contentDescription,
                tint = contentColor,
            )
            is Int -> Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                tint = contentColor,
            )
        }
    }
}

/**
 * A section header shown above a [SettingsGroupCard].
 */
@Composable
fun SettingsGroupHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 28.dp, top = 20.dp, bottom = 8.dp),
    )
}

/**
 * The rounded card that groups a set of related settings, in the Material
 * Expressive style: a large corner radius, a tonal surface and no elevation.
 */
@Composable
fun SettingsGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SettingsGroupShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

/**
 * An informational notice rendered as a tonal banner with an icon, replacing
 * the old plain-text info preference blocks.
 */
@Composable
fun HtmlNoticeBanner(
    html: String,
    modifier: Modifier = Modifier,
    title: String = "",
    icon: Any? = MaterialSymbolsIcons.Info,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SettingsGroupShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TonalIcon(
                icon = icon,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 2.dp),
            )
            Column {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                HtmlNoticeText(html = html)
            }
        }
    }
}

/**
 * The Material Expressive alert dialog used by all settings screens: a large
 * corner radius, an optional icon in a tonal circle, and clearly styled
 * confirm/dismiss buttons. Destructive confirms are tinted with the error
 * color.
 */
@Composable
fun SettingsAlertDialog(
    title: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    html: String? = null,
    icon: Any? = null,
    confirmLabel: String = "OK",
    dismissLabel: String? = "Cancel",
    onDismiss: (() -> Unit)? = null,
    destructive: Boolean = false,
    scrollable: Boolean = false,
) {
    val confirmColor = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        modifier = modifier,
        shape = SettingsDialogShape,
        icon = if (icon == null) {
            null
        } else {
            {
                TonalIcon(
                    icon = icon,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            val body: @Composable () -> Unit = {
                if (html != null) {
                    HtmlNoticeText(html = html)
                } else if (text != null) {
                    Text(text = text, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (scrollable) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    body()
                }
            } else {
                body()
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmLabel, color = confirmColor)
            }
        },
        dismissButton = if (dismissLabel != null) {
            {
                TextButton(onClick = { onDismiss?.invoke() }) {
                    Text(text = dismissLabel)
                }
            }
        } else {
            null
        },
    )
}

/**
 * A select dialog for settings with a fixed set of options: a radio button
 * list in an expressive dialog. Options are (label, value) pairs.
 */
@Composable
fun SettingsSelectDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SettingsDialogShape,
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(SettingHighlightShape)
                            .clickable {
                                onSelect(value)
                                onDismiss()
                            }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = value == selectedValue,
                            onClick = {
                                onSelect(value)
                                onDismiss()
                            },
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

/**
 * A thin divider between rows inside a group card.
 */
@Composable
fun SettingsRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    )
}

/**
 * The animated background tint for a highlighted setting row (used when
 * arriving from settings search).
 */
@Composable
fun highlightContainerColor(highlighted: Boolean): Color {
    val color by animateColorAsState(
        targetValue = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        label = "settingsHighlight",
    )
    return color
}
