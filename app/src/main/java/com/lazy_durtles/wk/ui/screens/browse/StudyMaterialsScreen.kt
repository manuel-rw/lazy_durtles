package com.lazy_durtles.wk.ui.screens.browse

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.lazy_durtles.wk.ui.icons.MaterialSymbolsIcons
import com.lazy_durtles.wk.ui.icons.MaterialSymbolIcon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.lazy_durtles.wk.GlobalSettings
import com.lazy_durtles.wk.WkApplication
import com.lazy_durtles.wk.db.Converters
import com.lazy_durtles.wk.db.model.Subject
import com.lazy_durtles.wk.jobs.SaveStudyMaterialJob
import com.lazy_durtles.wk.services.JobRunnerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The study materials editor ("My notes"), ported from the legacy
 * StudyMaterialsActivity: the user's meaning note, reading note and up to ten
 * meaning synonyms, added with a button and reordered by dragging their
 * handles. Saving pushes the material through SaveStudyMaterialJob, like the
 * legacy activity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMaterialsScreen(
    subjectId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var loading by remember { mutableStateOf(true) }
    var subject by remember { mutableStateOf<Subject?>(null) }
    // rememberSaveable keeps the user's edits across configuration changes,
    // like the legacy stateSaved flag.
    var populated by rememberSaveable { mutableStateOf(false) }
    var meaningNote by rememberSaveable { mutableStateOf("") }
    var readingNote by rememberSaveable { mutableStateOf("") }
    var synonyms by rememberSaveable { mutableStateOf(listOf<String>()) }

    // Drag-to-reorder state for the synonym rows. dragIndex is the row being
    // dragged, dragOffsetPx its visual offset within the current slot, and
    // rowStridePx the measured distance between row tops.
    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var rowStridePx by remember { mutableFloatStateOf(0f) }
    val rowSpacingPx = with(LocalDensity.current) { 8.dp.toPx() }

    LaunchedEffect(subjectId) {
        val loaded = withContext(Dispatchers.IO) {
            WkApplication.getDatabase().subjectDao().getById(subjectId)
        }
        if (loaded == null || !loaded.getType().canHaveStudyMaterials()) {
            onBack()
            return@LaunchedEffect
        }
        subject = loaded
        if (!populated) {
            populated = true
            meaningNote = loaded.getMeaningNote() ?: ""
            readingNote = loaded.getReadingNote() ?: ""
            synonyms = loaded.getMeaningSynonyms().take(10)
        }
        loading = false
    }

    val scope = rememberCoroutineScope()
    fun saveStudyMaterials() {
        val currentSubject = subject ?: return
        scope.launch {
            withContext(Dispatchers.IO) {
                val data = ArrayList<String>()
                data.add(currentSubject.getId().toString())
                data.add(meaningNote)
                data.add(readingNote)
                data.addAll(synonyms.filter { it.isNotBlank() })
                val dataString = Converters.getObjectMapper().writeValueAsString(data)
                JobRunnerService.schedule(SaveStudyMaterialJob::class.java, dataString)
            }
            onBack()
        }
    }

    val s = subject
    val noteFontSize = GlobalSettings.Font.getFontSizeSubjectInfo().sp
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = s?.let { it.getInfoTitle("My notes for ", ":").toString() } ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        MaterialSymbolIcon(MaterialSymbolsIcons.ArrowBack, "Back")
                    }
                },
            )
        },
        bottomBar = {
            // Pinned Save action, visible while the content scrolls.
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                Button(
                    onClick = { saveStudyMaterials() },
                    enabled = !loading && s != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text("Save")
                }
            }
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            s == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Subject not found")
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoSection(header = "Meaning note") {
                    OutlinedTextField(
                        value = meaningNote,
                        onValueChange = { meaningNote = it.take(500) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("My meaning note") },
                        textStyle = LocalTextStyle.current.copy(fontSize = noteFontSize),
                    )
                }
                InfoSection(header = "Reading note") {
                    OutlinedTextField(
                        value = readingNote,
                        onValueChange = { readingNote = it.take(500) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("My reading note") },
                        textStyle = LocalTextStyle.current.copy(fontSize = noteFontSize),
                    )
                }
                InfoSection(header = "Synonyms") {
                    synonyms.forEachIndexed { index, value ->
                        SynonymRow(
                            value = value,
                            isDragging = dragIndex == index,
                            dragOffsetPx = if (dragIndex == index) dragOffsetPx else 0f,
                            onValueChange = { newValue ->
                                synonyms = synonyms.toMutableList().also { it[index] = newValue }
                            },
                            onRemove = {
                                synonyms = synonyms.toMutableList().also { it.removeAt(index) }
                            },
                            onDragStart = {
                                dragIndex = index
                                dragOffsetPx = 0f
                            },
                            onDrag = { delta ->
                                dragOffsetPx += delta
                                val current = dragIndex ?: return@SynonymRow
                                if (rowStridePx <= 0f) return@SynonymRow
                                val steps = (dragOffsetPx / rowStridePx).toInt()
                                if (steps != 0) {
                                    val target = (current + steps).coerceIn(0, synonyms.lastIndex)
                                    if (target != current) {
                                        synonyms = synonyms.toMutableList().apply {
                                            add(target, removeAt(current))
                                        }
                                        dragIndex = target
                                        dragOffsetPx -= (target - current) * rowStridePx
                                    }
                                }
                            },
                            onDragEnd = {
                                dragIndex = null
                                dragOffsetPx = 0f
                            },
                            onRowMeasured = { rowStridePx = it + rowSpacingPx },
                        )
                    }
                    OutlinedButton(
                        onClick = { synonyms = synonyms + "" },
                        enabled = synonyms.size < 10,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MaterialSymbolIcon(MaterialSymbolsIcons.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add synonym")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * One synonym row: a drag handle for reordering, the text field, and a remove
 * button. The drag gesture lives on the handle only, so drags elsewhere keep
 * scrolling the form. While dragging, the row is offset by [dragOffsetPx] and
 * drawn above its siblings; the parent swaps list entries when the offset
 * crosses a row boundary. Callbacks are read through rememberUpdatedState so
 * an in-progress drag survives the live reordering.
 */
@Composable
private fun SynonymRow(
    value: String,
    isDragging: Boolean,
    dragOffsetPx: Float,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onRowMeasured: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { onRowMeasured(it.height) }
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer { translationY = dragOffsetPx },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { currentOnDragStart() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentOnDrag(dragAmount.y)
                        },
                        onDragEnd = { currentOnDragEnd() },
                        onDragCancel = { currentOnDragEnd() },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            MaterialSymbolIcon(MaterialSymbolsIcons.DragHandle, "Drag to reorder")
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text("Synonym") },
            textStyle = LocalTextStyle.current.copy(
                fontSize = GlobalSettings.Font.getFontSizeSubjectInfo().sp,
            ),
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            MaterialSymbolIcon(MaterialSymbolsIcons.Close, "Remove synonym")
        }
    }
}
