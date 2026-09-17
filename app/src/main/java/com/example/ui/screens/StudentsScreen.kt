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
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.*
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentStatus
import com.example.ui.EzigoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: EzigoViewModel,
    initialSelectedStudentId: Long? = null,
    onGenerateReport: (StudentEntity, String) -> Unit = { _, _ -> }
) {
    val students by viewModel.allStudents.collectAsState()
    val batches by viewModel.allBatches.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val feeAccounts by viewModel.allStudentFeeAccounts.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val marks by viewModel.allExams.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val chapters by viewModel.allChapters.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf<Long?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<StudentStatus?>(null) }

    var studentToView by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Auto-open student if requested
    LaunchedEffect(initialSelectedStudentId, students) {
        if (initialSelectedStudentId != null) {
            studentToView = students.find { it.id == initialSelectedStudentId }
        }
    }

    // Filtered students
    val filteredStudents = remember(students, searchQuery, selectedBatchFilter, selectedStatusFilter) {
        students.filter { s ->
            val matchQuery = searchQuery.isBlank() ||
                    s.fullName.contains(searchQuery, ignoreCase = true) ||
                    s.studentId.contains(searchQuery, ignoreCase = true) ||
                    s.parentPhone.contains(searchQuery, ignoreCase = true) ||
                    s.course.contains(searchQuery, ignoreCase = true)
            val matchBatch = selectedBatchFilter == null || s.batchId == selectedBatchFilter
            val matchStatus = selectedStatusFilter == null || s.status == selectedStatusFilter
            matchQuery && matchBatch && matchStatus
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EzigoNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("New Admission") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, ID (e.g. EZ-2026), phone, or course") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Batch & Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedBatchFilter == null,
                        onClick = { selectedBatchFilter = null },
                        label = { Text("All Batches (${students.size})") }
                    )
                }
                items(batches) { batch ->
                    FilterChip(
                        selected = selectedBatchFilter == batch.id,
                        onClick = { selectedBatchFilter = if (selectedBatchFilter == batch.id) null else batch.id },
                        label = { Text(batch.batchName) }
                    )
                }
            }

            // Results count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredStudents.size} enrolled students",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Student List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = EzigoTextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching student records found", color = EzigoTextMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val batch = batches.find { it.id == student.batchId }
                        Card(
                            onClick = { studentToView = student },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Avatar circle with initials
                                val initials = student.fullName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2).joinToString("")

                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(EzigoNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = EzigoGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = student.fullName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        StatusBadge(
                                            text = student.studentId,
                                            type = BadgeType.INFO
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${batch?.batchName ?: student.course} • Phone: ${student.parentPhone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Guardian: ${student.parentGuardianName} • ${student.schoolCollegeName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    IconButton(onClick = { studentToEdit = student }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EzigoNavy)
                                    }
                                    if (viewModel.canManageUsersOrConfig()) {
                                        IconButton(onClick = { studentToDelete = student }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = EzigoDanger)
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

    // Add / Edit Student Dialog
    if (showAddDialog || studentToEdit != null) {
        val editTarget = studentToEdit
        StudentFormDialog(
            initialStudent = editTarget,
            batches = batches,
            onDismiss = {
                showAddDialog = false
                studentToEdit = null
            },
            onSave = { student ->
                viewModel.saveStudent(student) {
                    showAddDialog = false
                    studentToEdit = null
                }
            },
            onGenerateId = {
                viewModel.repository.generateNextStudentId()
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { student ->
        ConfirmDialog(
            title = "Delete Student Record",
            message = "Are you sure you want to permanently delete student '${student.fullName}' (${student.studentId})? This will preserve audit trails.",
            confirmText = "Delete Student",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteStudent(student)
                studentToDelete = null
            },
            onDismiss = { studentToDelete = null }
        )
    }

    // 7-Tab Student Detail Profile Dialog / Modal
    studentToView?.let { student ->
        StudentProfileModal(
            student = student,
            batch = batches.find { it.id == student.batchId },
            attendanceList = attendance.filter { it.studentId == student.id },
            feeAccounts = feeAccounts.filter { it.studentId == student.id },
            payments = payments.filter { it.studentId == student.id },
            subjects = subjects.filter { it.batchId == student.batchId },
            chapters = chapters.filter { it.batchId == student.batchId },
            onDismiss = { studentToView = null },
            onEdit = {
                val s = studentToView
                studentToView = null
                studentToEdit = s
            },
            onGenerateReport = { type ->
                onGenerateReport(student, type)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    initialStudent: StudentEntity?,
    batches: List<BatchEntity>,
    onDismiss: () -> Unit,
    onSave: (StudentEntity) -> Unit,
    onGenerateId: suspend () -> String
) {
    var fullName by remember { mutableStateOf(initialStudent?.fullName ?: "") }
    var studentId by remember { mutableStateOf(initialStudent?.studentId ?: "") }
    var dateOfBirth by remember { mutableStateOf(initialStudent?.dateOfBirth ?: "2009-05-15") }
    var gender by remember { mutableStateOf(initialStudent?.gender ?: "Male") }
    var parentName by remember { mutableStateOf(initialStudent?.parentGuardianName ?: "") }
    var parentPhone by remember { mutableStateOf(initialStudent?.parentPhone ?: "") }
    var whatsappNumber by remember { mutableStateOf(initialStudent?.whatsappNumber ?: "") }
    var address by remember { mutableStateOf(initialStudent?.residentialAddress ?: "Thazhava, Karunagappally") }
    var schoolName by remember { mutableStateOf(initialStudent?.schoolCollegeName ?: "") }
    var selectedBatchId by remember { mutableStateOf(initialStudent?.batchId ?: batches.firstOrNull()?.id ?: 1L) }
    var admissionDate by remember { mutableStateOf(initialStudent?.admissionDate ?: "2026-06-01") }
    var academicYear by remember { mutableStateOf(initialStudent?.academicYear ?: "2026-2027") }
    var assignedSubjects by remember { mutableStateOf(initialStudent?.assignedSubjects ?: "Mathematics, Physics, Chemistry, Biology") }
    var notes by remember { mutableStateOf(initialStudent?.notes ?: "") }
    var status by remember { mutableStateOf(initialStudent?.status ?: StudentStatus.ACTIVE) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (initialStudent == null && studentId.isBlank()) {
            studentId = onGenerateId()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialStudent == null) "New Student Admission" else "Edit Student Record",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = EzigoNavy
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                if (errorMessage != null) {
                    Surface(
                        color = EzigoDangerBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = EzigoDanger,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it },
                            label = { Text("Student ID (Admission No)") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("e.g. EZ-2026-001 (Configurable Prefix)") }
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it },
                                label = { Text("Date of Birth (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                label = { Text("Gender") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = parentName,
                            onValueChange = { parentName = it },
                            label = { Text("Parent / Guardian Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = parentPhone,
                                onValueChange = { parentPhone = it },
                                label = { Text("Parent Phone *") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = whatsappNumber,
                                onValueChange = { whatsappNumber = it },
                                label = { Text("WhatsApp Number") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Residential Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = schoolName,
                            onValueChange = { schoolName = it },
                            label = { Text("School / College Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Batch / Course *", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(batches) { b ->
                                FilterChip(
                                    selected = selectedBatchId == b.id,
                                    onClick = { selectedBatchId = b.id },
                                    label = { Text(b.batchName) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = assignedSubjects,
                            onValueChange = { assignedSubjects = it },
                            label = { Text("Assigned Subjects (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Observations") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                errorMessage = "Please enter student's full name"
                                return@Button
                            }
                            if (parentName.isBlank()) {
                                errorMessage = "Parent/Guardian name is required"
                                return@Button
                            }
                            if (parentPhone.isBlank()) {
                                errorMessage = "Parent contact number is required"
                                return@Button
                            }
                            val selectedBatch = batches.find { it.id == selectedBatchId }
                            val newStudent = (initialStudent ?: StudentEntity(
                                studentId = studentId,
                                fullName = fullName,
                                dateOfBirth = dateOfBirth,
                                gender = gender,
                                parentGuardianName = parentName,
                                parentPhone = parentPhone,
                                whatsappNumber = if (whatsappNumber.isNotBlank()) whatsappNumber else parentPhone,
                                residentialAddress = address,
                                schoolCollegeName = schoolName,
                                course = selectedBatch?.course ?: "Higher Secondary",
                                batchId = selectedBatchId,
                                admissionDate = admissionDate,
                                academicYear = academicYear,
                                assignedSubjects = assignedSubjects,
                                status = status,
                                notes = notes
                            )).copy(
                                studentId = studentId,
                                fullName = fullName,
                                dateOfBirth = dateOfBirth,
                                gender = gender,
                                parentGuardianName = parentName,
                                parentPhone = parentPhone,
                                whatsappNumber = if (whatsappNumber.isNotBlank()) whatsappNumber else parentPhone,
                                residentialAddress = address,
                                schoolCollegeName = schoolName,
                                course = selectedBatch?.course ?: "Higher Secondary",
                                batchId = selectedBatchId,
                                admissionDate = admissionDate,
                                academicYear = academicYear,
                                assignedSubjects = assignedSubjects,
                                status = status,
                                notes = notes
                            )
                            onSave(newStudent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                    ) {
                        Text(if (initialStudent == null) "Register Student" else "Update Record")
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileModal(
    student: StudentEntity,
    batch: BatchEntity?,
    attendanceList: List<AttendanceEntity>,
    feeAccounts: List<StudentFeeAccountEntity>,
    payments: List<PaymentEntity>,
    subjects: List<SubjectEntity>,
    chapters: List<SyllabusChapterEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onGenerateReport: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Personal", "Academic", "Attendance", "Fees", "Exam Marks", "Syllabus", "Reports")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header with photo placeholder & details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(EzigoNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.fullName.take(2).uppercase(),
                                color = EzigoGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(text = student.studentId, type = BadgeType.INFO)
                            }
                            Text(
                                text = "${batch?.batchName ?: "Batch"} • Status: ${student.status.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    divider = { Divider(color = EzigoBorder) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> PersonalDetailsTab(student)
                        1 -> AcademicDetailsTab(student, batch)
                        2 -> AttendanceDetailsTab(student, attendanceList)
                        3 -> FeesDetailsTab(student, feeAccounts, payments)
                        4 -> ExamMarksTab(student)
                        5 -> SyllabusProgressTab(student, subjects, chapters)
                        6 -> ReportsTab(student, onGenerateReport)
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalDetailsTab(student: StudentEntity) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            ProfileFieldCard("Parent / Guardian", student.parentGuardianName)
        }
        item {
            ProfileFieldCard("Parent Phone Number", student.parentPhone)
        }
        item {
            ProfileFieldCard("WhatsApp Number", student.whatsappNumber)
        }
        item {
            ProfileFieldCard("Date of Birth", student.dateOfBirth)
        }
        item {
            ProfileFieldCard("Gender", student.gender)
        }
        item {
            ProfileFieldCard("Residential Address", student.residentialAddress)
        }
        item {
            ProfileFieldCard("Previous School / College", student.schoolCollegeName)
        }
        item {
            ProfileFieldCard("Administrative Notes", student.notes.ifBlank { "No special notes recorded." })
        }
    }
}

@Composable
fun AcademicDetailsTab(student: StudentEntity, batch: BatchEntity?) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            ProfileFieldCard("Course Enrolled", student.course)
        }
        item {
            ProfileFieldCard("Assigned Batch", batch?.batchName ?: "N/A")
        }
        item {
            ProfileFieldCard("Academic Year", student.academicYear)
        }
        item {
            ProfileFieldCard("Admission Date", student.admissionDate)
        }
        item {
            ProfileFieldCard("Assigned Subjects", student.assignedSubjects)
        }
        item {
            ProfileFieldCard("Session Schedule", batch?.sessionType ?: "Regular & Evening")
        }
    }
}

@Composable
fun AttendanceDetailsTab(student: StudentEntity, attendanceList: List<AttendanceEntity>) {
    val morningAtt = attendanceList.filter { it.session == AttendanceSession.MORNING }
    val eveningAtt = attendanceList.filter { it.session == AttendanceSession.EVENING }

    val morningPres = morningAtt.count { it.status == AttendanceStatus.PRESENT }
    val morningPct = if (morningAtt.isNotEmpty()) (morningPres * 100.0 / morningAtt.size) else 0.0

    val eveningPres = eveningAtt.count { it.status == AttendanceStatus.PRESENT }
    val eveningPct = if (eveningAtt.isNotEmpty()) (eveningPres * 100.0 / eveningAtt.size) else 0.0

    val totalSessions = morningAtt.size + eveningAtt.size
    val totalPres = morningPres + eveningPres
    val overallPct = if (totalSessions > 0) (totalPres * 100.0 / totalSessions) else 0.0

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Attendance Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Morning Session", style = MaterialTheme.typography.labelMedium)
                            Text("$morningPres / ${morningAtt.size} (${String.format("%.1f%%", morningPct)})", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Evening Session", style = MaterialTheme.typography.labelMedium)
                            Text("$eveningPres / ${eveningAtt.size} (${String.format("%.1f%%", eveningPct)})", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Overall Record", style = MaterialTheme.typography.labelMedium)
                            val isLow = overallPct < 75.0
                            Text(
                                "$totalPres / $totalSessions (${String.format("%.1f%%", overallPct)})",
                                fontWeight = FontWeight.Bold,
                                color = if (isLow) EzigoDanger else EzigoSuccess
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Session Log History", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        items(attendanceList.sortedByDescending { it.date }) { att ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "${att.date} • ${att.session.name}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (att.remarks.isNotBlank()) {
                            Text(text = "Remarks: ${att.remarks}", style = MaterialTheme.typography.bodySmall, color = EzigoTextMuted)
                        }
                    }
                    StatusBadge(
                        text = att.status.name,
                        type = when (att.status) {
                            AttendanceStatus.PRESENT -> BadgeType.SUCCESS
                            AttendanceStatus.ABSENT -> BadgeType.DANGER
                            AttendanceStatus.LEAVE -> BadgeType.WARNING
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FeesDetailsTab(
    student: StudentEntity,
    feeAccounts: List<StudentFeeAccountEntity>,
    payments: List<PaymentEntity>
) {
    val totalBilled = feeAccounts.sumOf { it.totalAmount }
    val totalPaid = feeAccounts.sumOf { it.paidAmount }
    val totalPending = feeAccounts.sumOf { it.pendingAmount }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EzigoNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Fixed Fee", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
                        Text("₹${String.format("%,.0f", totalBilled)}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Total Paid", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
                        Text("₹${String.format("%,.0f", totalPaid)}", color = EzigoSuccess, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Pending Balance", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
                        Text("₹${String.format("%,.0f", totalPending)}", color = if (totalPending > 0) EzigoGold else EzigoSuccess, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Fee Breakdown Accounts", fontWeight = FontWeight.Bold)
        }

        items(feeAccounts) { acc ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(acc.feeCategory.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Fixed: ₹${String.format("%,.0f", acc.totalAmount)} | Paid: ₹${String.format("%,.0f", acc.paidAmount)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (acc.pendingAmount > 0) "Due: ₹${String.format("%,.0f", acc.pendingAmount)}" else "Cleared",
                            fontWeight = FontWeight.Bold,
                            color = if (acc.pendingAmount > 0) EzigoDanger else EzigoSuccess
                        )
                        StatusBadge(text = acc.status.name, type = if (acc.status.name == "CLEARED") BadgeType.SUCCESS else BadgeType.WARNING)
                    }
                }
            }
        }

        item {
            Text("Payment History (${payments.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        items(payments) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(p.receiptNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${p.feeCategory.displayName} • ${p.paymentMethod.displayName} • ${p.paymentDate}", style = MaterialTheme.typography.labelSmall)
                    }
                    Text("₹${String.format("%,.0f", p.amount)}", fontWeight = FontWeight.Bold, color = EzigoSuccess)
                }
            }
        }
    }
}

@Composable
fun ExamMarksTab(student: StudentEntity) {
    // Sample mark presentation from terminal examination
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Terminal Examination 2026", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Higher Secondary Science • Maximum Marks: 300", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Mock marks preview for the student
        val isAbhin = student.fullName.contains("Abhin", ignoreCase = true)
        val isAshtami = student.fullName.contains("Ashtami", ignoreCase = true)

        val marksList = when {
            isAshtami -> listOf(Triple("Mathematics", 90.0, 100.0), Triple("Physics", 88.0, 100.0), Triple("Chemistry", 92.0, 100.0))
            isAbhin -> listOf(Triple("Mathematics", 85.0, 100.0), Triple("Physics", 78.0, 100.0), Triple("Chemistry", 90.0, 100.0))
            else -> listOf(Triple("Mathematics", 42.0, 100.0), Triple("Physics", 38.0, 100.0), Triple("Chemistry", 45.0, 100.0))
        }

        val total = marksList.sumOf { it.second }
        val maxTotal = marksList.sumOf { it.third }
        val pct = (total / maxTotal) * 100.0
        val grade = when {
            pct >= 90 -> "A+"
            pct >= 80 -> "A"
            pct >= 70 -> "B+"
            pct >= 60 -> "B"
            pct >= 50 -> "C"
            else -> "D"
        }
        val result = if (pct >= 50) "Passed" else "Needs Improvement"

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    marksList.forEach { (sub, score, max) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(sub, fontSize = 14.sp)
                            Text("${score.toInt()} / ${max.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total: ${total.toInt()} / ${maxTotal.toInt()} (${String.format("%.2f%%", pct)})", fontWeight = FontWeight.Bold)
                        Row {
                            StatusBadge(text = "Grade: $grade", type = BadgeType.SUCCESS)
                            Spacer(modifier = Modifier.width(4.dp))
                            StatusBadge(text = result, type = if (pct >= 50) BadgeType.SUCCESS else BadgeType.WARNING)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SyllabusProgressTab(
    student: StudentEntity,
    subjects: List<SubjectEntity>,
    chapters: List<SyllabusChapterEntity>
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Subject-wise Curriculum Status", fontWeight = FontWeight.Bold)
        }
        items(subjects) { subject ->
            val subChapters = chapters.filter { it.subjectId == subject.id }
            val completed = subChapters.count { it.status.name == "COMPLETED" || it.status.name == "REVISION" }
            val total = subChapters.size
            val pct = if (total > 0) (completed * 100.0 / total) else 75.0

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(subject.subjectName, fontWeight = FontWeight.Bold)
                        Text(String.format("%.1f%%", pct), fontWeight = FontWeight.Bold, color = EzigoSuccess)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EzigoSuccess,
                        trackColor = EzigoBorder
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$completed of $total chapters covered", style = MaterialTheme.typography.labelSmall, color = EzigoTextMuted)
                }
            }
        }
    }
}

@Composable
fun ReportsTab(
    student: StudentEntity,
    onGenerateReport: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Generate Institutional Documents", fontWeight = FontWeight.Bold)
        Text("Print-ready standardized reports for Chapter Educational Institute", style = MaterialTheme.typography.bodySmall, color = EzigoTextMuted)

        Card(
            onClick = { onGenerateReport("PORTFOLIO") },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBox, contentDescription = null, tint = EzigoNavy)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Individual Student Portfolio", fontWeight = FontWeight.Bold)
                    Text("Complete academic, attendance, and fee history sheet", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(
            onClick = { onGenerateReport("ID_CARD") },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = EzigoGold)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Student ID Card Data Sheet", fontWeight = FontWeight.Bold)
                    Text("Official institutional identification credentials", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(
            onClick = { onGenerateReport("PROGRESS_CARD") },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Grade, contentDescription = null, tint = EzigoSuccess)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Academic Progress & Marks Card", fontWeight = FontWeight.Bold)
                    Text("Terminal examination report with grades and remarks", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(
            onClick = { onGenerateReport("FEE_STATEMENT") },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF7C3AED))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Student Outstanding Fee Statement", fontWeight = FontWeight.Bold)
                    Text("Itemized fee balances and official receipts", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ProfileFieldCard(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = EzigoTextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}
