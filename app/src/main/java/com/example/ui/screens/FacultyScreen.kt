package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.entity.BatchEntity
import com.example.data.entity.FacultyAttendanceEntity
import com.example.data.entity.FacultyEntity
import com.example.data.entity.PeriodLogEntity
import com.example.data.model.PeriodStatus
import com.example.ui.EzigoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyScreen(
    viewModel: EzigoViewModel
) {
    val facultyList by viewModel.allFaculty.collectAsState()
    val periodLogs by viewModel.allPeriodLogs.collectAsState()
    val facultyAttendance by viewModel.allFacultyAttendance.collectAsState()
    val batches by viewModel.allBatches.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Faculty Directory, 1: Period Teaching Logs, 2: Faculty Attendance
    var showAddFacultyDialog by remember { mutableStateOf(false) }
    var showAddPeriodLogDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddFacultyDialog = true },
                    containerColor = EzigoNavy,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Add Faculty") }
                )
            } else if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddPeriodLogDialog = true },
                    containerColor = EzigoNavy,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PostAdd, contentDescription = null) },
                    text = { Text("Log Period") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Faculty & Teaching Workload",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = EzigoNavy
            )
            Text(
                text = "Track faculty profiles, daily working hours, and curriculum period logs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TabRow(
                selectedTabIndex = selectedTab,
                divider = { Divider(color = EzigoBorder) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Faculty Directory (${facultyList.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Teaching Logs (${periodLogs.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Daily Attendance") }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> FacultyDirectoryView(facultyList, periodLogs)
                    1 -> PeriodLogsView(periodLogs, facultyList, batches)
                    2 -> FacultyAttendanceView(facultyList, facultyAttendance, viewModel)
                }
            }
        }
    }

    if (showAddFacultyDialog) {
        AddFacultyDialog(
            onDismiss = { showAddFacultyDialog = false },
            onSave = { faculty ->
                viewModel.saveFaculty(faculty) {
                    showAddFacultyDialog = false
                }
            }
        )
    }

    if (showAddPeriodLogDialog) {
        AddPeriodLogDialog(
            facultyList = facultyList,
            batches = batches,
            onDismiss = { showAddPeriodLogDialog = false },
            onSave = { log ->
                viewModel.savePeriodLog(log) {
                    showAddPeriodLogDialog = false
                }
            }
        )
    }
}

@Composable
fun FacultyDirectoryView(
    facultyList: List<FacultyEntity>,
    periodLogs: List<PeriodLogEntity>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(facultyList) { fac ->
            val logsCount = periodLogs.count { it.facultyId == fac.id }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(EzigoNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fac.fullName.take(2).uppercase(),
                            color = EzigoGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(fac.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(text = fac.facultyId, type = BadgeType.INFO)
                        }
                        Text(
                            text = "Subjects: ${fac.assignedSubjects}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Qualification: ${fac.qualification} • Batches: ${fac.assignedBatches}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Phone: ${fac.phone} • Workload: $logsCount Teaching Periods",
                            style = MaterialTheme.typography.labelSmall,
                            color = EzigoTextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodLogsView(
    periodLogs: List<PeriodLogEntity>,
    facultyList: List<FacultyEntity>,
    batches: List<BatchEntity>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(periodLogs) { log ->
            val fac = facultyList.find { it.id == log.facultyId }
            val batch = batches.find { it.id == log.batchId }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${log.subject} • ${log.periodNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                text = log.status.name,
                                type = when (log.status) {
                                    PeriodStatus.COMPLETED -> BadgeType.SUCCESS
                                    PeriodStatus.IN_PROGRESS -> BadgeType.INFO
                                    PeriodStatus.RESCHEDULED -> BadgeType.WARNING
                                    PeriodStatus.CANCELLED -> BadgeType.DANGER
                                }
                            )
                        }
                        Text(
                            text = log.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = EzigoTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Topic: ${log.topicCovered}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Faculty: ${fac?.fullName ?: "Faculty #${log.facultyId}"} • Batch: ${batch?.batchName ?: "Batch"} (${log.startTime} - ${log.endTime})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.remarks.isNotBlank()) {
                        Text(
                            text = "Notes: ${log.remarks}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EzigoTextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FacultyAttendanceView(
    facultyList: List<FacultyEntity>,
    facultyAttendance: List<FacultyAttendanceEntity>,
    viewModel: EzigoViewModel
) {
    val today = "2026-09-16"
    val todayAttendance = facultyAttendance.filter { it.date == today }.associateBy { it.facultyId }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Working Duty Log", fontWeight = FontWeight.Bold)
                        Text("Date: $today • Chapter Educational Institute", style = MaterialTheme.typography.bodySmall)
                    }
                    val presentCount = todayAttendance.values.count { it.status == "Present" }
                    StatusBadge(text = "$presentCount / ${facultyList.size} Present", type = BadgeType.SUCCESS)
                }
            }
        }

        items(facultyList) { fac ->
            val att = todayAttendance[fac.id]
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(fac.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = if (att != null) "In: ${att.checkIn} | Out: ${att.checkOut}" else "Not checked in yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(
                            text = att?.status ?: "Pending",
                            type = if (att?.status == "Present") BadgeType.SUCCESS else BadgeType.WARNING
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            viewModel.saveFacultyAttendance(
                                FacultyAttendanceEntity(
                                    facultyId = fac.id,
                                    date = today,
                                    checkIn = "09:00 AM",
                                    checkOut = "05:00 PM",
                                    status = if (att?.status == "Present") "Absent" else "Present",
                                    remarks = "Regular Schedule"
                                )
                            )
                        }) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Toggle Status", tint = EzigoNavy)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddFacultyDialog(
    onDismiss: () -> Unit,
    onSave: (FacultyEntity) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var facultyId by remember { mutableStateOf("FAC-004") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var subjects by remember { mutableStateOf("") }
    var batches by remember { mutableStateOf("Plus One Science") }
    var qualification by remember { mutableStateOf("M.Sc, B.Ed") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add New Faculty Member", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Divider()

                OutlinedTextField(value = facultyId, onValueChange = { facultyId = it }, label = { Text("Faculty ID (e.g. FAC-004)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subjects, onValueChange = { subjects = it }, label = { Text("Assigned Subjects") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qualification, onValueChange = { qualification = it }, label = { Text("Qualification") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fullName.isNotBlank()) {
                                onSave(
                                    FacultyEntity(
                                        facultyId = facultyId,
                                        fullName = fullName,
                                        phone = phone,
                                        email = email,
                                        assignedSubjects = subjects,
                                        assignedBatches = batches,
                                        joiningDate = "2026-06-01",
                                        qualification = qualification
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                    ) {
                        Text("Save Faculty")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPeriodLogDialog(
    facultyList: List<FacultyEntity>,
    batches: List<BatchEntity>,
    onDismiss: () -> Unit,
    onSave: (PeriodLogEntity) -> Unit
) {
    var selectedFacultyId by remember { mutableStateOf(facultyList.firstOrNull()?.id ?: 1L) }
    var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 1L) }
    var subject by remember { mutableStateOf("Mathematics") }
    var periodNumber by remember { mutableStateOf("1st Hour") }
    var topicCovered by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:15 AM") }
    var endTime by remember { mutableStateOf("10:15 AM") }
    var remarks by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Log Teaching Period / Hour", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Divider()

                Text("Select Faculty", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(facultyList) { fac ->
                        FilterChip(
                            selected = selectedFacultyId == fac.id,
                            onClick = { selectedFacultyId = fac.id },
                            label = { Text(fac.fullName) }
                        )
                    }
                }

                Text("Select Batch", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(batches) { b ->
                        FilterChip(
                            selected = selectedBatchId == b.id,
                            onClick = { selectedBatchId = b.id },
                            label = { Text(b.batchName) }
                        )
                    }
                }

                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = periodNumber, onValueChange = { periodNumber = it }, label = { Text("Period (e.g. 1st Hour, Evening 1st Hour)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = topicCovered, onValueChange = { topicCovered = it }, label = { Text("Topic Covered *") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End Time") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Homework / Observations") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (topicCovered.isNotBlank()) {
                                onSave(
                                    PeriodLogEntity(
                                        date = "2026-09-16",
                                        facultyId = selectedFacultyId,
                                        batchId = selectedBatchId,
                                        subject = subject,
                                        periodNumber = periodNumber,
                                        startTime = startTime,
                                        endTime = endTime,
                                        topicCovered = topicCovered,
                                        status = PeriodStatus.COMPLETED,
                                        remarks = remarks
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                    ) {
                        Text("Save Log")
                    }
                }
            }
        }
    }
}
