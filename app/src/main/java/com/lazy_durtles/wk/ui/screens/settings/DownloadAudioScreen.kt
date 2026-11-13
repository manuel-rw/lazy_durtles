package com.lazy_durtles.wk.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lazy_durtles.wk.Constants
import com.lazy_durtles.wk.db.model.AudioDownloadStatus
import com.lazy_durtles.wk.jobs.AbortAudioDownloadJob
import com.lazy_durtles.wk.jobs.DeleteAllAudioJob
import com.lazy_durtles.wk.jobs.ScanAudioDownloadStatusJob
import com.lazy_durtles.wk.jobs.StartAudioDownloadJob
import com.lazy_durtles.wk.livedata.LiveAudioDownloadStatus
import com.lazy_durtles.wk.livedata.LiveAudioMoveStatus
import com.lazy_durtles.wk.livedata.LiveTaskCounts
import com.lazy_durtles.wk.services.JobRunnerService
import com.lazy_durtles.wk.ui.icons.MaterialSymbolIcon
import com.lazy_durtles.wk.ui.icons.MaterialSymbolsIcons
import com.lazy_durtles.wk.util.AudioUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Overview of vocabulary audio downloads per level bracket, with controls to
 * start / abort downloads, delete all downloaded audio, and move misplaced
 * audio files to the preferred location. A port of the legacy
 * DownloadAudioActivity and DownloadAudioBracketView, restyled with Material
 * Expressive cards and progress bars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadAudioScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val overview by LiveAudioDownloadStatus.getInstance().observeAsState(initial = emptyList())
    val moveStatusUpdate by LiveAudioMoveStatus.getInstance().observeAsState()
    val taskCounts by LiveTaskCounts.getInstance().observeAsState()
    val audioCount = taskCounts?.audioCount ?: 0

    var deleteConfirmOpen by remember { mutableStateOf(false) }
    var hasMisplacedFiles by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        JobRunnerService.schedule(ScanAudioDownloadStatusJob::class.java, "")
    }

    // The legacy activity deactivates any running move task when it pauses.
    DisposableEffect(Unit) {
        onDispose {
            LiveAudioMoveStatus.getInstance().isActive = false
            LiveAudioMoveStatus.getInstance().forceUpdate()
        }
    }

    LaunchedEffect(overview, moveStatusUpdate) {
        hasMisplacedFiles = withContext(Dispatchers.IO) { AudioUtil.hasAnyMisplacedAudioFiles() }
    }

    val moveActive = LiveAudioMoveStatus.getInstance().isActive
    val moveDone = LiveAudioMoveStatus.getInstance().numDone
    val moveTotal = LiveAudioMoveStatus.getInstance().numTotal
    val moveFraction = if (moveTotal > 0) {
        (moveDone.toFloat() / moveTotal.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    fun startMove() {
        val status = LiveAudioMoveStatus.getInstance()
        status.isActive = true
        status.numDone = 0
        status.numTotal = 100
        status.forceUpdate()
        scope.launch {
            withContext(Dispatchers.IO) {
                val numTotal = AudioUtil.getNumMisplacedAudioFiles()
                if (numTotal > 0) {
                    status.numTotal = numTotal
                    status.forceUpdate()
                    AudioUtil.iterateMisplacedAudioFiles { input ->
                        AudioUtil.moveToPreferredLocation(input)
                        status.numDone = status.numDone + 1
                        status.forceUpdate()
                        !status.isActive
                    }
                }
            }
            status.isActive = false
            status.forceUpdate()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Download audio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        MaterialSymbolIcon(
                            icon = MaterialSymbolsIcons.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "You can download vocabulary audio here, which will be played during lessons " +
                    "and reviews. Once started, downloads will continue in the background.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Active downloads card.
            SettingsGroupCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TonalIcon(
                            icon = MaterialSymbolsIcons.CloudDownload,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active downloads",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = if (audioCount > 0) {
                                    "$audioCount download${if (audioCount == 1) "" else "s"} in progress"
                                } else {
                                    "No downloads in progress"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (audioCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                JobRunnerService.schedule(AbortAudioDownloadJob::class.java, "")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "Abort downloads")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { deleteConfirmOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Delete all audio",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            // Misplaced files card, only relevant when files are misplaced or
            // a move task is running.
            if (hasMisplacedFiles || moveActive) {
                SettingsGroupCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TonalIcon(icon = MaterialSymbolsIcons.FolderOpen)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Misplaced audio files",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "Move audio files from other locations to the preferred location",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (moveActive) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { moveFraction },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                text = "$moveDone / $moveTotal files",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    LiveAudioMoveStatus.getInstance().isActive = false
                                    LiveAudioMoveStatus.getInstance().forceUpdate()
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = "Abort move")
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = ::startMove,
                                enabled = hasMisplacedFiles,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = "Move files")
                            }
                        }
                    }
                }
            }

            // Per-bracket download cards.
            val maxLevel = maxOf(overview.size, 60)
            var minLevel = 1
            while (minLevel <= maxLevel) {
                DownloadAudioBracket(
                    overview = overview,
                    minLevel = minLevel,
                    maxLevel = minLevel + 9,
                    audioCount = audioCount,
                )
                minLevel += 10
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (deleteConfirmOpen) {
        SettingsAlertDialog(
            title = "Delete all audio?",
            html = Constants.DELETE_AUDIO_WARNING,
            icon = MaterialSymbolsIcons.DeleteForever,
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            destructive = true,
            onConfirm = {
                deleteConfirmOpen = false
                JobRunnerService.schedule(DeleteAllAudioJob::class.java, "")
            },
            onDismiss = { deleteConfirmOpen = false },
        )
    }
}

/**
 * One level bracket in the audio download overview: a card with a progress
 * bar, a summary of how much audio is available for these levels, and a
 * download button. A port of the legacy DownloadAudioBracketView.
 */
@Composable
private fun DownloadAudioBracket(
    overview: List<AudioDownloadStatus>,
    minLevel: Int,
    maxLevel: Int,
    audioCount: Int,
) {
    var numTotal = 0
    var numNoAudio = 0
    var numMissingAudio = 0
    var numPartialAudio = 0
    var numFullAudio = 0
    for (status in overview) {
        if (status.level < minLevel || status.level > maxLevel) {
            continue
        }
        numTotal += status.numTotal
        numNoAudio += status.numNoAudio
        numMissingAudio += status.numMissingAudio
        numPartialAudio += status.numPartialAudio
        numFullAudio += status.numFullAudio
    }

    val numAvailable = numTotal - numNoAudio
    val numDownloaded = numFullAudio + numPartialAudio
    val fraction = if (numAvailable > 0) {
        (numDownloaded.toFloat() / numAvailable.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val complete = numAvailable > 0 && numMissingAudio == 0 && numPartialAudio == 0
    val downloadEnabled = audioCount == 0 && (numMissingAudio > 0 || numPartialAudio > 0)

    SettingsGroupCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Levels %d\u2013%d".format(Locale.ROOT, minLevel, maxLevel),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (complete) {
                    MaterialSymbolIcon(
                        icon = MaterialSymbolsIcons.CheckCircle,
                        contentDescription = "Download finished",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (numAvailable > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (complete) {
                    "All audio downloaded"
                } else if (numAvailable == 0) {
                    "No audio available for these levels"
                } else {
                    "%d/%d downloaded".format(Locale.ROOT, numDownloaded, numAvailable) +
                        if (numPartialAudio > 0) {
                            ", %d partially".format(Locale.ROOT, numPartialAudio)
                        } else {
                            ""
                        }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!complete && numAvailable > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        JobRunnerService.schedule(
                            StartAudioDownloadJob::class.java,
                            String.format(Locale.ROOT, "%d|%d", minLevel, maxLevel),
                        )
                    },
                    enabled = downloadEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                ) {
                    Text(text = "Download")
                }
            }
        }
    }
}
