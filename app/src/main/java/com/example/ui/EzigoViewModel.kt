package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.*
import com.example.data.model.*
import com.example.data.repository.EzigoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen(val title: String) {
    DASHBOARD("Dashboard"),
    STUDENTS("Students"),
    ATTENDANCE("Dual Attendance"),
    FACULTY("Faculty & Hours"),
    FEES("Fee Management"),
    EXAMS("Examinations"),
    SYLLABUS("Syllabus Tracker"),
    REPORTS("Institutional Reports"),
    SETTINGS("System Settings"),
    NOTIFICATIONS("Notifications")
}

data class ToastMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class EzigoViewModel(application: Application) : AndroidViewModel(application) {

    val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = EzigoRepository(database)

    // Active User / Session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _toastMessage = MutableStateFlow<ToastMessage?>(null)
    val toastMessage: StateFlow<ToastMessage?> = _toastMessage.asStateFlow()

    // Data streams
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBatches: StateFlow<List<BatchEntity>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFaculty: StateFlow<List<FacultyEntity>> = repository.allFaculty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceEntity>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFacultyAttendance: StateFlow<List<FacultyAttendanceEntity>> = repository.allFacultyAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPeriodLogs: StateFlow<List<PeriodLogEntity>> = repository.allPeriodLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeeStructures: StateFlow<List<FeeStructureEntity>> = repository.allFeeStructures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudentFeeAccounts: StateFlow<List<StudentFeeAccountEntity>> = repository.allStudentFeeAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdjustments: StateFlow<List<FeeAdjustmentEntity>> = repository.allAdjustments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<ExamEntity>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChapters: StateFlow<List<SyllabusChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemConfig: StateFlow<SystemConfigEntity?> = repository.systemConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Automatically sign in as Admin/Director by default for immediate usability,
        // while allowing easy switching between the 4 roles via header switcher or login page
        viewModelScope.launch {
            allUsers.collect { users ->
                if (_currentUser.value == null && users.isNotEmpty()) {
                    _currentUser.value = users.firstOrNull { it.role == UserRole.ADMIN } ?: users.first()
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun switchRole(role: UserRole) {
        val user = allUsers.value.find { it.role == role }
        if (user != null) {
            _currentUser.value = user
            showToast("Switched active role to: ${role.displayName}")
        }
    }

    fun showToast(message: String, isError: Boolean = false) {
        _toastMessage.value = ToastMessage(message = message, isError = isError)
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun logout() {
        _currentUser.value = null
    }

    fun login(user: UserEntity) {
        _currentUser.value = user
        showToast("Welcome back, ${user.fullName}")
    }

    // Role Permission Checks
    fun canAccess(module: String): Boolean {
        val role = _currentUser.value?.role ?: return false
        return when (role) {
            UserRole.ADMIN -> true
            UserRole.FACULTY -> module in listOf("Dashboard", "Students", "Attendance", "Faculty", "Exams", "Syllabus", "Reports", "Notifications")
            UserRole.ACCOUNTANT -> module in listOf("Dashboard", "Students", "Attendance", "Fees", "Reports", "Notifications")
            UserRole.STAFF -> module in listOf("Dashboard", "Students", "Attendance", "Exams", "Reports", "Notifications")
        }
    }

    fun canManageFees(): Boolean {
        val role = _currentUser.value?.role ?: return false
        return role == UserRole.ADMIN || role == UserRole.ACCOUNTANT
    }

    fun canManageUsersOrConfig(): Boolean {
        return _currentUser.value?.role == UserRole.ADMIN
    }

    fun canEditAttendance(): Boolean {
        val role = _currentUser.value?.role ?: return false
        return role in listOf(UserRole.ADMIN, UserRole.FACULTY, UserRole.STAFF)
    }

    fun canEditMarks(): Boolean {
        val role = _currentUser.value?.role ?: return false
        return role in listOf(UserRole.ADMIN, UserRole.FACULTY)
    }

    // Actions
    fun saveStudent(student: StudentEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Staff"
                repository.saveStudent(student, operator)
                showToast("Student ${student.fullName} saved successfully!")
                onComplete()
            } catch (e: Exception) {
                showToast("Error saving student: ${e.message}", isError = true)
            }
        }
    }

    fun deleteStudent(student: StudentEntity) {
        if (!canManageUsersOrConfig()) {
            showToast("Unauthorized: Only Admin can delete students", isError = true)
            return
        }
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Admin"
                repository.deleteStudent(student, operator)
                showToast("Student ${student.fullName} removed")
            } catch (e: Exception) {
                showToast("Error deleting student: ${e.message}", isError = true)
            }
        }
    }

