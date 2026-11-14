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

internal const val ABOUT_URL_TAG = "URL"

/**
 * A text segment of an About paragraph: plain text, or a link.
 */
internal data class AboutSegment(val text: String, val url: String? = null)

/**
 * Render a paragraph made of segments; link segments are tappable.
 */
@Composable
internal fun aboutParagraph(segments: List<AboutSegment>) {
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
 * The About category screen: the About page content in the shared category
 * scaffold.
 */
@Composable
internal fun AboutCategoryScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            SettingsTopBar(title = "About", onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            AboutSettings()
        }
    }
}

/**
 * The About page, a redesign of the legacy AboutActivity content using
 * Material Expressive 3.
 */
@Composable
internal fun AboutSettings() {
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




