package com.example.data.model

enum class UserRole(val displayName: String) {
    ADMIN("Admin / Director"),
    FACULTY("Faculty"),
    ACCOUNTANT("Accountant"),
    STAFF("Staff / Clerk")
}

enum class AttendanceSession(val displayName: String) {
    MORNING("Morning"),
    EVENING("Evening")
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LEAVE
}

enum class StudentStatus {
    ACTIVE,
    INACTIVE,
    COMPLETED,
    TRANSFERRED
}

enum class PeriodStatus {
    COMPLETED,
    IN_PROGRESS,
    RESCHEDULED,
    CANCELLED
}

enum class FeeCategory(val displayName: String) {
    ADMISSION_FEE("Admission Fee"),
    TUITION_FEE("Tuition Fee"),
    NIGHT_CLASS_SPECIAL_FEE("Night Class Special Fee"),
    STUDY_MATERIAL_FEE("Study Material Fee"),
    EXAM_FEE("Exam Fee"),
    OTHER_SPECIAL_FEE("Other Special Fee")
}

enum class FeeAccountStatus {
    PENDING,
    PARTIAL,
    CLEARED,
    OVERDUE
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other")
}

enum class SyllabusStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    REVISION("Revision")
}

enum class NotificationType {
    INFO,
    WARNING,
    ALERT,
    SUCCESS
}
