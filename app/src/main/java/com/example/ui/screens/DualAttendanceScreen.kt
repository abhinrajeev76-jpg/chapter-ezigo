package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.entity.AttendanceEntity
import com.example.data.entity.StudentEntity
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.ui.EzigoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualAttendanceScreen(
    viewModel: EzigoViewModel,
    initialSession: AttendanceSession? = null
) {
    val students by viewModel.allStudents.collectAsState()
    val batches by viewModel.allBatches.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()

    var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 1L) }
    var selectedDate by remember { mutableStateOf("2026-09-16") }
    var selectedSession by remember { mutableStateOf(initialSession ?: AttendanceSession.MORNING) }
    var remarksMap by remember { mutableStateOf(mutableMapOf<Long, String>()) }

    // Update selected batch when batches load
    LaunchedEffect(batches) {
        if (selectedBatchId == 1L && batches.isNotEmpty()) {
            selectedBatchId = batches.first().id
        }
    }

    // Students in selected batch
    val batchStudents = remember(students, selectedBatchId) {
        students.filter { it.batchId == selectedBatchId }
    }

    // Local state for the attendance being marked: studentId -> status
    val currentSessionAttendance = remember(allAttendance, selectedBatchId, selectedDate, selectedSession) {
        allAttendance
            .filter { it.batchId == selectedBatchId && it.date == selectedDate && it.session == selectedSession }
            .associate { it.studentId to it.status }
    }

    val attendanceMap = remember { mutableStateMapOf<Long, AttendanceStatus>() }

    // Populate attendance map from database or default to PRESENT
    LaunchedEffect(currentSessionAttendance, batchStudents) {
        attendanceMap.clear()
        batchStudents.forEach { s ->
            val existing = currentSessionAttendance[s.id]
            attendanceMap[s.id] = existing ?: AttendanceStatus.PRESENT
        }
    }

    // Overall daily dual-session statistics for the selected batch on the selected date
    val morningRecords = remember(allAttendance, selectedBatchId, selectedDate) {
        allAttendance.filter { it.batchId == selectedBatchId && it.date == selectedDate && it.session == AttendanceSession.MORNING }
    }
    val eveningRecords = remember(allAttendance, selectedBatchId, selectedDate) {
        allAttendance.filter { it.batchId == selectedBatchId && it.date == selectedDate && it.session == AttendanceSession.EVENING }
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val records = attendanceMap.map { (studentId, status) -> Pair(studentId, status) }
                        viewModel.submitAttendance(
                            batchId = selectedBatchId,
                            date = selectedDate,
                            session = selectedSession,
                            records = records,
                            onComplete = {}
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit ${selectedSession.displayName} Attendance (${attendanceMap.size} Students)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Title and Independent Session Explainer
                Text(
                    text = "Dual-Session Attendance Register",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = EzigoNavy
                )
                Text(
                    text = "Morning and Evening sessions are recorded & stored independently to ensure accurate partial-day calculations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Batch Selector Chips
            item {
                Text("Select Batch", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(batches) { batch ->
                        FilterChip(
                            selected = selectedBatchId == batch.id,
                            onClick = { selectedBatchId = batch.id },
                            label = { Text(batch.batchName) }
                        )
                    }
                }
            }

            // Session & Date Selector
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Morning Tab Button
                            Button(
                                onClick = { selectedSession = AttendanceSession.MORNING },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedSession == AttendanceSession.MORNING) EzigoNavy else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedSession == AttendanceSession.MORNING) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(18.dp), tint = EzigoGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Morning Session", fontWeight = FontWeight.SemiBold)
                            }

                            // Evening Tab Button
                            Button(
                                onClick = { selectedSession = AttendanceSession.EVENING },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedSession == AttendanceSession.EVENING) Color(0xFF1E1B4B) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedSession == AttendanceSession.EVENING) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFA78BFA))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Evening Session", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Date field
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = { selectedDate = it },
                            label = { Text("Attendance Date (YYYY-MM-DD)") },
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Dual Session Daily Matrix Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Daily Dual-Session Status for $selectedDate",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Morning Marked", style = MaterialTheme.typography.labelSmall)
                                Text("${morningRecords.size} / ${batchStudents.size}", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Evening Marked", style = MaterialTheme.typography.labelSmall)
                                Text("${eveningRecords.size} / ${batchStudents.size}", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                val mPres = morningRecords.count { it.status == AttendanceStatus.PRESENT }
                                val ePres = eveningRecords.count { it.status == AttendanceStatus.PRESENT }
                                Text("Day Present Rate", style = MaterialTheme.typography.labelSmall)
                                val rate = if (morningRecords.isNotEmpty() || eveningRecords.isNotEmpty()) {
                                    val totalSessions = morningRecords.size + eveningRecords.size
                                    if (totalSessions > 0) ((mPres + ePres) * 100.0 / totalSessions) else 0.0
                                } else 0.0
                                Text(String.format("%.1f%%", rate), fontWeight = FontWeight.Bold, color = EzigoSuccess)
                            }
                        }
                    }
                }
            }

            // Fast Bulk Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            batchStudents.forEach { s -> attendanceMap[s.id] = AttendanceStatus.PRESENT }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = EzigoSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Present", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            batchStudents.forEach { s -> attendanceMap[s.id] = AttendanceStatus.ABSENT }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = EzigoDanger, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Absent", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            batchStudents.forEach { s ->
                                val cur = attendanceMap[s.id] ?: AttendanceStatus.PRESENT
                                attendanceMap[s.id] = if (cur == AttendanceStatus.PRESENT) AttendanceStatus.ABSENT else AttendanceStatus.PRESENT
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Invert", fontSize = 12.sp)
                    }
                }
            }

            // Student Roster
            items(batchStudents, key = { it.id }) { student ->
                val currentStatus = attendanceMap[student.id] ?: AttendanceStatus.PRESENT
                val otherSessionStatus = remember(allAttendance, student.id, selectedDate, selectedSession) {
                    val otherSession = if (selectedSession == AttendanceSession.MORNING) AttendanceSession.EVENING else AttendanceSession.MORNING
                    allAttendance.find { it.studentId == student.id && it.date == selectedDate && it.session == otherSession }?.status
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = student.fullName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(text = student.studentId, type = BadgeType.NEUTRAL)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Show status of complementary session
                                val otherLabel = if (selectedSession == AttendanceSession.MORNING) "Evening Status" else "Morning Status"
                                Text(
                                    text = "$otherLabel: ${otherSessionStatus?.name ?: "Not Marked"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (otherSessionStatus) {
                                        AttendanceStatus.PRESENT -> EzigoSuccess
                                        AttendanceStatus.ABSENT -> EzigoDanger
                                        else -> EzigoTextMuted
                                    }
                                )
                            }

                            // Day Combined Status Badge (Prompt logic: Present+Present=Full, Present+Absent=Partial, Absent+Absent=Absent)
                            val combinedStatus = when {
                                currentStatus == AttendanceStatus.PRESENT && otherSessionStatus == AttendanceStatus.PRESENT -> "Full (100%)"
                                currentStatus == AttendanceStatus.PRESENT && otherSessionStatus == AttendanceStatus.ABSENT -> "Partial (50%)"
                                currentStatus == AttendanceStatus.ABSENT && otherSessionStatus == AttendanceStatus.PRESENT -> "Partial (50%)"
                                currentStatus == AttendanceStatus.ABSENT && otherSessionStatus == AttendanceStatus.ABSENT -> "Absent (0%)"
                                else -> "Pending"
                            }

                            StatusBadge(
                                text = combinedStatus,
                                type = when {
                                    combinedStatus.contains("Full") -> BadgeType.SUCCESS
                                    combinedStatus.contains("Partial") -> BadgeType.WARNING
                                    combinedStatus.contains("Absent") -> BadgeType.DANGER
                                    else -> BadgeType.INFO
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Selector Buttons (Present / Absent / Leave)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = currentStatus == AttendanceStatus.PRESENT,
                                onClick = { attendanceMap[student.id] = AttendanceStatus.PRESENT },
                                label = { Text("Present") },
                                leadingIcon = {
                                    if (currentStatus == AttendanceStatus.PRESENT) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EzigoSuccessBg,
                                    selectedLabelColor = EzigoSuccess
                                )
                            )

                            FilterChip(
                                selected = currentStatus == AttendanceStatus.ABSENT,
                                onClick = { attendanceMap[student.id] = AttendanceStatus.ABSENT },
                                label = { Text("Absent") },
                                leadingIcon = {
                                    if (currentStatus == AttendanceStatus.ABSENT) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EzigoDangerBg,
                                    selectedLabelColor = EzigoDanger
                                )
                            )

                            FilterChip(
                                selected = currentStatus == AttendanceStatus.LEAVE,
                                onClick = { attendanceMap[student.id] = AttendanceStatus.LEAVE },
                                label = { Text("Leave") },
                                leadingIcon = {
                                    if (currentStatus == AttendanceStatus.LEAVE) {
                                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EzigoWarningBg,
                                    selectedLabelColor = EzigoWarning
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
