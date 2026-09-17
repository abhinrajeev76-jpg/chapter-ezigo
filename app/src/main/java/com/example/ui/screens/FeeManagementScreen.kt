package com.example.ui.screens

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
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.*
import com.example.data.model.FeeAccountStatus
import com.example.data.model.FeeCategory
import com.example.data.model.PaymentMethod
import com.example.ui.EzigoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeManagementScreen(
    viewModel: EzigoViewModel,
    onViewReceipt: (PaymentEntity, StudentEntity?) -> Unit = { _, _ -> }
) {
    val students by viewModel.allStudents.collectAsState()
    val feeAccounts by viewModel.allStudentFeeAccounts.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val feeStructures by viewModel.allFeeStructures.collectAsState()
    val adjustments by viewModel.allAdjustments.collectAsState()
    val batches by viewModel.allBatches.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Student Balances, 1: Receipts Ledger, 2: Fee Structures, 3: Concession Audit
    var showPaymentDialogForAccount by remember { mutableStateOf<Pair<StudentEntity, StudentFeeAccountEntity>?>(null) }
    var showAdjustmentDialogForAccount by remember { mutableStateOf<Pair<StudentEntity, StudentFeeAccountEntity>?>(null) }
    var showAddStructureDialog by remember { mutableStateOf(false) }

    var studentSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<FeeCategory?>(null) }

    // Summary calculations
    val totalCollected = remember(payments) { payments.sumOf { it.amount } }
    val totalPending = remember(feeAccounts) { feeAccounts.sumOf { it.pendingAmount } }
    val totalBilled = remember(feeAccounts) { feeAccounts.sumOf { it.totalAmount } }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 2 && viewModel.canManageFees()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddStructureDialog = true },
                    containerColor = EzigoNavy,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Configure Fee") }
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
                text = "Fee & Financial Management",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = EzigoNavy
            )

            // Financial Summary Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Collected",
                    value = "₹${String.format("%,.0f", totalCollected)}",
                    subtitle = "${payments.size} Receipts",
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = EzigoSuccessBg,
                    iconColor = EzigoSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Pending",
                    value = "₹${String.format("%,.0f", totalPending)}",
                    subtitle = "Billed ₹${String.format("%,.0f", totalBilled)}",
                    icon = Icons.Default.PendingActions,
                    iconBgColor = EzigoDangerBg,
                    iconColor = EzigoDanger,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                divider = { Divider(color = EzigoBorder) }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Student Accounts") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Receipts Ledger (${payments.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Fee Categories") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Audit / Waivers") })
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> StudentFeeAccountsView(
                        students = students,
                        feeAccounts = feeAccounts,
                        searchQuery = studentSearchQuery,
                        onSearchChange = { studentSearchQuery = it },
                        canManage = viewModel.canManageFees(),
                        onCollectPayment = { s, acc -> showPaymentDialogForAccount = Pair(s, acc) },
                        onApplyAdjustment = { s, acc -> showAdjustmentDialogForAccount = Pair(s, acc) }
                    )
                    1 -> ReceiptsLedgerView(
                        payments = payments,
                        students = students,
                        onViewReceipt = onViewReceipt
                    )
                    2 -> FeeStructuresView(
                        structures = feeStructures,
                        batches = batches
                    )
                    3 -> FeeAdjustmentsAuditView(
                        adjustments = adjustments,
                        students = students
                    )
                }
            }
        }
    }

    // Payment Collection Dialog
    showPaymentDialogForAccount?.let { (student, account) ->
        PaymentCollectionDialog(
            student = student,
            account = account,
            onDismiss = { showPaymentDialogForAccount = null },
            onConfirmPayment = { amount, method, remarks ->
                viewModel.recordFeePayment(
                    studentId = student.id,
                    feeAccountId = account.id,
                    amount = amount,
                    paymentMethod = method,
                    remarks = remarks,
                    onSuccess = { receiptNo ->
                        showPaymentDialogForAccount = null
                    }
                )
            }
        )
    }

    // Strict Fee Adjustment / Concession Dialog with Mandatory Reason
    showAdjustmentDialogForAccount?.let { (student, account) ->
        FeeAdjustmentDialog(
            student = student,
            account = account,
            onDismiss = { showAdjustmentDialogForAccount = null },
            onConfirmAdjustment = { type, amount, reason ->
                viewModel.recordFeeAdjustment(
                    studentId = student.id,
                    feeAccountId = account.id,
                    adjustmentType = type,
                    amount = amount,
                    reason = reason,
                    onComplete = {
                        showAdjustmentDialogForAccount = null
                    }
                )
            }
        )
    }

    // Add Fee Structure Dialog
    if (showAddStructureDialog) {
        AddFeeStructureDialog(
            batches = batches,
            onDismiss = { showAddStructureDialog = false },
            onSave = { structure ->
                viewModel.saveFeeStructure(structure) {
                    showAddStructureDialog = false
                }
            }
        )
    }
}

