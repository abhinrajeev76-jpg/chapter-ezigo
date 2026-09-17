package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeAccountStatus
import com.example.data.model.FeeCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.PeriodStatus
import com.example.data.model.StudentStatus
import com.example.data.model.SyllabusStatus
import com.example.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String, // e.g. EZ-2026-001
    val fullName: String,
    val photograph: String = "",
    val dateOfBirth: String,
    val gender: String,
    val parentGuardianName: String,
    val parentPhone: String,
    val whatsappNumber: String,
    val residentialAddress: String,
    val schoolCollegeName: String,
    val course: String,
    val batchId: Long,
    val admissionDate: String,
    val academicYear: String,
    val assignedSubjects: String, // Comma separated
    val status: StudentStatus = StudentStatus.ACTIVE,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchName: String, // e.g. Plus One Science
    val course: String,
    val academicYear: String,
    val sessionType: String = "Regular & Evening",
    val startDate: String,
    val endDate: String,
    val assignedFaculty: String = "",
    val status: String = "Active"
)

@Entity(tableName = "faculty")
data class FacultyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facultyId: String, // e.g. FAC-001
    val fullName: String,
    val phone: String,
    val email: String,
    val assignedSubjects: String,
    val assignedBatches: String,
    val joiningDate: String,
    val qualification: String,
    val status: String = "Active"
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val batchId: Long,
    val date: String, // YYYY-MM-DD
    val session: AttendanceSession, // MORNING or EVENING
    val status: AttendanceStatus, // PRESENT, ABSENT, LEAVE
    val markedBy: String,
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "faculty_attendance")
data class FacultyAttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facultyId: Long,
    val date: String,
    val checkIn: String,
    val checkOut: String,
    val status: String = "Present",
    val remarks: String = ""
)

@Entity(tableName = "period_logs")
data class PeriodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val facultyId: Long,
    val batchId: Long,
    val subject: String,
    val periodNumber: String, // e.g. 1st Hour
    val startTime: String,
    val endTime: String,
    val topicCovered: String,
    val status: PeriodStatus = PeriodStatus.COMPLETED,
    val remarks: String = ""
)

@Entity(tableName = "fee_structures")
data class FeeStructureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val course: String,
    val batchId: Long,
    val academicYear: String,
    val feeCategory: FeeCategory,
    val fixedAmount: Double,
    val dueDate: String,
    val description: String = "",
    val active: Boolean = true
)

@Entity(tableName = "student_fee_accounts")
data class StudentFeeAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val feeStructureId: Long,
    val feeCategory: FeeCategory,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double,
    val dueDate: String,
    val status: FeeAccountStatus = FeeAccountStatus.PENDING
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val feeAccountId: Long,
    val receiptNumber: String, // e.g. REC-2026-0001
    val paymentDate: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val feeCategory: FeeCategory,
    val collectedBy: String,
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fee_adjustments")
data class FeeAdjustmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val feeAccountId: Long,
    val adjustmentType: String, // Discount, Concession, Waiver, Correction
    val amount: Double,
    val reason: String,
    val originalAmount: Double,
    val modifiedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examName: String,
    val examType: String, // Unit Test, Mid Term, Terminal, Model Exam
    val batchId: Long,
    val academicYear: String,
    val examDate: String,
    val totalMarks: Double,
    val status: String = "Active"
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val course: String,
    val batchId: Long,
    val maximumMarks: Double = 100.0,
    val assignedFaculty: String = ""
)

@Entity(tableName = "exam_marks")
data class ExamMarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val studentId: Long,
    val subjectId: Long,
    val marksObtained: Double,
    val maximumMarks: Double = 100.0,
    val remarks: String = "",
    val enteredBy: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "syllabus_chapters")
data class SyllabusChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val batchId: Long,
    val chapterName: String,
    val chapterNumber: Int,
    val totalTopics: Int,
    val completedTopics: Int,
    val status: SyllabusStatus = SyllabusStatus.PENDING,
    val completionDate: String = "",
    val remarks: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val action: String, // CREATE, UPDATE, DELETE, ADJUSTMENT, ATTENDANCE, MARKS, PAYMENT
    val module: String, // Students, Fees, Attendance, Exams, Syllabus, Configuration
    val recordId: String,
    val oldValue: String = "",
    val newValue: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_config")
data class SystemConfigEntity(
    @PrimaryKey val id: Int = 1,
    val instituteName: String = "Chapter Educational Institute",
    val location: String = "Thazhava",
    val systemName: String = "EZIGO",
    val tagline: String = "Integrated Institute Management System",
    val contactNumber: String = "+91 94470 12345",
    val academicYear: String = "2026-2027",
    val studentIdPrefix: String = "EZ",
    val attendanceThreshold: Double = 75.0,
    val currencySymbol: String = "₹",
    val reportSignatureText: String = "Director / Authorized Signatory",
    val morningStartTime: String = "09:00 AM",
    val eveningStartTime: String = "04:30 PM"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String = "INFO",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
