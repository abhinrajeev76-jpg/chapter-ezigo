package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.PaymentEntity
import com.example.data.entity.StudentEntity
import com.example.data.model.AttendanceSession
import com.example.ui.EzigoViewModel
import com.example.ui.Screen
import com.example.ui.components.EzigoBottomNavBar
import com.example.ui.components.EzigoTopBar
import com.example.ui.components.ReceiptPrintDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EzigoApp()
            }
        }
    }
}

@Composable
fun EzigoApp(
    viewModel: EzigoViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val unreadNotifCount by viewModel.unreadNotificationsCount.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Active dialog state for receipts
    var viewingReceiptPayment by remember { mutableStateOf<Pair<PaymentEntity, StudentEntity?>?>(null) }

    // Fast quick actions originating from dashboard
    var attendanceDefaultSession by remember { mutableStateOf<AttendanceSession?>(null) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EzigoTopBar(
                currentUser = currentUser,
                unreadNotifCount = unreadNotifCount,
                onNavigateToNotifications = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
                onSwitchRole = { role -> viewModel.switchRole(role) },
                onLogout = { viewModel.logout() }
            )
        },
        bottomBar = {
            EzigoBottomNavBar(
                currentScreen = currentScreen,
                canAccess = { viewModel.navigateTo(Screen.valueOf(it)) },
                canAccessModule = { module -> viewModel.canAccess(module) },
                onScreenSelected = { screen -> viewModel.navigateTo(screen) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onQuickAction = { action ->
                            when (action) {
                                "register_student" -> viewModel.navigateTo(Screen.STUDENTS)
                                "mark_morning" -> {
                                    attendanceDefaultSession = AttendanceSession.MORNING
                                    viewModel.navigateTo(Screen.ATTENDANCE)
                                }
                                "mark_evening" -> {
                                    attendanceDefaultSession = AttendanceSession.EVENING
                                    viewModel.navigateTo(Screen.ATTENDANCE)
                                }
                                "record_payment" -> viewModel.navigateTo(Screen.FEES)
                            }
                        }
                    )
                }
                Screen.STUDENTS -> {
                    StudentsScreen(
                        viewModel = viewModel,
                        onGenerateReport = { student, reportType ->
                            viewModel.navigateTo(Screen.REPORTS)
                        }
                    )
                }
                Screen.ATTENDANCE -> {
                    DualAttendanceScreen(
                        viewModel = viewModel,
                        initialSession = attendanceDefaultSession
                    )
                }
                Screen.FACULTY -> {
                    FacultyScreen(viewModel = viewModel)
                }
                Screen.FEES -> {
                    FeeManagementScreen(
                        viewModel = viewModel,
                        onViewReceipt = { payment, student ->
                            viewingReceiptPayment = Pair(payment, student)
                        }
                    )
                }
                Screen.EXAMS -> {
                    ExaminationScreen(
                        viewModel = viewModel,
                        onGenerateProgressCard = { student, exam ->
                            viewModel.navigateTo(Screen.REPORTS)
                        }
                    )
                }
                Screen.SYLLABUS -> {
                    SyllabusScreen(viewModel = viewModel)
                }
                Screen.REPORTS -> {
                    ReportsScreen(viewModel = viewModel)
                }
                Screen.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
                Screen.NOTIFICATIONS -> {
                    NotificationsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Receipt Print Dialog
    viewingReceiptPayment?.let { (payment, student) ->
        ReceiptPrintDialog(
            payment = payment,
            student = student,
            onDismiss = { viewingReceiptPayment = null }
        )
    }
}
