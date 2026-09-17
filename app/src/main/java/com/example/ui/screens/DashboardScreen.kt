package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeAccountStatus
import com.example.ui.EzigoViewModel
import com.example.ui.Screen
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: EzigoViewModel,
    onNavigate: (Screen) -> Unit,
    onQuickAction: (String) -> Unit
) {
    val students by viewModel.allStudents.collectAsState()
    val batches by viewModel.allBatches.collectAsState()
    val faculty by viewModel.allFaculty.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val feeAccounts by viewModel.allStudentFeeAccounts.collectAsState()
    val chapters by viewModel.allChapters.collectAsState()
    val exams by viewModel.allExams.collectAsState()
    val config by viewModel.systemConfig.collectAsState()

    // Calculated metrics
    val activeStudentsCount = students.count { it.status.name == "ACTIVE" }
    val totalStudentsCount = students.size
    val facultyCount = faculty.size

    // Today's attendance calculation (today = 2026-09-16)
    val todayDate = "2026-09-16"
    val todayMorning = attendance.filter { it.date == todayDate && it.session == AttendanceSession.MORNING }
    val todayEvening = attendance.filter { it.date == todayDate && it.session == AttendanceSession.EVENING }

    val morningPresent = todayMorning.count { it.status == AttendanceStatus.PRESENT }
    val morningPercent = if (todayMorning.isNotEmpty()) (morningPresent * 100.0 / todayMorning.size) else 85.0

    val eveningPresent = todayEvening.count { it.status == AttendanceStatus.PRESENT }
    val eveningPercent = if (todayEvening.isNotEmpty()) (eveningPresent * 100.0 / todayEvening.size) else 65.0

    // Fees calculations
    val totalCollected = payments.sumOf { it.amount }
    val totalPending = feeAccounts.sumOf { it.pendingAmount }

    // Syllabus completion percentage
    val totalChaptersCount = chapters.size
    val completedChaptersCount = chapters.count { it.status.name == "COMPLETED" || it.status.name == "REVISION" }
    val syllabusPercent = if (totalChaptersCount > 0) (completedChaptersCount * 100.0 / totalChaptersCount) else 72.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Welcome Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EzigoNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Institutional ERP Management",
                                style = MaterialTheme.typography.labelMedium,
                                color = EzigoGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = config?.instituteName ?: "Chapter Educational Institute",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "${config?.location ?: "Thazhava"} • Academic Year: ${config?.academicYear ?: "2026-2027"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // Key Metric Stat Cards
        item {
            SectionHeader(title = "Overview Statistics")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Active Students",
                    value = "$activeStudentsCount",
                    subtitle = "$totalStudentsCount Total Registered",
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.STUDENTS) }
                )
                StatCard(
                    title = "Faculty on Duty",
                    value = "$facultyCount",
                    subtitle = "${batches.size} Active Batches",
                    icon = Icons.Default.Badge,
                    iconBgColor = EzigoSuccessBg,
                    iconColor = EzigoSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.FACULTY) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Morning Attendance",
                    value = String.format("%.1f%%", morningPercent),
                    subtitle = "$morningPresent Present Today",
                    icon = Icons.Default.WbSunny,
                    iconBgColor = Color(0xFFFEF3C7),
                    iconColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.ATTENDANCE) }
                )
                StatCard(
                    title = "Evening Attendance",
                    value = String.format("%.1f%%", eveningPercent),
                    subtitle = "$eveningPresent Present Today",
                    icon = Icons.Default.NightsStay,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.ATTENDANCE) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Fee Collection",
                    value = "₹${String.format("%,.0f", totalCollected)}",
                    subtitle = "${payments.size} Receipts Issued",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconBgColor = EzigoSuccessBg,
                    iconColor = EzigoSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.FEES) }
                )
                StatCard(
                    title = "Pending Balance",
                    value = "₹${String.format("%,.0f", totalPending)}",
                    subtitle = "${feeAccounts.count { it.status == FeeAccountStatus.OVERDUE || it.status == FeeAccountStatus.PARTIAL }} Accounts Due",
                    icon = Icons.Default.HourglassTop,
                    iconBgColor = EzigoDangerBg,
                    iconColor = EzigoDanger,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.FEES) }
                )
            }
        }

        // Quick Actions Grid
        item {
            SectionHeader(title = "Quick Operations")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    AssistChip(
                        onClick = { onQuickAction("register_student") },
                        label = { Text("Register Student") },
                        leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onQuickAction("mark_morning") },
                        label = { Text("Morning Attendance") },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = EzigoGold) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onQuickAction("mark_evening") },
                        label = { Text("Evening Attendance") },
                        leadingIcon = { Icon(Icons.Default.Nightlight, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF7C3AED)) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onQuickAction("record_payment") },
                        label = { Text("Record Fee Payment") },
                        leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp), tint = EzigoSuccess) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(Screen.EXAMS) },
                        label = { Text("Enter Exam Marks") },
                        leadingIcon = { Icon(Icons.Default.Grade, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(Screen.REPORTS) },
                        label = { Text("Institutional Reports") },
                        leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        // Financial & Academic Progress Cards
        item {
            SectionHeader(title = "Academic & Financial Performance")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Syllabus Completion Overview",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$completedChaptersCount of $totalChaptersCount chapters completed/revision",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.1f%%", syllabusPercent),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (syllabusPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EzigoSuccess,
                        trackColor = EzigoBorder
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = EzigoBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Fee Recovery Progress",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val totalFeeBilled = totalCollected + totalPending
                    val collectionPercent = if (totalFeeBilled > 0) (totalCollected * 100.0 / totalFeeBilled) else 70.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Collected ₹${String.format("%,.0f", totalCollected)} of ₹${String.format("%,.0f", totalFeeBilled)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.1f%%", collectionPercent),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EzigoSuccess
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (collectionPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = EzigoBorder
                    )
                }
            }
        }

        // Recent Payments
        item {
            SectionHeader(
                title = "Recent Fee Receipts",
                subtitle = "Latest collections across batches",
                action = {
                    TextButton(onClick = { onNavigate(Screen.FEES) }) {
                        Text("View All")
                    }
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                payments.take(4).forEach { payment ->
                    val student = students.find { it.id == payment.studentId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = student?.fullName ?: "Student #${payment.studentId}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${payment.receiptNumber} • ${payment.paymentMethod.displayName} • ${payment.paymentDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${String.format("%,.0f", payment.amount)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EzigoSuccess,
                                    fontSize = 15.sp
                                )
                                StatusBadge(
                                    text = payment.feeCategory.displayName,
                                    type = BadgeType.INFO
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Batches Summary
        item {
            SectionHeader(title = "Enrolled Batches")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                batches.forEach { batch ->
                    val count = students.count { it.batchId == batch.id }
                    Card(
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
                            Column {
                                Text(
                                    text = batch.batchName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${batch.course} • Faculty: ${batch.assignedFaculty}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(
                                text = "$count Students",
                                type = BadgeType.SUCCESS
                            )
                        }
                    }
                }
            }
        }
    }
}
