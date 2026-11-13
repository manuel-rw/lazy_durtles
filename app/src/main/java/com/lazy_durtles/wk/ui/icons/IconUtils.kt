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
    val Error = '\uE000'
    val Album = '\uE019'
    val Equalizer = '\uE01D'
    val Mic = '\uE029'
    val LibraryMusic = '\uE030'
    val Pause = '\uE034'
    val PlayArrow = '\uE037'
    val Stop = '\uE047'
    val VolumeDown = '\uE04D'
    val VolumeMute = '\uE04E'
    val VolumeOff = '\uE04F'
    val VolumeUp = '\uE050'
    val PlaylistPlay = '\uE05F'
    val VpnKey = '\uE0DA'
    val Lightbulb = '\uE0F0'
    val Archive = '\uE149'
    val Backspace = '\uE14A'
    val FilterList = '\uE152'
    val Flag = '\uE153'
    val Link = '\uE157'
    val DoNotDisturbOn = '\uE15C'
    val Save = '\uE161'
    val Sort = '\uE164'
    val TextFormat = '\uE165'
    val FontDownload = '\uE167'
    val LowPriority = '\uE16D'
    val Download = '\uE171'
    val Schedule = '\uE192'
    val Inventory2 = '\uE1A1'
    val DataUsage = '\uE1AF'
    val Devices = '\uE1B1'
    val GraphicEq = '\uE1B8'
    val Widgets = '\uE1BD'
    val SdCard = '\uE1C2'
    val Storage = '\uE1DB'
    val FormatAlignLeft = '\uE236'
    val FormatColorFill = '\uE23A'
    val FormatSize = '\uE245'
    val SpaceBar = '\uE256'
    val TextFields = '\uE262'
    val Title = '\uE264'
    val TableChart = '\uE265'
    val BarChart = '\uE26B'
    val Notes = '\uE26C'
    val History = '\uE28E'
    val CloudDownload = '\uE2C0'
    val CloudOff = '\uE2C1'
    val CloudUpload = '\uE2C3'
    val FolderOpen = '\uE2C8'
    val TaskAlt = '\uE2E6'
    val Headphones = '\uE310'
    val Keyboard = '\uE312'
    val KeyboardArrowDown = '\uE313'
    val KeyboardArrowUp = '\uE316'
    val Speaker = '\uE32D'
    val TabletAndroid = '\uE330'
    val MusicNote = '\uE3A1'
    val Brush = '\uE3AE'
    val Palette = '\uE3B7'
    val CropFree = '\uE3C2'
    val FlashOn = '\uE3E7'
    val AccountBox = '\uE416'
    val Visibility = '\uE417'
    val Straighten = '\uE41C'
    val Style = '\uE41D'
    val Timer = '\uE425'
    val Tune = '\uE429'
    val PhotoSizeSelectLarge = '\uE433'
    val QueryStats = '\uE4FC'
    val LightMode = '\uE518'
    val DarkMode = '\uE51C'
    val Category = '\uE574'
    val Apps = '\uE5C3'
    val ArrowBack = '\uE5C4'
    val ArrowDropDown = '\uE5C5'
    val ArrowForward = '\uE5C8'
    val Cancel = '\uE5C9'
    val ChevronRight = '\uE5CC'
    val Close = '\uE5CD'
    val ExpandLess = '\uE5CE'
    val ExpandMore = '\uE5CF'
    val Fullscreen = '\uE5D0'
    val MoreVert = '\uE5D4'
    val Refresh = '\uE5D5'
    val Sync = '\uE627'
    val PriorityHigh = '\uE645'
    val AutoAwesome = '\uE65F'
    val AutoStories = '\uE666'
    val Check = '\uE668'
    val SpaceDashboard = '\uE66B'
    val Checklist = '\uE6B1'
    val ShowChart = '\uE6E1'
    val Animation = '\uE71C'
    val Stars = '\uE739'
    val Key = '\uE73C'
    val EditNote = '\uE745'
    val Draw = '\uE746'
    val Shield = '\uE75B'
    val TipsAndUpdates = '\uE79A'
    val Notifications = '\uE7F5'
    val NotificationsActive = '\uE7F7'
    val School = '\uE80C'
    val Android = '\uE859'
    val AspectRatio = '\uE85B'
    val Assignment = '\uE85D'
    val Backup = '\uE864'
    val BugReport = '\uE868'
    val Description = '\uE873'
    val Done = '\uE876'
    val Info = '\uE88E'
    val Language = '\uE894'
    val OpenInNew = '\uE895'
    val List = '\uE896'
    val LockOpen = '\uE898'
    val Lock = '\uE899'
    val Settings = '\uE8B8'
    val SettingsApplications = '\uE8B9'
    val SettingsInputComponent = '\uE8C0'
    val SettingsRemote = '\uE8C7'
    val Spellcheck = '\uE8CE'
    val Subject = '\uE8D2'
    val TrendingDown = '\uE8E3'
    val TrendingUp = '\uE8E5'
    val ViewList = '\uE8EF'
    val VisibilityOff = '\uE8F5'
    val Help = '\uE8FD'
    val Fingerprint = '\uE90D'
    val TouchApp = '\uE913'
    val DateRange = '\uE916'
    val DonutLarge = '\uE917'
    val RecordVoiceOver = '\uE91F'
    val Timeline = '\uE922'
    val PanTool = '\uE925'
    val DeleteForever = '\uE92B'
    val Delete = '\uE92E'
    val DashboardCustomize = '\uE99B'
    val GridView = '\uE9B0'
    val Home = '\uE9B2'
    val MotionPhotosOn = '\uE9C1'
    val Speed = '\uE9E4'
    val Swipe = '\uE9EC'
    val Bolt = '\uEA0B'
    val Height = '\uEA16'
    val SyncAlt = '\uEA18'
    val MenuBook = '\uEA19'
    val Construction = '\uEA3C'
    val Engineering = '\uEA3D'
    val Psychology = '\uEA4A'
    val Science = '\uEA4B'
    val Badge = '\uEA67'
    val Login = '\uEA77'
    val TextDecrease = '\uEADD'
    val TextIncrease = '\uEAE2'
    val Contrast = '\uEB37'
    val CloudSync = '\uEB5A'
    val AudioFile = '\uEB82'
    val AdminPanelSettings = '\uEF3D'
    val Analytics = '\uEF3E'
    val Article = '\uEF42'
    val Campaign = '\uEF49'
    val ReceiptLong = '\uEF6E'
    val Search = '\uEF7A'
    val BatterySaver = '\uEFDE'
    val DownloadForOffline = '\uF000'
    val ManageAccounts = '\uF02E'
    val Password = '\uF042'
    val Quiz = '\uF04C'
    val SmartToy = '\uF06C'
    val Warning = '\uF083'
    val Insights = '\uF092'
    val Edit = '\uF097'
    val Star = '\uF09A'
    val CheckCircle = '\uF0BE'
    val FactCheck = '\uF0C5'
    val Person = '\uF0D3'
    val Handyman = '\uF10B'
    val Rule = '\uF1C2'
    val OpenInFull = '\uF1CE'
    val CloseFullscreen = '\uF1CF'
    val AccountCircle = '\uF20B'
    val Build = '\uF8CD'
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
