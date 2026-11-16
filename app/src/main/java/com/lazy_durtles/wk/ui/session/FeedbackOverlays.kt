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


internal val CorrectColor = Color(0xFF43A047)

internal class Speck(val angle: Float, val distanceFraction: Float, val radius: Float, val color: Color)

@Composable
internal fun CorrectOverlay(trigger: Int, modifier: Modifier = Modifier) {
    FeedbackOverlay(
        trigger = trigger,
        speckColors = listOf(Color(0xFF66BB6A), Color(0xFF81C784), Color(0xFFA5D6A7), Color(0xFF43A047)),
        circleColor = CorrectColor,
        icon = MaterialSymbolsIcons.Check,
        iconDescription = "Correct",
        modifier = modifier,
    )
}

@Composable
internal fun FailOverlay(trigger: Int, modifier: Modifier = Modifier) {
    FeedbackOverlay(
        trigger = trigger,
        speckColors = listOf(Color(0xFFE57373), Color(0xFFEF9A9A), Color(0xFFE53935)),
        circleColor = MaterialTheme.colorScheme.error,
        icon = MaterialSymbolsIcons.Close,
        iconDescription = "Incorrect",
        modifier = modifier,
    )
}

@Composable
internal fun FeedbackOverlay(
    trigger: Int,
    speckColors: List<Color>,
    circleColor: Color,
    icon: Char,
    iconDescription: String,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(1f) }
    var active by remember { mutableStateOf(false) }
    val specks = remember {
        List(28) { i ->
            Speck(
                angle = (2 * PI * i / 28 + Random.nextDouble(-0.1, 0.1)).toFloat(),
                distanceFraction = 0.2f + Random.nextFloat() * 0.5f,
                radius = 2f + Random.nextFloat() * 2f,
                color = speckColors[i % speckColors.size],
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger == 0) {
            return@LaunchedEffect
        }
        active = true
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 600, easing = LinearOutSlowInEasing))
        active = false
    }

    if (!active) {
        return
    }

    val p = progress.value
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxDistance = maxOf(size.width, size.height)
            specks.forEach { speck ->
                val d = speck.distanceFraction * maxDistance * p
                drawCircle(
                    color = speck.color,
                    radius = speck.radius,
                    center = Offset(center.x + cos(speck.angle) * d, center.y + sin(speck.angle) * d),
                    alpha = (1f - p).coerceIn(0f, 1f),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale((p * 4f).coerceAtMost(1f))
                .alpha(1f - ((p - 0.6f) / 0.4f).coerceIn(0f, 1f))
                .background(circleColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            MaterialSymbolIcon(
                icon = icon,
                contentDescription = iconDescription,
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

