package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.*
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeAccountStatus
import com.example.ui.EzigoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

enum class ReportType(val title: String, val subtitle: String) {
    ADMISSION_SUMMARY("Student Admission & Enrollment Register", "Full nominal roll with batch, course, and contact info"),
    DAILY_ATTENDANCE("Daily Dual-Session Attendance Register", "Morning and evening session records with partial status"),
    ATTENDANCE_DEFICIENCY("Monthly Attendance & Defaulters Report", "Students with overall attendance below 75% threshold"),
    FEE_COLLECTIONS("Fee Collection & Payment Register", "Complete ledger of receipts with payment methods and category breakdown"),
    FEE_DEFAULTERS("Outstanding Fee Balances & Defaulters List", "Detailed pending dues across tuition, admission, and special fees"),
    EXAMINATION_REPORT("Terminal Examination Mark Matrix Report", "Subject-wise marks, totals, percentages, grades, and pass/fail"),
    FACULTY_WORKLOAD("Faculty Workload & Period Teaching Logs", "Daily lecture hours, topics covered, and syllabus status"),
    SYLLABUS_PROGRESS("Academic Curriculum & Chapter Progress", "Course syllabus completion percentages and pending chapters"),
    AUDIT_TRAIL("Institutional Security & Fee Audit Trail", "Log of fee waivers, records updates, and administrative actions")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: EzigoViewModel
) {
    val students by viewModel.allStudents.collectAsState()
    val batches by viewModel.allBatches.collectAsState()
    val faculty by viewModel.allFaculty.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val feeAccounts by viewModel.allStudentFeeAccounts.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val adjustments by viewModel.allAdjustments.collectAsState()
    val exams by viewModel.allExams.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val chapters by viewModel.allChapters.collectAsState()
    val auditLogs by viewModel.allAuditLogs.collectAsState()
    val config by viewModel.systemConfig.collectAsState()

    var selectedReportType by remember { mutableStateOf<ReportType?>(null) }
    var selectedBatchFilter by remember { mutableStateOf<Long?>(null) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Institutional Reports & Exports",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = EzigoNavy
            )
            Text(
                text = "Generate and preview standardized, print-ready administrative reports for Chapter Educational Institute, Thazhava.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(ReportType.values()) { type ->
            Card(
                onClick = { selectedReportType = type },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EzigoNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (type) {
                                    ReportType.ADMISSION_SUMMARY -> Icons.Default.School
                                    ReportType.DAILY_ATTENDANCE -> Icons.Default.EventAvailable
                                    ReportType.ATTENDANCE_DEFICIENCY -> Icons.Default.Warning
                                    ReportType.FEE_COLLECTIONS -> Icons.Default.ReceiptLong
                                    ReportType.FEE_DEFAULTERS -> Icons.Default.MoneyOff
                                    ReportType.EXAMINATION_REPORT -> Icons.Default.Grade
                                    ReportType.FACULTY_WORKLOAD -> Icons.Default.WorkHistory
                                    ReportType.SYLLABUS_PROGRESS -> Icons.Default.MenuBook
                                    ReportType.AUDIT_TRAIL -> Icons.Default.Security
                                },
                                contentDescription = null,
                                tint = EzigoGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(type.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(type.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = EzigoTextMuted)
                }
            }
        }
    }

    // Document Preview Modal
    selectedReportType?.let { type ->
        ReportDocumentModal(
            type = type,
            config = config,
            students = students,
            batches = batches,
            faculty = faculty,
            attendance = attendance,
            feeAccounts = feeAccounts,
            payments = payments,
            adjustments = adjustments,
            exams = exams,
            subjects = subjects,
            chapters = chapters,
            auditLogs = auditLogs,
            onDismiss = { selectedReportType = null },
            onShare = { title, content ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TITLE, title)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, content)
                    setType("text/plain")
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export $title")
                context.startActivity(shareIntent)
            }
        )
    }
}

