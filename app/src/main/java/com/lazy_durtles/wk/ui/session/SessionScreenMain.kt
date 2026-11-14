package com.lazy_durtles.wk.ui.session

import android.text.SpannableStringBuilder
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import com.lazy_durtles.wk.ui.icons.MaterialSymbolsIcons
import com.lazy_durtles.wk.ui.icons.MaterialSymbolIcon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lazy_durtles.wk.Constants
import com.lazy_durtles.wk.GlobalSettings
import com.lazy_durtles.wk.R
import com.lazy_durtles.wk.WkApplication
import com.lazy_durtles.wk.db.model.Subject
import com.lazy_durtles.wk.enums.CloseEnoughAction
import com.lazy_durtles.wk.enums.SessionType
import com.lazy_durtles.wk.model.FloatingUiState
import com.lazy_durtles.wk.model.Question
import com.lazy_durtles.wk.model.Session
import com.lazy_durtles.wk.ui.common.BulkOpDialog
import com.lazy_durtles.wk.ui.common.BulkOpState
import com.lazy_durtles.wk.ui.common.BulkOpType
import com.lazy_durtles.wk.ui.common.runBulkOp
import com.lazy_durtles.wk.ui.screens.session.AnkiQuestionScreen
import com.lazy_durtles.wk.ui.screens.session.AutoDismissMessage
import com.lazy_durtles.wk.ui.screens.session.LessonPresentationScreen
import com.lazy_durtles.wk.ui.screens.session.SrsIndicatorChip
import com.lazy_durtles.wk.ui.screens.session.SrsStageChange
import com.lazy_durtles.wk.ui.screens.session.SessionProgressHeader
import com.lazy_durtles.wk.ui.screens.session.SessionScaffold
import com.lazy_durtles.wk.ui.screens.session.SessionSummaryScreen
import com.lazy_durtles.wk.ui.screens.session.StarRatingRow
import com.lazy_durtles.wk.ui.screens.session.SpecialButtonsRow
import com.lazy_durtles.wk.ui.screens.session.SlideTransitionBox
import com.lazy_durtles.wk.ui.screens.session.SubjectInfoCard
import com.lazy_durtles.wk.ui.screens.session.SubjectInfoContainer
import com.lazy_durtles.wk.ui.screens.session.captureSrsStageChange
import com.lazy_durtles.wk.ui.screens.session.rememberSessionHaptics
import com.lazy_durtles.wk.ui.screens.settings.HtmlNoticeText
import com.lazy_durtles.wk.ui.screens.session.sessionSwipes
import com.lazy_durtles.wk.util.AudioUtil
import com.lazy_durtles.wk.util.PseudoIme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SessionScreen(
    onFinishSession: () -> Unit,
    modifier: Modifier = Modifier,
    onViewSubject: (subjectId: Long, subjectIds: List<Long>) -> Unit = { _, _ -> },
    onBackPressed: () -> Unit = { },
) {
    val session = remember { Session.getInstance() }
    val haptics = rememberSessionHaptics()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    var uiTick by remember { mutableIntStateOf(0) }
    var currentSubject by remember { mutableStateOf<Subject?>(null) }
    var matchingKanji by remember { mutableStateOf<Subject?>(null) }
    var canReplayAudio by remember { mutableStateOf(false) }
    var answerField by remember { mutableStateOf(TextFieldValue("")) }
    var closeMessage by remember { mutableStateOf<String?>(null) }
    var alternativesMessage by remember { mutableStateOf<String?>(null) }
    var digraphMessage by remember { mutableStateOf<String?>(null) }
    var showDigraphHelp by remember { mutableStateOf(false) }
    var lightningToastMessage by remember { mutableStateOf<String?>(null) }
    var srsToast by remember { mutableStateOf<SrsStageChange?>(null) }
    var burstTrigger by remember { mutableIntStateOf(0) }
    var failTrigger by remember { mutableIntStateOf(0) }
    var nextFrozen by remember { mutableStateOf(false) }
    var resurrectIds by remember { mutableStateOf<LongArray?>(null) }
    val bulkOpState = remember { BulkOpState() }
    val shakeOffset = remember { Animatable(0f) }

    val question = session.getCurrentQuestion()
    val item = session.getCurrentItem()
    val answered = session.isAnswered()
    val correct = session.isCorrect()
    val ankiMode = question != null && GlobalSettings.getAnkiMode(session.getType(), question.getType())

    LaunchedEffect(Unit) {
        session.chooseQuestion()
        uiTick++
    }

    // Central tick handler used by all sub-screens: like the legacy
    // SessionActivity.updateFragment(), it picks a new question when the
    // session is active but has none (e.g. right after startQuiz()).
    fun handleTick() {
        if (session.getCurrentQuestion() == null && session.isActive()) {
            session.chooseQuestion()
        }
        uiTick++
    }

    LaunchedEffect(uiTick) {
        if (session.isInactive()) {
            onFinishSession()
        }
    }

    // Load the subject (and the matching kanji for single-kanji vocab) off the main thread.
    LaunchedEffect(item?.getId(), uiTick) {
        val itemId = item?.getId() ?: return@LaunchedEffect
        val loaded = withContext(Dispatchers.IO) {
            val db = WkApplication.getDatabase()
            val subject = db.subjectCollectionsDao().getSessionSubjects()[itemId]
            val characters = subject?.getCharacters().orEmpty()
            val matching = if (subject != null && subject.getType().isVocabulary() && characters.length == 1
                && GlobalSettings.AdvancedOther.getShakeOnMatchingKanji()
            ) {
                db.subjectDao().getKanjiByCharacters(characters)
            } else {
                null
            }
            Triple(subject, matching, subject != null && AudioUtil.hasAudio(subject))
        }
        item.setSubject(loaded.first)
        currentSubject = loaded.first
        matchingKanji = loaded.second
        canReplayAudio = loaded.third
    }

    // Freeze the Next button after an incorrect answer, per getNextButtonDelay().
    LaunchedEffect(answered, correct, uiTick) {
        if (answered && !correct && session.isNextButtonFrozen()) {
            nextFrozen = true
            while (session.isNextButtonFrozen()) {
                delay(200)
            }
            nextFrozen = false
        } else {
            nextFrozen = false
        }
    }

    LaunchedEffect(uiTick) {
        captureSrsStageChange()?.let { srsToast = it }
    }

    fun shakeInput() {
        coroutineScope.launch {
            repeat(3) {
                shakeOffset.animateTo(12f, tween(durationMillis = 50))
                shakeOffset.animateTo(-12f, tween(durationMillis = 50))
            }
            shakeOffset.animateTo(0f, tween(durationMillis = 50))
        }
    }

    fun onAnswerChanged(value: TextFieldValue) {
        if (value.text.length > 50) {
            return
        }
        val kana = question?.getType()?.isKana() == true
        answerField = if (kana && value.composition == null && value.text != answerField.text) {
            val editable = SpannableStringBuilder(value.text)
            PseudoIme.fixup(editable, 0, editable.length)
            val converted = editable.toString()
            TextFieldValue(converted, TextRange(converted.length))
        } else {
            value
        }
    }

    fun clearMessages() {
        closeMessage = null
        alternativesMessage = null
        digraphMessage = null
    }

    fun playAudioForCorrectAnswer() {
        val subject = currentSubject
        if (subject != null && question?.getType()?.isReading() == true
            && !FloatingUiState.audioPlayed && GlobalSettings.getAutoPlay(session.getType())
        ) {
            FloatingUiState.audioPlayed = true
            AudioUtil.playAudio(subject, FloatingUiState.getLastMatchedAnswer())
        }
    }

    fun advanceToNextQuestion() {
        session.advance()
        session.chooseQuestion()
        answerField = TextFieldValue("")
        clearMessages()
        FloatingUiState.audioPlayed = false
        uiTick++
    }

    fun submitAnswer() {
        if (answered || ankiMode || question == null) {
            return
        }
        FloatingUiState.setCurrentAnswer(answerField.text)
        val verdict = try {
            session.submit(matchingKanji)
        } catch (e: Exception) {
            Log.e("SessionScreen", "Error checking answer", e)
            return
        }
        if (verdict.isRetry()) {
            shakeInput()
            haptics.retry()
            return
        }

        val lastVerdict = FloatingUiState.lastVerdict
        closeMessage = if (FloatingUiState.showCloseToast && lastVerdict != null
            && GlobalSettings.Review.getCloseEnoughAction().isShowToast()
        ) {
            "Your answer ('${lastVerdict.getGivenAnswer()}') was a bit off, the closest correct answer was " +
                "'${lastVerdict.getMatchedAnswer()}'. Check the meaning to make sure you are correct."
        } else {
            null
        }
        alternativesMessage = if (FloatingUiState.showAlternativesToast && GlobalSettings.Review.getEnableAlternativesToast()) {
            FloatingUiState.alternativesForLastCorrectAnswer?.let { "Alt: $it" }
        } else {
            null
        }
        digraphMessage = lastVerdict?.getDigraphMatch()?.let { match ->
            String.format(
                "Your answer was incorrect because you mixed up the regular kana %c and the small kana %c. " +
                    "Tap this message for more information about the difference between small and regular kana.",
                match.getRegularKana(),
                match.getSmallKana(),
            )
        }

        if (verdict.isOk()) {
            haptics.success()
            if (GlobalSettings.Review.getShowAnswerToast()) {
                burstTrigger++
            }
            if (session.isLightningModeActive()
                && lastVerdict != null && lastVerdict.isNearMatch()
                && GlobalSettings.Review.getCloseEnoughAction() == CloseEnoughAction.ACCEPT_WITH_TOAST_NO_LM
            ) {
                FloatingUiState.lingerOnAnswer = true
            }
            playAudioForCorrectAnswer()
            if (session.isLightningModeActive() && !FloatingUiState.lingerOnAnswer) {
                val toastParts = listOfNotNull(closeMessage, alternativesMessage).filter { it.isNotBlank() }
                if (toastParts.isNotEmpty()) {
                    lightningToastMessage = toastParts.joinToString("\n")
                }
                advanceToNextQuestion()
                return
            }
        } else {
            haptics.failure()
            if (GlobalSettings.Review.getShowAnswerToast()) {
                failTrigger++
            }
        }
        uiTick++
    }

    fun dontKnow() {
        if (answered || ankiMode || question == null) {
            return
        }
        session.submitDontKnow()
        answerField = TextFieldValue("")
        clearMessages()
        uiTick++
    }

    LaunchedEffect(question, currentSubject, answered) {
        if (!answered && currentSubject != null) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    val subjectColor = currentSubject?.let { Color(it.getBackgroundColor()) } ?: MaterialTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxSize()) {
        when {
            session.isInactive() -> Unit
            question == null && !session.isActive() -> {
                SessionSummaryScreen(
                    onFinishSession = onFinishSession,
                    onViewSubject = onViewSubject,
                    onResurrectSubjects = { ids ->
                        resurrectIds = ids
                        bulkOpState.reset(ids.size)
                    },
                    onSessionTick = { handleTick() },
                )
            }
            session.getType() == SessionType.LESSON && question != null && !session.isActive() -> {
                LessonPresentationScreen(
                    subject = currentSubject,
                    onFinishSession = onFinishSession,
                    onSessionTick = { handleTick() },
                    onViewSubject = onViewSubject,
                )
            }
            ankiMode -> {
                AnkiQuestionScreen(
                    question = question,
                    subject = currentSubject,
                    onFinishSession = onFinishSession,
                    onSessionTick = { handleTick() },
                    onViewSubject = onViewSubject,
                )
            }
            question != null && answered -> {
                SessionScaffold(
                    title = question.getTitle(),
                    backgroundColor = subjectColor,
                    onFinishSession = onFinishSession,
                    onSessionTick = { handleTick() },
                    onViewSubject = onViewSubject,
                    bottomBar = {
                        Surface(
                            tonalElevation = 3.dp,
                            shadowElevation = 8.dp,
                            modifier = Modifier.navigationBarsPadding(),
                        ) {
                            Button(
                                onClick = { if (!nextFrozen) advanceToNextQuestion() },
                                enabled = !nextFrozen,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                Text("Next")
                            }
                        }
                    },
                ) { paddingValues ->
                    SlideTransitionBox(
                        trigger = question,
                        direction = session.getQuestionChoiceReason().getAnimation(),
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                    ) {
                        AnsweredQuestionContent(
                            session = session,
                            question = question,
                            subject = currentSubject,
                            isCorrect = correct,
                            closeMessage = closeMessage,
                            alternativesMessage = alternativesMessage,
                            digraphMessage = digraphMessage,
                            canReplayAudio = canReplayAudio,
                            onNext = { if (!nextFrozen) advanceToNextQuestion() },
                            onSpecialAction = { handleTick() },
                            onShowDigraphHelp = { showDigraphHelp = true },
                        )
                    }
                }
            }
            question != null -> {
                SessionScaffold(
                    title = question.getTitle(),
                    backgroundColor = subjectColor,
                    onFinishSession = onFinishSession,
                    onSessionTick = { handleTick() },
                    onViewSubject = onViewSubject,
                ) { paddingValues ->
                    SlideTransitionBox(
                        trigger = question,
                        direction = session.getQuestionChoiceReason().getAnimation(),
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                    ) {
                        UnansweredQuestionContent(
                            session = session,
                            question = question,
                            subject = currentSubject,
                            answerField = answerField,
                            onAnswerChanged = { onAnswerChanged(it) },
                            onSubmit = { submitAnswer() },
                            onDontKnow = { dontKnow() },
                            onSpecialAction = { handleTick() },
                            focusRequester = focusRequester,
                            shakeOffset = shakeOffset,
                        )
                    }
                }
            }
        }

        CorrectOverlay(trigger = burstTrigger, modifier = Modifier.fillMaxSize())
        FailOverlay(trigger = failTrigger, modifier = Modifier.fillMaxSize())
        lightningToastMessage?.let { message ->
            AutoDismissMessage(
                text = message,
                modifier = Modifier.fillMaxSize(),
                onFinished = { lightningToastMessage = null },
            )
        }
        srsToast?.let { toast ->
            AutoDismissMessage(
                text = "${if (toast.goingUp) "↑" else "↓"} ${toast.stageName}",
                modifier = Modifier.fillMaxSize(),
                onFinished = { srsToast = null },
            )
        }
    }

    if (showDigraphHelp) {
        DigraphHelpDialog(onDismiss = { showDigraphHelp = false })
    }

    if (resurrectIds != null && bulkOpState.visible) {
        BulkOpDialog(
            type = BulkOpType.RESURRECT,
            state = bulkOpState,
            onStart = {
                val ids = requireNotNull(resurrectIds).toList()
                resurrectIds = null
                coroutineScope.launch {
                    bulkOpState.running = true
                    runBulkOp(BulkOpType.RESURRECT, ids, bulkOpState)
                }
            },
            onDismiss = { resurrectIds = null },
        )
    }
}
