package uiz.attendance.examattendance.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uiz.attendance.examattendance.model.ExamBlock
import uiz.attendance.examattendance.network.RetrofitClient
import uiz.attendance.examattendance.network.isNetworkConnected
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private enum class ExamBlockTimingState { NOT_STARTED, IN_PROGRESS, FINISHED }

/**
 * `scheduledTime` arrives with no timezone offset (e.g. "2026-09-15T09:00:00"): it means local
 * wall-clock time in the room where the exam happens. The proctor's phone is physically in that
 * room, so the device's own timezone is the correct one to interpret it in — set explicitly below
 * rather than left to SimpleDateFormat's implicit default, so that assumption is visible in code.
 * Returns null (no opinion) if `scheduledTime` doesn't parse, rather than guessing.
 */
private fun examBlockTimingState(
    examBlock: ExamBlock,
    now: Long = System.currentTimeMillis()
): ExamBlockTimingState? {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }
    val scheduledTime = runCatching { format.parse(examBlock.scheduledTime) }.getOrNull() ?: return null
    val start = scheduledTime.time
    val end = start + examBlock.durationMinutes * 60_000L
    return when {
        now < start -> ExamBlockTimingState.NOT_STARTED
        now < end -> ExamBlockTimingState.IN_PROGRESS
        else -> ExamBlockTimingState.FINISHED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamBlockListScreen(modifier: Modifier = Modifier, onStartScanning: (ExamBlock) -> Unit) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var examBlocks by remember { mutableStateOf<List<ExamBlock>>(emptyList()) }
    var selectedExamBlock by remember { mutableStateOf<ExamBlock?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    val filteredExamBlocks = if (searchQuery.isBlank()) {
        examBlocks
    } else {
        examBlocks.filter { it.moduleName.contains(searchQuery.trim(), ignoreCase = true) }
    }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        errorMessage = null
        if (!isNetworkConnected(context)) {
            errorMessage = "No internet connection"
            isLoading = false
            return@LaunchedEffect
        }
        try {
            examBlocks = RetrofitClient.apiService.getExamBlocks()
        } catch (e: IOException) {
            errorMessage = "Server unreachable"
        } catch (e: Exception) {
            errorMessage = "Could not load exam blocks. Check your connection."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Exam Attendance") },
                actions = {
                    IconButton(onClick = { refreshTrigger++ }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage.orEmpty())
                    }
                }

                else -> {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(text = "Search by module name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (filteredExamBlocks.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No exam block matches your search")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(filteredExamBlocks) { examBlock ->
                                val isSelected = examBlock == selectedExamBlock
                                val timingState = examBlockTimingState(examBlock)
                                val isDimmed = timingState != null && timingState != ExamBlockTimingState.IN_PROGRESS
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clickable { selectedExamBlock = examBlock },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Module name stays at full opacity in every state so it's
                                        // always readable; only the secondary info below dims.
                                        Text(
                                            text = examBlock.moduleName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Column(modifier = Modifier.alpha(if (isDimmed) 0.8f else 1f)) {
                                            Text(text = examBlock.sessionType, style = MaterialTheme.typography.bodySmall)
                                            Text(text = examBlock.scheduledTime, style = MaterialTheme.typography.bodySmall)
                                            if (timingState != null) {
                                                Text(
                                                    text = when (timingState) {
                                                        ExamBlockTimingState.NOT_STARTED -> "Not started yet"
                                                        ExamBlockTimingState.IN_PROGRESS -> "In progress"
                                                        ExamBlockTimingState.FINISHED -> "Finished"
                                                    },
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (timingState) {
                                                        ExamBlockTimingState.NOT_STARTED -> MaterialTheme.colorScheme.outline
                                                        ExamBlockTimingState.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                                                        ExamBlockTimingState.FINISHED -> MaterialTheme.colorScheme.error
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            selectedExamBlock?.let(onStartScanning)
                        },
                        enabled = selectedExamBlock != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Start Scanning")
                    }
                }
            }
        }
    }
}
