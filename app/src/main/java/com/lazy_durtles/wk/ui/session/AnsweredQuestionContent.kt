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
internal fun AnsweredQuestionContent(
    session: Session,
    question: Question,
    subject: Subject?,
    isCorrect: Boolean,
    closeMessage: String?,
    alternativesMessage: String?,
    digraphMessage: String?,
    canReplayAudio: Boolean,
    onNext: () -> Unit,
    onSpecialAction: () -> Unit,
    onShowDigraphHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val heightDp = GlobalSettings.Display.getQuizQuestionViewHeight()
    val isReadingQuestion = question.getType().isReading()

    Column(
        modifier = modifier
            .fillMaxSize()
            .sessionSwipes(onSwipeRight = onNext)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SessionProgressHeader(
            progressText = session.getProgressText(),
            subjectColor = subject?.let { Color(it.getBackgroundColor()) } ?: MaterialTheme.colorScheme.primary,
        )

        if (subject != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SrsIndicatorChip(subject = subject)
                if (GlobalSettings.Other.getEnableStarRatings()) {
                    StarRatingRow(subject = subject)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(
                    containerColor = Color(question.getType().getHintColor()),
                ),
            ) {
                Text(
                    text = question.getTitle(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(question.getType().getHintContrastColor()),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (heightDp > 0) Modifier.height(heightDp.dp) else Modifier),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = subject?.let { Color(it.getBackgroundColor()) } ?: MaterialTheme.colorScheme.primary,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = subject?.getCharacters() ?: subject?.getSlug() ?: "",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = GlobalSettings.Font.getMaxFontSizeQuizText().coerceAtMost(100).sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = subject?.let { Color(it.getTextColor()) } ?: MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCorrect) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.errorContainer
                },
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MaterialSymbolIcon(
                        icon = if (isCorrect) MaterialSymbolsIcons.Check else MaterialSymbolsIcons.Close,
                        contentDescription = if (isCorrect) "Correct" else "Incorrect",
                        tint = if (isCorrect) CorrectColor else MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = if (isCorrect) "Correct!" else "Incorrect",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isCorrect) CorrectColor else MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                if (FloatingUiState.getCurrentAnswer().isNotEmpty()) {
                    Text(
                        text = FloatingUiState.getCurrentAnswer(),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                closeMessage?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                alternativesMessage?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                digraphMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onShowDigraphHelp),
                    )
                }
            }
        }

        if (subject != null) {
            SubjectInfoCard(
                subject = subject,
                container = SubjectInfoContainer.ANSWERED_QUESTION,
            )
        }

        if (isReadingQuestion && canReplayAudio && subject != null) {
            OutlinedButton(
                onClick = {
                    FloatingUiState.audioPlayed = true
                    AudioUtil.playAudio(subject, FloatingUiState.getLastMatchedAnswer())
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_volume_up_24dp),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play audio")
            }
        }

        SpecialButtonsRow(onActionPerformed = onSpecialAction)
    }
}

