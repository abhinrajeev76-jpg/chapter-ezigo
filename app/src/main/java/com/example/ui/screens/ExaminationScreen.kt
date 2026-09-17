package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.entity.*
import com.example.ui.EzigoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

data class StudentMarkRow(
    val student: StudentEntity,
    val subjectMarks: Map<Long, Double>,
    val totalObtained: Double,
    val totalMax: Double,
    val percentage: Double,
    val grade: String,
    val result: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExaminationScreen(
    viewModel: EzigoViewModel,
    onGenerateProgressCard: (StudentEntity, ExamEntity) -> Unit = { _, _ -> }
) {
    val exams by viewModel.allExams.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val batches by viewModel.allBatches.collectAsState()

    var selectedExamId by remember { mutableStateOf(exams.firstOrNull()?.id ?: 1L) }
    var showEditMarksForStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var showAddExamDialog by remember { mutableStateOf(false) }

    val currentExam = exams.find { it.id == selectedExamId } ?: exams.firstOrNull()

    // Get marks for current exam
    val marksList by produceState<List<ExamMarkEntity>>(initialValue = emptyList(), key1 = selectedExamId) {
        if (selectedExamId > 0) {
            viewModel.repository.getMarksForExam(selectedExamId).collect {
                value = it
            }
        }
    }

    // Filter subjects for the exam's batch
    val examSubjects = remember(subjects, currentExam) {
        if (currentExam != null) {
            subjects.filter { it.batchId == currentExam.batchId }
        } else subjects
    }

    // Filter students for the exam's batch
    val examStudents = remember(students, currentExam) {
        if (currentExam != null) {
            students.filter { it.batchId == currentExam.batchId }
        } else students
    }

    // Build the calculation matrix
    val matrixRows = remember(examStudents, examSubjects, marksList) {
        examStudents.map { student ->
            val studentMarks = marksList.filter { it.studentId == student.id }
            val map = mutableMapOf<Long, Double>()
            examSubjects.forEach { sub ->
                val m = studentMarks.find { it.subjectId == sub.id }
                if (m != null) {
                    map[sub.id] = m.marksObtained
                }
            }

            val totalObtained = map.values.sum()
            val totalMax = examSubjects.sumOf { it.maximumMarks }
            val pct = if (totalMax > 0) (totalObtained / totalMax) * 100.0 else 0.0

            val grade = when {
                pct >= 90.0 -> "A+"
                pct >= 80.0 -> "A"
                pct >= 70.0 -> "B+"
                pct >= 60.0 -> "B"
                pct >= 50.0 -> "C"
                else -> "D"
            }

            val passed = pct >= 50.0 && map.values.all { it >= 35.0 }
            val result = if (passed) "Passed" else "Needs Improvement"

            StudentMarkRow(
                student = student,
                subjectMarks = map,
                totalObtained = totalObtained,
                totalMax = totalMax,
                percentage = pct,
                grade = grade,
                result = result
            )
        }
    }

    Scaffold(
        floatingActionButton = {
            if (viewModel.canManageUsersOrConfig()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddExamDialog = true },
                    containerColor = EzigoNavy,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Schedule Exam") }
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
                text = "Examinations & Marks Matrix",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = EzigoNavy
            )
            Text(
                text = "Track terminal, mid-term, and model examinations with automated grade, percentage, and pass/fail calculations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Exam Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exams) { exam ->
                    FilterChip(
                        selected = selectedExamId == exam.id,
                        onClick = { selectedExamId = exam.id },
                        label = { Text("${exam.examName} (${exam.examType})") },
                        leadingIcon = {
                            if (selectedExamId == exam.id) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }

            // Exam Header Card
            currentExam?.let { exam ->
                val batch = batches.find { it.id == exam.batchId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(exam.examName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Batch: ${batch?.batchName ?: "All"} • Date: ${exam.examDate} • Max: ${exam.totalMarks.toInt()}", style = MaterialTheme.typography.bodySmall)
                        }
                        StatusBadge(text = exam.status, type = BadgeType.SUCCESS)
                    }
                }
            }

            // Interactive Marks Matrix Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Text(
                        text = "Official Academic Mark Matrix",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal scrolling table
                    val horizontalScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp))
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Student Name", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(150.dp))
                            Text("Admission ID", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(100.dp))
                            examSubjects.forEach { sub ->
                                Text(sub.subjectName, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(110.dp))
                            }
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(90.dp))
                            Text("Percentage", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(100.dp))
                            Text("Grade", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(80.dp))
                            Text("Result", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(110.dp))
                            Text("Actions", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(120.dp))
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = EzigoBorder)

                        // Data Rows
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(matrixRows) { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(row.student.fullName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.width(150.dp))
                                    Text(row.student.studentId, fontSize = 12.sp, color = EzigoTextMuted, modifier = Modifier.width(100.dp))

                                    examSubjects.forEach { sub ->
                                        val score = row.subjectMarks[sub.id]
                                        val scoreText = if (score != null) "${score.toInt()}" else "-"
                                        Text(scoreText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.width(110.dp))
                                    }

                                    Text("${row.totalObtained.toInt()}/${row.totalMax.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(90.dp))
                                    Text(String.format("%.2f%%", row.percentage), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(100.dp))

                                    Box(modifier = Modifier.width(80.dp)) {
                                        StatusBadge(text = row.grade, type = if (row.grade.startsWith("A")) BadgeType.SUCCESS else if (row.grade.startsWith("B")) BadgeType.INFO else BadgeType.WARNING)
                                    }

                                    Box(modifier = Modifier.width(110.dp)) {
                                        StatusBadge(text = row.result, type = if (row.result == "Passed") BadgeType.SUCCESS else BadgeType.DANGER)
                                    }

                                    Row(modifier = Modifier.width(120.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (viewModel.canEditMarks()) {
                                            IconButton(onClick = { showEditMarksForStudent = row.student }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Marks", tint = EzigoNavy, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        currentExam?.let { ex ->
                                            IconButton(onClick = { onGenerateProgressCard(row.student, ex) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Print, contentDescription = "Progress Card", tint = EzigoSuccess, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                                Divider(color = EzigoBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Marks Dialog for a student
    showEditMarksForStudent?.let { student ->
        currentExam?.let { exam ->
            EditMarksDialog(
                student = student,
                exam = exam,
                subjects = examSubjects,
                existingMarks = marksList.filter { it.studentId == student.id },
                onDismiss = { showEditMarksForStudent = null },
                onSave = { updatedList ->
                    viewModel.saveExamMarks(updatedList) {
                        showEditMarksForStudent = null
                    }
                }
            )
        }
    }

    // Schedule Exam Dialog
    if (showAddExamDialog) {
        AddExamDialog(
            batches = batches,
            onDismiss = { showAddExamDialog = false },
            onSave = { newExam ->
                viewModel.saveExam(newExam) {
                    showAddExamDialog = false
                }
            }
        )
    }
}

@Composable
fun EditMarksDialog(
    student: StudentEntity,
    exam: ExamEntity,
    subjects: List<SubjectEntity>,
    existingMarks: List<ExamMarkEntity>,
    onDismiss: () -> Unit,
    onSave: (List<ExamMarkEntity>) -> Unit
) {
    val marksInputs = remember {
        mutableStateMapOf<Long, String>().apply {
            subjects.forEach { s ->
                val m = existingMarks.find { it.subjectId == s.id }
                this[s.id] = m?.marksObtained?.toInt()?.toString() ?: "75"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Enter / Modify Examination Marks", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = EzigoNavy)
                Text("Student: ${student.fullName} (${student.studentId}) • ${exam.examName}", fontWeight = FontWeight.SemiBold)
                Divider()

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f, fill = false)) {
                    items(subjects) { sub ->
                        OutlinedTextField(
                            value = marksInputs[sub.id] ?: "",
                            onValueChange = { marksInputs[sub.id] = it },
                            label = { Text("${sub.subjectName} (Max: ${sub.maximumMarks.toInt()})") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val list = subjects.map { sub ->
                                val score = marksInputs[sub.id]?.toDoubleOrNull() ?: 0.0
                                val ex = existingMarks.find { it.subjectId == sub.id }
                                (ex ?: ExamMarkEntity(
                                    examId = exam.id,
                                    studentId = student.id,
                                    subjectId = sub.id,
                                    marksObtained = score,
                                    maximumMarks = sub.maximumMarks,
                                    remarks = "Terminal evaluation",
                                    enteredBy = "Faculty"
                                )).copy(
                                    marksObtained = score,
                                    maximumMarks = sub.maximumMarks
                                )
                            }
                            onSave(list)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                    ) {
                        Text("Save Marks")
                    }
                }
            }
        }
    }
}

@Composable
fun AddExamDialog(
    batches: List<BatchEntity>,
    onDismiss: () -> Unit,
    onSave: (ExamEntity) -> Unit
) {
    var examName by remember { mutableStateOf("Model Examination 2026") }
    var examType by remember { mutableStateOf("Model") }
    var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 1L) }
    var examDate by remember { mutableStateOf("2026-11-20") }
    var totalMarks by remember { mutableStateOf("300") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Schedule New Examination", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Divider()

                OutlinedTextField(value = examName, onValueChange = { examName = it }, label = { Text("Exam Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = examType, onValueChange = { examType = it }, label = { Text("Exam Type (e.g. Terminal, Mid-Term, Model, Unit)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = examDate, onValueChange = { examDate = it }, label = { Text("Exam Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = totalMarks, onValueChange = { totalMarks = it }, label = { Text("Total Aggregate Marks") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (examName.isNotBlank()) {
                                onSave(
                                    ExamEntity(
                                        examName = examName,
                                        examType = examType,
                                        batchId = selectedBatchId,
                                        academicYear = "2026-2027",
                                        examDate = examDate,
                                        totalMarks = totalMarks.toDoubleOrNull() ?: 300.0,
                                        status = "Scheduled"
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                    ) {
                        Text("Schedule")
                    }
                }
            }
        }
    }
}