@Composable
fun StudentFeeAccountsView(
    students: List<StudentEntity>,
    feeAccounts: List<StudentFeeAccountEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    canManage: Boolean,
    onCollectPayment: (StudentEntity, StudentFeeAccountEntity) -> Unit,
    onApplyAdjustment: (StudentEntity, StudentFeeAccountEntity) -> Unit
) {
    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students
        else students.filter { it.fullName.contains(searchQuery, ignoreCase = true) || it.studentId.contains(searchQuery, ignoreCase = true) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Search by student name or admission ID") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredStudents) { student ->
                val accounts = feeAccounts.filter { it.studentId == student.id }
                val studentTotalBilled = accounts.sumOf { it.totalAmount }
                val studentTotalPaid = accounts.sumOf { it.paidAmount }
                val studentTotalPending = accounts.sumOf { it.pendingAmount }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(text = student.studentId, type = BadgeType.NEUTRAL)
                                }
                                Text("Parent: ${student.parentGuardianName} • ${student.parentPhone}", style = MaterialTheme.typography.labelSmall, color = EzigoTextMuted)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (studentTotalPending <= 0) "Cleared" else "Pending: ₹${String.format("%,.0f", studentTotalPending)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (studentTotalPending <= 0) EzigoSuccess else EzigoDanger,
                                    fontSize = 14.sp
                                )
                                Text("Paid: ₹${String.format("%,.0f", studentTotalPaid)} / ₹${String.format("%,.0f", studentTotalBilled)}", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = EzigoBorder)

                        // Itemized Accounts
                        accounts.forEach { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(acc.feeCategory.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Fixed: ₹${String.format("%,.0f", acc.totalAmount)} | Paid: ₹${String.format("%,.0f", acc.paidAmount)} | Due: ₹${String.format("%,.0f", acc.pendingAmount)}", style = MaterialTheme.typography.labelSmall, color = EzigoTextMuted)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(text = acc.status.name, type = if (acc.status == FeeAccountStatus.CLEARED) BadgeType.SUCCESS else if (acc.status == FeeAccountStatus.PARTIAL) BadgeType.WARNING else BadgeType.DANGER)

                                    if (canManage && acc.pendingAmount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        FilledTonalButton(
                                            onClick = { onCollectPayment(student, acc) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Pay", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { onApplyAdjustment(student, acc) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = "Audit Concession", modifier = Modifier.size(16.dp), tint = EzigoGold)
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

@Composable
fun ReceiptsLedgerView(
    payments: List<PaymentEntity>,
    students: List<StudentEntity>,
    onViewReceipt: (PaymentEntity, StudentEntity?) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(payments) { p ->
            val student = students.find { it.id == p.studentId }
            Card(
                onClick = { onViewReceipt(p, student) },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(p.receiptNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = EzigoNavy)
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(text = p.paymentMethod.displayName, type = BadgeType.INFO)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("Student: ${student?.fullName ?: "ID #${p.studentId}"} (${student?.studentId ?: ""})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Category: ${p.feeCategory.displayName} • Date: ${p.paymentDate} • By: ${p.collectedBy}", style = MaterialTheme.typography.labelSmall, color = EzigoTextMuted)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${String.format("%,.0f", p.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EzigoSuccess)
                        TextButton(onClick = { onViewReceipt(p, student) }, contentPadding = PaddingValues(0.dp)) {
                            Text("Print Receipt", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeeStructuresView(
    structures: List<FeeStructureEntity>,
    batches: List<BatchEntity>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text("Configured Institutional Fee Schedules", fontWeight = FontWeight.Bold)
        }
        items(structures) { s ->
            val batch = batches.find { it.id == s.batchId }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(s.feeCategory.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Batch: ${batch?.batchName ?: s.course} • Due: ${s.dueDate}", style = MaterialTheme.typography.bodySmall)
                        Text(s.description, style = MaterialTheme.typography.labelSmall, color = EzigoTextMuted)
                    }
                    Text("₹${String.format("%,.0f", s.fixedAmount)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EzigoNavy)
                }
            }
        }
    }
}

@Composable
fun FeeAdjustmentsAuditView(
    adjustments: List<FeeAdjustmentEntity>,
    students: List<StudentEntity>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EzigoWarningBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EzigoWarning)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Strict Audit Trail: All fee waivers and concessions require recorded managerial justification.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF78350F))
                }
            }
        }

        items(adjustments) { adj ->
            val student = students.find { it.id == adj.studentId }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EzigoBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Concession: ${adj.adjustmentType}", fontWeight = FontWeight.Bold, color = EzigoGold)
                        Text("₹${String.format("%,.0f", adj.amount)}", fontWeight = FontWeight.Bold, color = EzigoDanger)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Student: ${student?.fullName ?: "ID #${adj.studentId}"} • Modified By: ${adj.modifiedBy}", style = MaterialTheme.typography.bodySmall)
                    Text("Original Total: ₹${String.format("%,.0f", adj.originalAmount)}", style = MaterialTheme.typography.labelSmall, color = EzigoTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                        Text("Mandatory Justification: ${adj.reason}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentCollectionDialog(
    student: StudentEntity,
    account: StudentFeeAccountEntity,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, method: PaymentMethod, remarks: String) -> Unit
) {
    var amountText by remember { mutableStateOf(account.pendingAmount.toInt().toString()) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var remarks by remember { mutableStateOf("Fee installment clearance") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Collect Fee Payment", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = EzigoNavy)
                Text("Student: ${student.fullName} (${student.studentId})", fontWeight = FontWeight.SemiBold)
                Text("Category: ${account.feeCategory.displayName} • Due: ₹${String.format("%,.0f", account.pendingAmount)}", color = EzigoTextMuted, style = MaterialTheme.typography.bodySmall)
                Divider()

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = EzigoDanger, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹) *") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PaymentMethod.values()) { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method.displayName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Receipt Remarks / Transaction Reference") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMsg = "Please enter a valid positive amount"
                                return@Button
                            }
                            if (amt > account.pendingAmount) {
                                errorMsg = "Amount exceeds outstanding balance (₹${account.pendingAmount})"
                                return@Button
                            }
                            onConfirmPayment(amt, selectedMethod, remarks)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                    ) {
                        Text("Issue Receipt")
                    }
                }
            }
        }
    }
}