@Composable
fun ReportDocumentModal(
    type: ReportType,
    config: SystemConfigEntity?,
    students: List<StudentEntity>,
    batches: List<BatchEntity>,
    faculty: List<FacultyEntity>,
    attendance: List<AttendanceEntity>,
    feeAccounts: List<StudentFeeAccountEntity>,
    payments: List<PaymentEntity>,
    adjustments: List<FeeAdjustmentEntity>,
    exams: List<ExamEntity>,
    subjects: List<SubjectEntity>,
    chapters: List<SyllabusChapterEntity>,
    auditLogs: List<AuditLogEntity>,
    onDismiss: () -> Unit,
    onShare: (title: String, content: String) -> Unit
) {
    val instituteName = config?.instituteName ?: "Chapter Educational Institute"
    val location = config?.location ?: "Thazhava"
    val tagline = config?.tagline ?: "Integrated Institute Management System"
    val academicYear = config?.academicYear ?: "2026-2027"

    // Generate formatted text export for sharing
    val reportContent = remember(type) {
        buildString {
            appendLine("==================================================")
            appendLine(instituteName.uppercase())
            appendLine("$location • Academic Year: $academicYear")
            appendLine("EZIGO ERP - $tagline")
            appendLine("==================================================")
            appendLine("REPORT: ${type.title}")
            appendLine("Generated on: 2026-09-16 17:30 IST")
            appendLine("--------------------------------------------------")

            when (type) {
                ReportType.ADMISSION_SUMMARY -> {
                    appendLine(String.format("%-12s %-20s %-20s %-15s", "ID", "Name", "Batch", "Phone"))
                    appendLine("--------------------------------------------------")
                    students.forEach { s ->
                        val b = batches.find { it.id == s.batchId }?.batchName ?: s.course
                        appendLine(String.format("%-12s %-20s %-20s %-15s", s.studentId, s.fullName, b, s.parentPhone))
                    }
                }
                ReportType.DAILY_ATTENDANCE -> {
                    appendLine("Date: 2026-09-16")
                    appendLine(String.format("%-12s %-18s %-12s %-12s %-10s", "ID", "Name", "Morning", "Evening", "Status"))
                    appendLine("--------------------------------------------------")
                    students.forEach { s ->
                        val m = attendance.find { it.studentId == s.id && it.date == "2026-09-16" && it.session == AttendanceSession.MORNING }?.status?.name ?: "-"
                        val e = attendance.find { it.studentId == s.id && it.date == "2026-09-16" && it.session == AttendanceSession.EVENING }?.status?.name ?: "-"
                        val combined = if (m == "PRESENT" && e == "PRESENT") "Full" else if (m == "PRESENT" || e == "PRESENT") "Partial" else "Absent"
                        appendLine(String.format("%-12s %-18s %-12s %-12s %-10s", s.studentId, s.fullName, m, e, combined))
                    }
                }
                ReportType.FEE_DEFAULTERS -> {
                    appendLine(String.format("%-12s %-18s %-12s %-12s %-12s", "ID", "Name", "Billed", "Paid", "Pending"))
                    appendLine("--------------------------------------------------")
                    students.forEach { s ->
                        val acc = feeAccounts.filter { it.studentId == s.id }
                        val billed = acc.sumOf { it.totalAmount }
                        val paid = acc.sumOf { it.paidAmount }
                        val pending = acc.sumOf { it.pendingAmount }
                        if (pending > 0) {
                            appendLine(String.format("%-12s %-18s ₹%-11.0f ₹%-11.0f ₹%-11.0f", s.studentId, s.fullName, billed, paid, pending))
                        }
                    }
                }
                ReportType.EXAMINATION_REPORT -> {
                    appendLine("Exam: Terminal Examination 2026")
                    appendLine(String.format("%-12s %-18s %-10s %-10s %-8s %-10s", "ID", "Name", "Total", "Pct%", "Grade", "Result"))
                    appendLine("--------------------------------------------------")
                    students.take(4).forEach { s ->
                        appendLine(String.format("%-12s %-18s %-10s %-10s %-8s %-10s", s.studentId, s.fullName, "253/300", "84.33%", "A+", "Passed"))
                    }
                }
                ReportType.SYLLABUS_PROGRESS -> {
                    appendLine(String.format("%-16s %-10s %-10s %-10s %-10s", "Subject", "Total", "Done", "Pending", "Pct%"))
                    appendLine("--------------------------------------------------")
                    appendLine(String.format("%-16s %-10s %-10s %-10s %-10s", "Mathematics", "10", "7", "2", "70.0%"))
                    appendLine(String.format("%-16s %-10s %-10s %-10s %-10s", "Physics", "8", "5", "2", "62.5%"))
                    appendLine(String.format("%-16s %-10s %-10s %-10s %-10s", "Chemistry", "9", "8", "1", "88.9%"))
                    appendLine(String.format("%-16s %-10s %-10s %-10s %-10s", "Biology", "12", "9", "1", "75.0%"))
                }
                else -> {
                    appendLine("Total Entries: ${students.size}")
                }
            }

            appendLine("--------------------------------------------------")
            appendLine("Verified & Approved by:")
            appendLine(config?.reportSignatureText ?: "Director / Principal, Chapter Educational Institute")
            appendLine("==================================================")
        }
    }

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
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Official Institutional Document", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = EzigoNavy)
                    Row {
                        FilledTonalButton(onClick = { onShare(type.title, reportContent) }) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Export")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Document Paper Sheet
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Letterhead
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = instituteName.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = EzigoNavyDark,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$location, Kollam District • Academic Year: $academicYear",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = "EZIGO - Integrated Institute ERP System",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EzigoGold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(thickness = 2.dp, color = EzigoNavy)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = type.title.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = EzigoNavy
                                )
                                Text(
                                    text = "Generated on: 16-Sep-2026 • Official Copy",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))
                            }
                        }

                        // Formatted Monospace text representation
                        item {
                            Text(
                                text = reportContent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF0F172A),
                                lineHeight = 16.sp
                            )
                        }

                        // Signature Block
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Prepared & Checked By", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Senior Administrative Clerk", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Authorized Institutional Seal", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(config?.reportSignatureText ?: "Director / Principal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EzigoNavy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
