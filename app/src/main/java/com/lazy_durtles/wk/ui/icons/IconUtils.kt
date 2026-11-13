package com.lazy_durtles.wk.ui.icons

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.lazy_durtles.wk.R
import dev.tclement.fonticons.FontIcon
import dev.tclement.fonticons.IconFont
import dev.tclement.fonticons.rememberVariableIconFont

/**
 * Material Symbols Rounded icon codepoints.
 *
 * The icons are rendered from the variable font at res/font/material_symbols_rounded.ttf
 * (subset to these codepoints) using the FontIcon composable from compose-font-icons.
 * Filled and outlined rendering are two settings of the same font's FILL axis, so a
 * codepoint alone does not fix the variant; pass filled = false for outlined icons.
 */
object MaterialSymbolsIcons {
    val ArrowBack = '\uE5C4'
    val ArrowForward = '\uE5C8'
    val ArrowDropDown = '\uE5C5'
    val AccountCircle = '\uF20B'
    val Build = '\uF8CD'
    val Check = '\uE668'
    val CheckCircle = '\uF0BE'
    val ChevronRight = '\uE5CC'
    val Close = '\uE5CD'
    val Create = '\uF097'
    val DateRange = '\uE916'
    val Delete = '\uE92E'
    val Done = '\uE876'
    val Edit = '\uF097'
    val Help = '\uE8FD'
    val Home = '\uE9B2'
    val Info = '\uE88E'
    val KeyboardArrowDown = '\uE313'
    val KeyboardArrowUp = '\uE316'
    val List = '\uE896'
    val Lock = '\uE899'
    val MoreVert = '\uE5D4'
    val Notifications = '\uE7F5'
    val Person = '\uF0D3'
    val PlayArrow = '\uE037'
    val Refresh = '\uE5D5'
    val Search = '\uEF7A'
    val Settings = '\uE8B8'
    val Star = '\uF09A'
    val Sync = '\uE627'
    val Warning = '\uF083'
}

val LocalFilledSymbolsFont = staticCompositionLocalOf<IconFont> {
    error("Material Symbols font not provided; wrap content in ProvideMaterialSymbolsIcons")
}

val LocalOutlinedSymbolsFont = staticCompositionLocalOf<IconFont> {
    error("Material Symbols font not provided; wrap content in ProvideMaterialSymbolsIcons")
}

/**
 * Creates the Material Symbols Rounded variable icon font with the FILL axis fixed
 * to the requested variant. The weight axis is set per icon by the library.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun rememberMaterialSymbolsFont(filled: Boolean): IconFont = if (filled) {
    rememberVariableIconFont(
        resId = R.font.material_symbols_rounded,
        weights = arrayOf(
            FontWeight.Thin, FontWeight.Light, FontWeight.Normal,
            FontWeight.Medium, FontWeight.Bold, FontWeight.Black,
        ),
        fontVariationSettings = FontVariation.Settings(FontVariation.Setting("FILL", 1f)),
    )
} else {
    rememberVariableIconFont(
        resId = R.font.material_symbols_rounded,
        weights = arrayOf(
            FontWeight.Thin, FontWeight.Light, FontWeight.Normal,
            FontWeight.Medium, FontWeight.Bold, FontWeight.Black,
        ),
        fontVariationSettings = FontVariation.Settings(FontVariation.Setting("FILL", 0f)),
    )
}

/**
 * Provides the Material Symbols font to MaterialSymbolIcon composables.
 * Wrap the application content once, e.g. inside the theme.
 */
@Composable
fun ProvideMaterialSymbolsIcons(content: @Composable () -> Unit) {
    val filledFont = rememberMaterialSymbolsFont(filled = true)
    val outlinedFont = rememberMaterialSymbolsFont(filled = false)
    androidx.compose.runtime.CompositionLocalProvider(
        LocalFilledSymbolsFont provides filledFont,
        LocalOutlinedSymbolsFont provides outlinedFont,
        content = content,
    )
}

/**
 * Displays a Material Symbols Rounded icon, mirroring the Icon composable's API.
 *
 * On Android versions below Oreo (API 26) variable font axes are ignored and icons
 * render with the font's default instance.
 */
@Composable
fun MaterialSymbolIcon(
    icon: Char,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    tint: Color = LocalContentColor.current,
) {
    val font = if (filled) LocalFilledSymbolsFont.current else LocalOutlinedSymbolsFont.current
    FontIcon(
        icon = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
        iconFont = font,
    )
}
