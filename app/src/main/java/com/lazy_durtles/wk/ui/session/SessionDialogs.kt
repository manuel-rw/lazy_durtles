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
internal fun DigraphHelpDialog(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Digraph information") },
        text = {
            Column(modifier = modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HtmlNoticeText(html = Constants.DIGRAPH_HELP_DOCUMENT_1)
                HtmlNoticeText(html = Constants.DIGRAPH_HELP_DOCUMENT_2)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}
