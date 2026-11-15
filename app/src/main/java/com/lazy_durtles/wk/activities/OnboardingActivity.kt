package com.lazy_durtles.wk.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lazy_durtles.wk.BuildConfig
import com.lazy_durtles.wk.Constants
import com.lazy_durtles.wk.GlobalSettings
import com.lazy_durtles.wk.Identification
import com.lazy_durtles.wk.jobs.StartMnemonicImageDownloadJob
import com.lazy_durtles.wk.services.JobRunnerService
import com.lazy_durtles.wk.ui.icons.MaterialSymbolIcon
import com.lazy_durtles.wk.ui.icons.MaterialSymbolsIcons
import com.lazy_durtles.wk.ui.screens.settings.SettingsAlertDialog
import com.lazy_durtles.wk.ui.theme.LazyDurtlesTheme
import com.lazy_durtles.wk.util.Logger
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * First-run onboarding. Runs once after install (gated by
 * [GlobalSettings.Onboarding.getCompleted]) and walks the user through:
 * 1. entering and validating the V2 API token,
 * 2. whether to download WaniKani's mnemonic background images,
 * 3. how the keyboard works by default (continue or adjust settings),
 * 4. the notification permission.
 * After the last step the user lands on the main overview activity.
 */
class OnboardingActivity : ComponentActivity() {
    private val logger = Logger.get(OnboardingActivity::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LazyDurtlesTheme {
                OnboardingScreen(
                    onFinished = {
                        GlobalSettings.Onboarding.setCompleted(true)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}

/**
 * The steps of the onboarding flow, in order.
 */
private enum class OnboardingStep {
    API_TOKEN,
    MNEMONIC_IMAGES,
    KEYBOARD,
    NOTIFICATIONS,
}

@Composable
private fun OnboardingScreen(
    onFinished: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(OnboardingStep.API_TOKEN) }

    fun goBack() {
        if (step.ordinal > 0) {
            step = OnboardingStep.entries[step.ordinal - 1]
        }
    }

    // The keyboard step can jump out to the system keyboard settings; when the
    // user comes back we simply stay on the step so they can continue.
    BackHandler(enabled = step != OnboardingStep.API_TOKEN) {
        goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding(),
    ) {
        StepIndicator(
            current = step.ordinal + 1,
            total = OnboardingStep.entries.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )
        when (step) {
            OnboardingStep.API_TOKEN -> ApiTokenStep(
                onValidated = { step = OnboardingStep.MNEMONIC_IMAGES },
            )
            OnboardingStep.MNEMONIC_IMAGES -> MnemonicImagesStep(
                onAnswered = { step = OnboardingStep.KEYBOARD },
                onBack = ::goBack,
            )
            OnboardingStep.KEYBOARD -> KeyboardStep(
                onContinue = { step = OnboardingStep.NOTIFICATIONS },
                onBack = ::goBack,
            )
            OnboardingStep.NOTIFICATIONS -> NotificationsStep(
                onDone = onFinished,
                onBack = ::goBack,
            )
        }
    }
}

@Composable
private fun StepIndicator(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index < current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

/**
 * Shared step layout: a scrollable content area with a large icon and title on
 * top, and a pinned bottom action bar (thumb-reachable) holding the primary
 * actions and an optional Back button that returns to the previous question.
 */
@Composable
private fun OnboardingStepScaffold(
    icon: Char,
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                MaterialSymbolIcon(
                    icon = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 24.dp),
            )
            Spacer(Modifier.height(16.dp))
            content()
            Spacer(Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            actions()
            if (onBack != null) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    MaterialSymbolIcon(
                        icon = MaterialSymbolsIcons.ArrowBack,
                        contentDescription = "Back to the previous question",
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = "Back")
                }
            }
        }
    }
}

/**
 * Step 1: enter the V2 API token. The token is validated against
 * `GET /v2/user` before it is saved; a rejected token shows an inline error
 * and keeps the user on this step.
 */
@Composable
private fun ApiTokenStep(
    onValidated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var fieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    val focusManager = LocalFocusManager.current
    val token = fieldValue.text.trim()
    var validating by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showHelp by rememberSaveable { mutableStateOf(false) }

    fun save() {
        if (token.isEmpty() || validating) {
            return
        }
        focusManager.clearFocus()
        validating = true
        error = null
        Thread {
            val result = validateApiToken(token)
            validating = false
            when (result) {
                TokenValidationResult.VALID -> {
                    GlobalSettings.Api.setApiKey(token)
                    onValidated()
                }
                TokenValidationResult.REJECTED -> {
                    error = "WaniKani is not accepting this token. Did you give it the correct permissions?"
                }
                TokenValidationResult.NETWORK_ERROR -> {
                    error = "Could not reach the WaniKani servers. Check your connection and try again."
                }
            }
        }.start()
    }

    OnboardingStepScaffold(
        icon = MaterialSymbolsIcons.Key,
        title = "Enter your API token",
        onBack = null,
        modifier = modifier,
        content = {
            OutlinedTextField(
                value = fieldValue,
                onValueChange = {
                    fieldValue = it
                    error = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        if (state.isFocused && fieldValue.text.isNotEmpty()) {
                            fieldValue = fieldValue.copy(
                                selection = androidx.compose.ui.text.TextRange(0, fieldValue.text.length),
                            )
                        }
                    },
                placeholder = { Text(text = "Your V2 API token") },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(text = it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Unspecified,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { save() }),
            )
            Text(
                text = "Paste the token from your WaniKani settings. It is checked against the WaniKani servers before we continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(
                onClick = { showHelp = true },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(text = "How do I get a token?")
            }
        },
        actions = {
            Button(
                onClick = { save() },
                enabled = token.isNotEmpty() && !validating,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                if (validating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = "Checking token...")
                } else {
                    Text(text = "Continue")
                }
            }
        },
    )

    if (showHelp) {
        SettingsAlertDialog(
            title = "Getting a token",
            html = Constants.NO_API_KEY_HELP_DOCUMENT,
            icon = MaterialSymbolsIcons.Help,
            confirmLabel = "Got it",
            onConfirm = { showHelp = false },
            scrollable = true,
        )
    }
}

