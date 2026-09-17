package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.entity.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EzigoRepository(private val database: AppDatabase) {

    // DAOs
    private val userDao = database.userDao()
    private val studentDao = database.studentDao()
    private val batchDao = database.batchDao()
    private val facultyDao = database.facultyDao()
    private val attendanceDao = database.attendanceDao()
    private val facultyAttendanceDao = database.facultyAttendanceDao()
    private val periodLogDao = database.periodLogDao()
    private val feeDao = database.feeDao()
    private val examDao = database.examDao()
    private val syllabusDao = database.syllabusDao()
    private val auditDao = database.auditDao()
    private val configDao = database.systemConfigDao()
    private val notificationDao = database.notificationDao()

    // Flows
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val allBatches: Flow<List<BatchEntity>> = batchDao.getAllBatches()
    val allFaculty: Flow<List<FacultyEntity>> = facultyDao.getAllFaculty()
    val allAttendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()
    val allFacultyAttendance: Flow<List<FacultyAttendanceEntity>> = facultyAttendanceDao.getAllFacultyAttendance()
    val allPeriodLogs: Flow<List<PeriodLogEntity>> = periodLogDao.getAllPeriodLogs()
    val allFeeStructures: Flow<List<FeeStructureEntity>> = feeDao.getAllFeeStructures()
    val allStudentFeeAccounts: Flow<List<StudentFeeAccountEntity>> = feeDao.getAllStudentFeeAccounts()
    val allPayments: Flow<List<PaymentEntity>> = feeDao.getAllPayments()
    val allAdjustments: Flow<List<FeeAdjustmentEntity>> = feeDao.getAllAdjustments()
    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()
    val allSubjects: Flow<List<SubjectEntity>> = examDao.getAllSubjects()
    val allChapters: Flow<List<SyllabusChapterEntity>> = syllabusDao.getAllChapters()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditDao.getAllAuditLogs()
    val systemConfig: Flow<SystemConfigEntity?> = configDao.getConfig()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()

    // Authentication
    suspend fun authenticate(email: String, passwordHash: String): UserEntity? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email)
        if (user != null && user.passwordHash == passwordHash) {
            userDao.updateUser(user.copy(lastLogin = System.currentTimeMillis()))
            user
        } else null
    }

    suspend fun getUserById(id: Long): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    // Student CRUD
    fun getStudentsByBatch(batchId: Long): Flow<List<StudentEntity>> = studentDao.getStudentsByBatch(batchId)
    fun getStudentById(id: Long): Flow<StudentEntity?> = studentDao.getStudentById(id)

    suspend fun generateNextStudentId(): String = withContext(Dispatchers.IO) {
        val config = configDao.getConfigDirect()
        val prefix = config?.studentIdPrefix ?: "EZ"
        val year = "2026"
        val count = (studentDao.getStudentCount().firstOrNull() ?: 0) + 1
        String.format(Locale.getDefault(), "%s-%s-%03d", prefix, year, count)
    }

    suspend fun saveStudent(student: StudentEntity, operatorName: String): Long = withContext(Dispatchers.IO) {
        if (student.id == 0L) {
            val newId = studentDao.insertStudent(student)
            // Auto create fee accounts for the student based on batch fee structures
            val batchStructures = feeDao.getFeeStructuresForBatch(student.batchId).firstOrNull() ?: emptyList()
            batchStructures.forEach { structure ->
                feeDao.insertStudentFeeAccount(
                    StudentFeeAccountEntity(
                        studentId = newId,
                        feeStructureId = structure.id,
                        feeCategory = structure.feeCategory,
                        totalAmount = structure.fixedAmount,
                        paidAmount = 0.0,
                        pendingAmount = structure.fixedAmount,
                        dueDate = structure.dueDate,
                        status = FeeAccountStatus.PENDING
                    )
                )
            }
            logAudit(operatorName, "CREATE", "Students", student.studentId, "", student.fullName, "Student admission registered")
            newId
        } else {
            studentDao.updateStudent(student.copy(updatedAt = System.currentTimeMillis()))
            logAudit(operatorName, "UPDATE", "Students", student.studentId, "", student.fullName, "Student details modified")
            student.id
        }
    }

    suspend fun deleteStudent(student: StudentEntity, operatorName: String) = withContext(Dispatchers.IO) {
        studentDao.deleteStudent(student)
        logAudit(operatorName, "DELETE", "Students", student.studentId, student.fullName, "", "Student record deleted")
    }

    // Attendance Operations
    fun getAttendanceForBatchAndDate(batchId: Long, date: String, session: AttendanceSession): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForBatchAndDate(batchId, date, session)

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStudent(studentId)

    suspend fun saveAttendanceRecord(
        studentId: Long,
        batchId: Long,
        date: String,
        session: AttendanceSession,
        status: AttendanceStatus,
        markedBy: String,
        remarks: String
    ) = withContext(Dispatchers.IO) {
        val existing = attendanceDao.getAttendanceForBatchAndDateDirect(batchId, date, session)
            .find { it.studentId == studentId }

        if (existing != null) {
            attendanceDao.updateAttendance(
                existing.copy(
                    status = status,
                    markedBy = markedBy,
                    remarks = remarks,
                    updatedAt = System.currentTimeMillis()
                )
            )
            logAudit(markedBy, "UPDATE", "Attendance", "ATT-$studentId-$date-$session", existing.status.name, status.name, "Attendance updated")
        } else {
            val newAtt = AttendanceEntity(
                studentId = studentId,
                batchId = batchId,
                date = date,
                session = session,
                status = status,
                markedBy = markedBy,
                remarks = remarks
            )
            attendanceDao.insertAttendance(newAtt)
            logAudit(markedBy, "ATTENDANCE", "Attendance", "ATT-$studentId-$date-$session", "", status.name, "Attendance marked")
        }
    }

    suspend fun saveBatchAttendance(
        batchId: Long,
        date: String,
        session: AttendanceSession,
        records: List<Pair<Long, AttendanceStatus>>,
        markedBy: String
    ) = withContext(Dispatchers.IO) {
        val existing = attendanceDao.getAttendanceForBatchAndDateDirect(batchId, date, session).associateBy { it.studentId }
        val toInsert = mutableListOf<AttendanceEntity>()
        for ((studentId, status) in records) {
            val ex = existing[studentId]
            if (ex != null) {
                if (ex.status != status) {
                    attendanceDao.updateAttendance(
                        ex.copy(status = status, markedBy = markedBy, updatedAt = System.currentTimeMillis())
                    )
                }
            } else {
                toInsert.add(
                    AttendanceEntity(
                        studentId = studentId,
                        batchId = batchId,
                        date = date,
                        session = session,
                        status = status,
                        markedBy = markedBy
                    )
                )
            }
        }
        if (toInsert.isNotEmpty()) {
            attendanceDao.insertAttendanceList(toInsert)
        }
        logAudit(markedBy, "ATTENDANCE", "Attendance", "Batch-$batchId-$date-$session", "", "${records.size} students", "Batch session attendance submitted")
    }

    // Faculty & Workload Operations
    suspend fun saveFaculty(faculty: FacultyEntity, operator: String) = withContext(Dispatchers.IO) {
        if (faculty.id == 0L) {
            facultyDao.insertFaculty(faculty)
            logAudit(operator, "CREATE", "Faculty", faculty.facultyId, "", faculty.fullName, "Faculty registered")
        } else {
            facultyDao.updateFaculty(faculty)
            logAudit(operator, "UPDATE", "Faculty", faculty.facultyId, "", faculty.fullName, "Faculty details modified")
        }
    }

    suspend fun savePeriodLog(log: PeriodLogEntity, operator: String) = withContext(Dispatchers.IO) {
        if (log.id == 0L) {
            periodLogDao.insertPeriodLog(log)
            logAudit(operator, "CREATE", "PeriodLogs", log.periodNumber, "", log.topicCovered, "Teaching log recorded")
        } else {
            periodLogDao.updatePeriodLog(log)
            logAudit(operator, "UPDATE", "PeriodLogs", log.periodNumber, "", log.topicCovered, "Teaching log updated")
        }
    }

    suspend fun saveFacultyAttendance(att: FacultyAttendanceEntity, operator: String) = withContext(Dispatchers.IO) {
        if (att.id == 0L) {
            facultyAttendanceDao.insertFacultyAttendance(att)
        } else {
            facultyAttendanceDao.updateFacultyAttendance(att)
        }
        logAudit(operator, "FACULTY_ATTENDANCE", "Faculty", "FAC-${att.facultyId}-${att.date}", "", att.status, "Faculty attendance logged")
    }

    // Fees Operations
    fun getFeeAccountsForStudent(studentId: Long): Flow<List<StudentFeeAccountEntity>> = feeDao.getStudentFeeAccounts(studentId)
    fun getPaymentsForStudent(studentId: Long): Flow<List<PaymentEntity>> = feeDao.getPaymentsForStudent(studentId)

    suspend fun saveFeeStructure(feeStructure: FeeStructureEntity, operator: String) = withContext(Dispatchers.IO) {
        if (feeStructure.id == 0L) {
            val newId = feeDao.insertFeeStructure(feeStructure)
            // Automatically attach to all active students in this batch
            val students = studentDao.getStudentsByBatchDirect(feeStructure.batchId)
            students.forEach { s ->
                feeDao.insertStudentFeeAccount(
                    StudentFeeAccountEntity(
                        studentId = s.id,
                        feeStructureId = newId,
                        feeCategory = feeStructure.feeCategory,
                        totalAmount = feeStructure.fixedAmount,
                        paidAmount = 0.0,
                        pendingAmount = feeStructure.fixedAmount,
                        dueDate = feeStructure.dueDate,
                        status = FeeAccountStatus.PENDING
                    )
                )
            }
            logAudit(operator, "CREATE", "FeeStructures", feeStructure.feeCategory.name, "", "${feeStructure.fixedAmount}", "New fee structure configured")
        } else {
            feeDao.updateFeeStructure(feeStructure)
            logAudit(operator, "UPDATE", "FeeStructures", feeStructure.feeCategory.name, "", "${feeStructure.fixedAmount}", "Fee structure updated")
        }
    }

    suspend fun generateNextReceiptNumber(): String = withContext(Dispatchers.IO) {
        val payments = feeDao.getAllPayments().firstOrNull() ?: emptyList()
        val nextNum = payments.size + 1
        String.format(Locale.getDefault(), "REC-2026-%04d", nextNum)
    }

    suspend fun recordFeePayment(
        studentId: Long,
        feeAccountId: Long,
        amount: Double,
        paymentMethod: PaymentMethod,
        collectedBy: String,
        remarks: String
    ): String = withContext(Dispatchers.IO) {
        require(amount > 0) { "Payment amount must be greater than zero" }
        val account = feeDao.getStudentFeeAccountById(feeAccountId) ?: throw IllegalArgumentException("Fee account not found")

        val receiptNumber = generateNextReceiptNumber()
        val paymentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Save Payment
        val payment = PaymentEntity(
            studentId = studentId,
            feeAccountId = feeAccountId,
            receiptNumber = receiptNumber,
            paymentDate = paymentDate,
            amount = amount,
            paymentMethod = paymentMethod,
            feeCategory = account.feeCategory,
            collectedBy = collectedBy,
            remarks = remarks
        )
        feeDao.insertPayment(payment)

        // Update Account
        val newPaid = account.paidAmount + amount
        val newPending = (account.totalAmount - newPaid).coerceAtLeast(0.0)
        val newStatus = when {
            newPending <= 0.0 -> FeeAccountStatus.CLEARED
            newPaid > 0.0 -> FeeAccountStatus.PARTIAL
            else -> FeeAccountStatus.PENDING
        }

        feeDao.updateStudentFeeAccount(
            account.copy(
                paidAmount = newPaid,
                pendingAmount = newPending,
                status = newStatus
            )
        )

        logAudit(collectedBy, "PAYMENT", "Fees", receiptNumber, "Pending: ${account.pendingAmount}", "Paid: $amount, Remaining: $newPending", remarks)
        receiptNumber
    }

    suspend fun recordFeeAdjustment(
        studentId: Long,
        feeAccountId: Long,
        adjustmentType: String,
        amount: Double,
        reason: String,
        modifiedBy: String
    ) = withContext(Dispatchers.IO) {
        require(reason.isNotBlank()) { "Adjustment reason is mandatory for audit security" }
        val account = feeDao.getStudentFeeAccountById(feeAccountId) ?: throw IllegalArgumentException("Fee account not found")

        val origAmount = account.totalAmount
        val newTotal = (account.totalAmount - amount).coerceAtLeast(account.paidAmount)
        val newPending = (newTotal - account.paidAmount).coerceAtLeast(0.0)
        val newStatus = when {
            newPending <= 0.0 -> FeeAccountStatus.CLEARED
            account.paidAmount > 0.0 -> FeeAccountStatus.PARTIAL
            else -> FeeAccountStatus.PENDING
        }

        feeDao.insertAdjustment(
            FeeAdjustmentEntity(
                studentId = studentId,
                feeAccountId = feeAccountId,
                adjustmentType = adjustmentType,
                amount = amount,
                reason = reason,
                originalAmount = origAmount,
                modifiedBy = modifiedBy
            )
        )

        feeDao.updateStudentFeeAccount(
            account.copy(
                totalAmount = newTotal,
                pendingAmount = newPending,
                status = newStatus
            )
        )

        logAudit(modifiedBy, "ADJUSTMENT", "Fees", "ACC-$feeAccountId", "$origAmount", "$newTotal", "Adjustment: $adjustmentType, Reason: $reason")
    }

    // Examination & Marks Operations
    fun getMarksForExam(examId: Long): Flow<List<ExamMarkEntity>> = examDao.getMarksForExam(examId)
    fun getMarksForStudent(studentId: Long): Flow<List<ExamMarkEntity>> = examDao.getMarksForStudent(studentId)

    suspend fun saveExam(exam: ExamEntity, operator: String) = withContext(Dispatchers.IO) {
        if (exam.id == 0L) {
            examDao.insertExam(exam)
            logAudit(operator, "CREATE", "Exams", exam.examName, "", "${exam.totalMarks}", "Exam scheduled")
        } else {
            examDao.updateExam(exam)
            logAudit(operator, "UPDATE", "Exams", exam.examName, "", "${exam.totalMarks}", "Exam modified")
        }
    }

    suspend fun saveExamMarks(marks: List<ExamMarkEntity>, operator: String) = withContext(Dispatchers.IO) {
        examDao.insertExamMarksList(marks)
        logAudit(operator, "MARKS", "Exams", "Exam-${marks.firstOrNull()?.examId ?: 0}", "", "${marks.size} entries", "Exam marks entered/updated")
    }

    // Syllabus Operations
    fun getChaptersBySubject(subjectId: Long): Flow<List<SyllabusChapterEntity>> = syllabusDao.getChaptersBySubject(subjectId)

    suspend fun updateChapter(chapter: SyllabusChapterEntity, operator: String) = withContext(Dispatchers.IO) {
        syllabusDao.updateChapter(chapter)
        logAudit(operator, "UPDATE", "Syllabus", "CH-${chapter.id}", "", "${chapter.status.name} (${chapter.completedTopics}/${chapter.totalTopics})", "Chapter progress updated")
    }

    // System Config
    suspend fun saveConfig(config: SystemConfigEntity, operator: String) = withContext(Dispatchers.IO) {
        configDao.insertOrUpdateConfig(config)
        logAudit(operator, "CONFIG", "SystemConfig", "SYS-001", "", config.instituteName, "System configuration updated")
    }

    // Notifications
    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    // Audit Logger
    suspend fun logAudit(
        userId: String,
        action: String,
        module: String,
        recordId: String,
        oldValue: String,
        newValue: String,
        reason: String
    ) = withContext(Dispatchers.IO) {
        auditDao.insertAuditLog(
            AuditLogEntity(
                userId = userId,
                action = action,
                module = module,
                recordId = recordId,
                oldValue = oldValue,
                newValue = newValue,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