@Composable
fun FeeAdjustmentDialog(
    student: StudentEntity,
    account: StudentFeeAccountEntity,
    onDismiss: () -> Unit,
    onConfirmAdjustment: (type: String, amount: Double, reason: String) -> Unit
) {
    var amountText by remember { mutableStateOf("1000") }
    var adjustmentType by remember { mutableStateOf("Merit Scholarship Concession") }
    var reason by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Managerial Fee Concession / Waiver", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = EzigoNavy)
                Text("Student: ${student.fullName} • ${account.feeCategory.displayName}", fontWeight = FontWeight.SemiBold)
                Divider()

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = EzigoDanger, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = adjustmentType,
                    onValueChange = { adjustmentType = it },
                    label = { Text("Concession Type (e.g. Merit Scholarship, Sibling Discount)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Concession Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Mandatory Justification / Management Approval Note *") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Strictly required for audit integrity") }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMsg = "Enter a valid positive waiver amount"
                                return@Button
                            }
                            if (reason.isBlank()) {
                                errorMsg = "Justification note is mandatory for security audit"
                                return@Button
                            }
                            onConfirmAdjustment(adjustmentType, amt, reason)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoGold)
                    ) {
                        Text("Apply & Log Concession")
                    }
                }
            }
        }
    }
}

@Composable
fun AddFeeStructureDialog(
    batches: List<BatchEntity>,
    onDismiss: () -> Unit,
    onSave: (FeeStructureEntity) -> Unit
) {
    var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 1L) }
    var selectedCategory by remember { mutableStateOf(FeeCategory.TUITION_FEE) }
    var amountText by remember { mutableStateOf("20000") }
    var dueDate by remember { mutableStateOf("2026-10-15") }
    var description by remember { mutableStateOf("Annual Batch Tuition Fee") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Configure Batch Fee Structure", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Divider()

                Text("Batch", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(batches) { b ->
                        FilterChip(
                            selected = selectedBatchId == b.id,
                            onClick = { selectedBatchId = b.id },
                            label = { Text(b.batchName) }
                        )
                    }
                }

                Text("Fee Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FeeCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName) }
                        )
                    }
                }

                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Fixed Amount (₹)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val batch = batches.find { it.id == selectedBatchId }
                            onSave(
                                FeeStructureEntity(
                                    course = batch?.course ?: "Higher Secondary",
                                    batchId = selectedBatchId,
                                    academicYear = "2026-2027",
                                    feeCategory = selectedCategory,
                                    fixedAmount = amt,
                                    dueDate = dueDate,
                                    description = description
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EzigoNavy)
                    ) {
                        Text("Save Structure")
                    }
                }
            }
        }
    }
}
