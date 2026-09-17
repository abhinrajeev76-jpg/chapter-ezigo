package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.PaymentEntity
import com.example.data.entity.StudentEntity
import com.example.ui.theme.EzigoGold
import com.example.ui.theme.EzigoNavy
import com.example.ui.theme.EzigoNavyDark
import com.example.ui.theme.EzigoSuccess

@Composable
fun ReceiptPrintDialog(
    payment: PaymentEntity,
    student: StudentEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val receiptText = """
==================================================
CHAPTER EDUCATIONAL INSTITUTE
Thazhava, Karunagappally, Kollam
EZIGO ERP - Official Fee Payment Receipt
==================================================
Receipt No: ${payment.receiptNumber}
Date: ${payment.paymentDate}
--------------------------------------------------
Student Name   : ${student?.fullName ?: "N/A"}
Admission ID   : ${student?.studentId ?: "N/A"}
Course / Batch : ${student?.course ?: "Higher Secondary"}
Parent/Guardian: ${student?.parentGuardianName ?: "N/A"}
--------------------------------------------------
Fee Category   : ${payment.feeCategory.displayName}
Payment Mode   : ${payment.paymentMethod.displayName}
Amount Paid    : ₹${String.format("%,.2f", payment.amount)}
Remarks        : ${payment.remarks}
--------------------------------------------------
Collected By   : ${payment.collectedBy}
Status         : Validated & Credited to Institute A/C
==================================================
Authorized Cashier Signature: ____________________
    """.trimIndent()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
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
                        text = "Official Fee Receipt",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = EzigoNavy
                    )
                    Row {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Fee Receipt ${payment.receiptNumber}")
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Fee Receipt"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Printable Receipt Sheet
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Header
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "CHAPTER EDUCATIONAL INSTITUTE",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = EzigoNavyDark,
                                    letterSpacing = 0.5.sp
                                )
                                Text("Thazhava, Kollam • Ph: +91 94470 12345", fontSize = 11.sp, color = Color(0xFF475569))
                                Text("EZIGO ERP - Integrated Management System", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EzigoGold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(thickness = 2.dp, color = EzigoNavy)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Receipt: ${payment.receiptNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EzigoNavy)
                                    Text("Date: ${payment.paymentDate}", fontSize = 12.sp, color = Color(0xFF334155))
                                }
                                Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE2E8F0))
                            }

                            // Student Info
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Received with thanks from:", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(student?.fullName ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Admission ID: ${student?.studentId ?: "N/A"} • ${student?.course ?: "Higher Secondary"}", fontSize = 12.sp)
                                Text("Guardian: ${student?.parentGuardianName ?: "N/A"}", fontSize = 12.sp, color = Color(0xFF475569))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Payment Details Table
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Particulars", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Amount", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${payment.feeCategory.displayName}\n(via ${payment.paymentMethod.displayName})", fontSize = 12.sp)
                                        Text("₹${String.format("%,.2f", payment.amount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EzigoSuccess)
                                    }
                                    if (payment.remarks.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Note: ${payment.remarks}", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }

                        // Bottom Signature
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("Collected By: ${payment.collectedBy}", fontSize = 11.sp, color = Color(0xFF475569))
                                    Text("Computer-generated receipt", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Authorized Signatory", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text("Accounts Section", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EzigoNavy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