    fun submitAttendance(
        batchId: Long,
        date: String,
        session: AttendanceSession,
        records: List<Pair<Long, AttendanceStatus>>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Faculty"
                repository.saveBatchAttendance(batchId, date, session, records, operator)
                showToast("Saved ${session.name} attendance for ${records.size} students")
                onComplete()
            } catch (e: Exception) {
                showToast("Error saving attendance: ${e.message}", isError = true)
            }
        }
    }

    fun savePeriodLog(log: PeriodLogEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Faculty"
                repository.savePeriodLog(log, operator)
                showToast("Teaching log recorded for ${log.subject} (${log.periodNumber})")
                onComplete()
            } catch (e: Exception) {
                showToast("Error saving teaching log: ${e.message}", isError = true)
            }
        }
    }

    fun saveFacultyAttendance(att: FacultyAttendanceEntity) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Admin"
                repository.saveFacultyAttendance(att, operator)
                showToast("Faculty attendance recorded")
            } catch (e: Exception) {
                showToast("Error: ${e.message}", isError = true)
            }
        }
    }

    fun saveFaculty(faculty: FacultyEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Admin"
                repository.saveFaculty(faculty, operator)
                showToast("Faculty ${faculty.fullName} saved")
                onComplete()
            } catch (e: Exception) {
                showToast("Error: ${e.message}", isError = true)
            }
        }
    }

    fun saveFeeStructure(feeStructure: FeeStructureEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Accountant"
                repository.saveFeeStructure(feeStructure, operator)
                showToast("Fee structure ${feeStructure.feeCategory.displayName} configured")
                onComplete()
            } catch (e: Exception) {
                showToast("Error: ${e.message}", isError = true)
            }
        }
    }

    fun recordFeePayment(
        studentId: Long,
        feeAccountId: Long,
        amount: Double,
        paymentMethod: PaymentMethod,
        remarks: String,
        onSuccess: (receiptNumber: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Accountant"
                val receipt = repository.recordFeePayment(studentId, feeAccountId, amount, paymentMethod, operator, remarks)
                showToast("Payment recorded successfully! Receipt: $receipt")
                onSuccess(receipt)
            } catch (e: Exception) {
                showToast("Payment failed: ${e.message}", isError = true)
            }
        }
    }

    fun recordFeeAdjustment(
        studentId: Long,
        feeAccountId: Long,
        adjustmentType: String,
        amount: Double,
        reason: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Accountant"
                repository.recordFeeAdjustment(studentId, feeAccountId, adjustmentType, amount, reason, operator)
                showToast("Adjustment of ₹$amount applied with audit log")
                onComplete()
            } catch (e: Exception) {
                showToast("Adjustment failed: ${e.message}", isError = true)
            }
        }
    }

    fun saveExam(exam: ExamEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Admin"
                repository.saveExam(exam, operator)
                showToast("Exam ${exam.examName} scheduled")
                onComplete()
            } catch (e: Exception) {
                showToast("Error: ${e.message}", isError = true)
            }
        }
    }

    fun saveExamMarks(marks: List<ExamMarkEntity>, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Faculty"
                repository.saveExamMarks(marks, operator)
                showToast("Saved examination marks successfully")
                onComplete()
            } catch (e: Exception) {
                showToast("Error: ${e.message}", isError = true)
            }
        }
    }

    fun updateChapter(chapter: SyllabusChapterEntity) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Faculty"
                repository.updateChapter(chapter, operator)
                showToast("Chapter progress updated: ${chapter.chapterName}")
            } catch (e: Exception) {
                showToast("Error: ${e.message}", isError = true)
            }
        }
    }

    fun saveSystemConfig(config: SystemConfigEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val operator = _currentUser.value?.fullName ?: "Admin"
                repository.saveConfig(config, operator)
                showToast("System configuration updated")
                onComplete()
            } catch (e: Exception) {
                showToast("Error: ${e.message}", isError = true)
            }
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            showToast("All notifications marked as read")
        }
    }
}
