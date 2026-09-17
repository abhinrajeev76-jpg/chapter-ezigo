package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.SubjectEntity
import com.example.data.entity.SyllabusChapterEntity
import com.example.data.model.SyllabusStatus
import com.example.ui.EzigoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    viewModel: EzigoViewModel
) {
    val subjects by viewModel.allSubjects.collectAsState()
    val chapters by viewModel.allChapters.collectAsState()
    val batches by viewModel.allBatches.collectAsState()

    var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 1L) }
    var expandedSubjectId by remember { mutableStateOf<Long?>(subjects.firstOrNull()?.id ?: 1L) }
    var chapterToEdit by remember { mutableStateOf<SyllabusChapterEntity?>(null) }

    val batchSubjects = remember(subjects, selectedBatchId) {
        subjects.filter { it.batchId == selectedBatchId }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Academic Syllabus & Chapter Progress",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = EzigoNavy
            )
            Text(
                text = "Monitor topic-wise and chapter-wise syllabus completion percentages across academic terms.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Batch Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(batches) { b ->
                    FilterChip(
                        selected = selectedBatchId == b.id,
                        onClick = {
                            selectedBatchId = b.id
                            expandedSubjectId = null
                        },
                        label = { Text(b.batchName) }
                    )
                }
            }
        }

        // Subject Summary Cards
        items(batchSubjects) { subject ->
            val subChapters = chapters.filter { it.subjectId == subject.id }
            val totalChapters = subChapters.size
            val completedChapters = subChapters.count { it.status == SyllabusStatus.COMPLETED }
            val revisionChapters = subChapters.count { it.status == SyllabusStatus.REVISION }
            val pendingChapters = subChapters.count { it.status == SyllabusStatus.PENDING || it.status == SyllabusStatus.IN_PROGRESS }

            val percentage = if (totalChapters > 0) {
                ((completedChapters + revisionChapters) * 100.0 / totalChapters)
            } else 0.0

            val isExpanded = expandedSubjectId == subject.id

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSubjectId = if (isExpanded) null else subject.id },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject.subjectName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Faculty: ${subject.assignedFaculty} • $totalChapters Chapters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%.1f%%", percentage),
                                fontWeight = FontWeight.ExtraBold,
                                color = EzigoSuccess,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = EzigoNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EzigoSuccess,
                        trackColor = EzigoBorder
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Breakdown counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Completed: $completedChapters", fontSize = 12.sp, color = EzigoSuccess, fontWeight = FontWeight.SemiBold)
                        Text("Revision: $revisionChapters", fontSize = 12.sp, color = EzigoGold, fontWeight = FontWeight.SemiBold)
                        Text("Pending: $pendingChapters", fontSize = 12.sp, color = EzigoDanger, fontWeight = FontWeight.SemiBold)
                    }

                    // Expanded Chapter List
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Divider(color = EzigoBorder)
                            Text("Chapter Syllabus Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            subChapters.sortedBy { it.chapterNumber }.forEach { chapter ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${chapter.chapterNumber}. ${chapter.chapterName}",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Topics: ${chapter.completedTopics}/${chapter.totalTopics} • ${if (chapter.completionDate.isNotBlank()) "Completed: ${chapter.completionDate}" else "In progress"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EzigoTextMuted
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            StatusBadge(
                                                text = chapter.status.name,
                                                type = when (chapter.status) {
                                                    SyllabusStatus.COMPLETED -> BadgeType.SUCCESS
                                                    SyllabusStatus.REVISION -> BadgeType.INFO
                                                    SyllabusStatus.IN_PROGRESS -> BadgeType.WARNING
                                                    SyllabusStatus.PENDING -> BadgeType.DANGER
                                                }
                                            )

                                            IconButton(
                                                onClick = { chapterToEdit = chapter },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Chapter", modifier = Modifier.size(16.dp), tint = EzigoNavy)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Chapter Dialog
    chapterToEdit?.let { chapter ->
        EditChapterDialog(
            chapter = chapter,
            onDismiss = { chapterToEdit = null },
            onSave = { updated ->
                viewModel.updateChapter(updated)
                chapterToEdit = null
            }
        )
    }
}

@Composable
fun EditChapterDialog(
    chapter: SyllabusChapterEntity,
    onDismiss: () -> Unit,
    onSave: (SyllabusChapterEntity) -> Unit
) {
    var completedTopicsText by remember { mutableStateOf(chapter.completedTopics.toString()) }
    var totalTopicsText by remember { mutableStateOf(chapter.totalTopics.toString()) }
    var selectedStatus by remember { mutableStateOf(chapter.status) }
    var completionDate by remember { mutableStateOf(chapter.completionDate.ifBlank { "2026-09-16" }) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Update Chapter Progress", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = EzigoNavy)
                Text("Chapter: ${chapter.chapterNumber}. ${chapter.chapterName}", fontWeight = FontWeight.SemiBold)
                Divider()

                Text("Status", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SyllabusStatus.values()) { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(st.name) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = completedTopicsText, onValueChange = { completedTopicsText = it }, label = { Text("Completed Topics") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = totalTopicsText, onValueChange = { totalTopicsText = it }, label = { Text("Total Topics") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = completionDate, onValueChange = { completionDate = it }, label = { Text("Completion Date") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val comp = completedTopicsText.toIntOrNull() ?: chapter.completedTopics
                            val tot = totalTopicsText.toIntOrNull() ?: chapter.totalTopics
                            onSave(
                                chapter.copy(
                                    completedTopics = comp,
                                    totalTopics = tot,
                                    status = selectedStatus,
                                    completionDate = if (selectedStatus == SyllabusStatus.COMPLETED) completionDate else ""
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                    ) {
                        Text("Save Progress")
                    }
                }
            }
        }
    }
}