private enum class TokenValidationResult {
    VALID,
    REJECTED,
    NETWORK_ERROR,
}

/**
 * Validate an API token with a single `GET /v2/user` call. Runs on a
 * background thread; must not touch the database.
 */
private fun validateApiToken(token: String): TokenValidationResult {
    var connection: HttpsURLConnection? = null
    return try {
        val url = URL("https://api.wanikani.com/v2/user")
        connection = (url.openConnection() as HttpsURLConnection).apply {
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Wanikani-Revision", Constants.API_VERSION)
            setRequestProperty("User-Agent", Identification.APP_NAME_UA + "/" + BuildConfig.VERSION_NAME)
            requestMethod = "GET"
            doInput = true
            doOutput = false
            connectTimeout = 10_000
            readTimeout = 30_000
        }
        val code = connection.responseCode
        when (code) {
            HttpURLConnection.HTTP_OK -> TokenValidationResult.VALID
            HttpURLConnection.HTTP_UNAUTHORIZED -> TokenValidationResult.REJECTED
            else -> TokenValidationResult.NETWORK_ERROR
        }
    } catch (e: Exception) {
        TokenValidationResult.NETWORK_ERROR
    } finally {
        connection?.disconnect()
    }
}

/**
 * Step 2: whether to download WaniKani's mnemonic background images. Shown as
 * two answer cards pinned at the bottom. The drawings are copyright
 * WaniKani / Tofugu.
 */
@Composable
private fun MnemonicImagesStep(
    onAnswered: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun answer(download: Boolean) {
        GlobalSettings.MnemonicImages.setOnboardingAsked(true)
        if (download) {
            GlobalSettings.MnemonicImages.setEnabled(true)
            JobRunnerService.schedule(StartMnemonicImageDownloadJob::class.java, "")
        }
        onAnswered()
    }

    OnboardingStepScaffold(
        icon = MaterialSymbolsIcons.AutoStories,
        title = "Mnemonic images",
        onBack = onBack,
        modifier = modifier,
        content = {
            Text(
                text = "Download WaniKani's mnemonic background images to show them in sessions?",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Drawings are copyright WaniKani / Tofugu, not the author of this app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        },
        actions = {
            AnswerCard(
                icon = MaterialSymbolsIcons.DownloadForOffline,
                title = "Yes, download",
                description = "Show the mnemonic drawings behind the question during sessions.",
                onClick = { answer(true) },
            )
            AnswerCard(
                icon = MaterialSymbolsIcons.CloudOff,
                title = "Not now",
                description = "You can enable this later in the settings.",
                onClick = { answer(false) },
            )
        },
    )
}

@Composable
private fun AnswerCard(
    icon: Char,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                MaterialSymbolIcon(
                    icon = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Step 3: explains how the keyboard works by default. Two buttons pinned at
 * the bottom: continue with the defaults, or adjust the keyboard settings
 * first (opens the system keyboard picker / settings).
 */
@Composable
private fun KeyboardStep(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    fun finish() {
        GlobalSettings.Tutorials.setKeyboardHelpDismissed(true)
        onContinue()
    }

    OnboardingStepScaffold(
        icon = MaterialSymbolsIcons.Keyboard,
        title = "Your keyboard",
        onBack = onBack,
        modifier = modifier,
        content = {
            Text(
                text = "By default the app works with any keyboard: for reading questions you type romaji " +
                    "(for example \"konnichiwa\") and the app converts it to kana (こんにちは) for you. " +
                    "For meaning questions an ASCII keyboard is requested.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "If your keyboard gives you inappropriate hints or auto-fills text, you can adjust " +
                    "the keyboard settings of your device and of this app.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        },
        actions = {
            Button(
                onClick = { finish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(text = "Continue")
            }
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                MaterialSymbolIcon(
                    icon = MaterialSymbolsIcons.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(text = "Adjust keyboard settings")
            }
        },
    )
}

/**
 * Step 4: ask for the notification permission (Android 13+). On older versions
 * this step just confirms and finishes.
 */
@Composable
private fun NotificationsStep(
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        GlobalSettings.Tutorials.setNotificationPermissionAsked(true)
        onDone()
    }

    OnboardingStepScaffold(
        icon = MaterialSymbolsIcons.NotificationsActive,
        title = "Notifications",
        onBack = onBack,
        modifier = modifier,
        content = {
            Text(
                text = if (needsPermission) {
                    "Allow notifications so the app can remind you when new reviews are available?"
                } else {
                    "You're all set. The app can remind you of reviews with notifications."
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        },
        actions = {
            Button(
                onClick = {
                    if (needsPermission) {
                        GlobalSettings.Tutorials.setNotificationPermissionAsked(true)
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onDone()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(text = if (needsPermission) "Allow notifications" else "Continue")
            }
            if (needsPermission) {
                TextButton(
                    onClick = {
                        GlobalSettings.Tutorials.setNotificationPermissionAsked(true)
                        onDone()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text(text = "Not now")
                }
            }
        },
    )
}
