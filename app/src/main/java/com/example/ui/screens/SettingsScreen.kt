package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.SystemConfigEntity
import com.example.data.entity.UserEntity
import com.example.ui.EzigoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: EzigoViewModel
) {
    val config by viewModel.systemConfig.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val auditLogs by viewModel.allAuditLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: System Config, 1: User Roles, 2: Audit Logs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "System Configuration & Governance",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = EzigoNavy
        )

        TabRow(
            selectedTabIndex = selectedTab,
            divider = { Divider(color = EzigoBorder) }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Institute Config") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("User Roles (${users.size})") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Audit Trail (${auditLogs.size})") })
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> InstituteConfigView(config, viewModel)
                1 -> UserRolesView(users, viewModel)
                2 -> AuditLogsView(auditLogs)
            }
        }
    }
}

@Composable
fun InstituteConfigView(
    config: SystemConfigEntity?,
    viewModel: EzigoViewModel
) {
    var instituteName by remember(config) { mutableStateOf(config?.instituteName ?: "Chapter Educational Institute") }
    var location by remember(config) { mutableStateOf(config?.location ?: "Thazhava") }
    var contactNumber by remember(config) { mutableStateOf(config?.contactNumber ?: "+91 94470 12345") }
    var academicYear by remember(config) { mutableStateOf(config?.academicYear ?: "2026-2027") }
    var idPrefix by remember(config) { mutableStateOf(config?.studentIdPrefix ?: "EZ") }
    var threshold by remember(config) { mutableStateOf(config?.attendanceThreshold?.toString() ?: "75.0") }
    var signatory by remember(config) { mutableStateOf(config?.reportSignatureText ?: "Director / Principal, Chapter Educational Institute") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Institute Profile Parameters", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = instituteName,
                        onValueChange = { instituteName = it },
                        label = { Text("Institute Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location / Campus") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Official Contact Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = academicYear,
                            onValueChange = { academicYear = it },
                            label = { Text("Active Academic Year") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = idPrefix,
                            onValueChange = { idPrefix = it },
                            label = { Text("Student ID Prefix") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = threshold,
                        onValueChange = { threshold = it },
                        label = { Text("Attendance Warning Threshold (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = signatory,
                        onValueChange = { signatory = it },
                        label = { Text("Official Report Signatory Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (viewModel.canManageUsersOrConfig()) {
                        Button(
                            onClick = {
                                val updated = (config ?: SystemConfigEntity(
                                    instituteName = instituteName,
                                    location = location,
                                    contactNumber = contactNumber,
                                    academicYear = academicYear,
                                    studentIdPrefix = idPrefix,
                                    attendanceThreshold = threshold.toDoubleOrNull() ?: 75.0,
                                    reportSignatureText = signatory
                                )).copy(
                                    instituteName = instituteName,
                                    location = location,
                                    contactNumber = contactNumber,
                                    academicYear = academicYear,
                                    studentIdPrefix = idPrefix,
                                    attendanceThreshold = threshold.toDoubleOrNull() ?: 75.0,
                                    reportSignatureText = signatory
                                )
                                viewModel.saveSystemConfig(updated) {}
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                        ) {
                            Text("Save Configuration")
                        }
                    } else {
                        Text("Only Administrator can modify system parameters.", color = EzigoTextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun UserRolesView(
    users: List<UserEntity>,
    viewModel: EzigoViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(users) { u ->
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EzigoNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = EzigoGold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(u.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(u.email, style = MaterialTheme.typography.bodySmall, color = EzigoTextMuted)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(text = u.role.displayName, type = BadgeType.INFO)
                        TextButton(
                            onClick = { viewModel.switchRole(u.role) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Switch to this role", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogsView(
    auditLogs: List<AuditLogEntity>
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EzigoNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Immutable Security Audit Trail: Records every fee payment, waiver, attendance batch, and exam entry.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        items(auditLogs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(text = log.action, type = BadgeType.INFO)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(log.module, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(sdf.format(Date(log.timestamp)), fontSize = 11.sp, color = EzigoTextMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("User: ${log.userId} • Record: ${log.recordId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (log.newValue.isNotBlank()) {
                        Text("Value: ${log.newValue}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (log.reason.isNotBlank()) {
                        Text("Reason: ${log.reason}", fontSize = 11.sp, color = EzigoTextMuted)
                    }
                }
            }
        }
    }
}
