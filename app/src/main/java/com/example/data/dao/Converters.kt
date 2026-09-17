package com.example.data.dao

import androidx.room.TypeConverter
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeAccountStatus
import com.example.data.model.FeeCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.PeriodStatus
import com.example.data.model.StudentStatus
import com.example.data.model.SyllabusStatus
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.STAFF
    }

    @TypeConverter
    fun fromAttendanceSession(value: AttendanceSession): String = value.name

    @TypeConverter
    fun toAttendanceSession(value: String): AttendanceSession = try {
        AttendanceSession.valueOf(value)
    } catch (e: Exception) {
        AttendanceSession.MORNING
    }

    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = try {
        AttendanceStatus.valueOf(value)
    } catch (e: Exception) {
        AttendanceStatus.ABSENT
    }

    @TypeConverter
    fun fromStudentStatus(value: StudentStatus): String = value.name

    @TypeConverter
    fun toStudentStatus(value: String): StudentStatus = try {
        StudentStatus.valueOf(value)
    } catch (e: Exception) {
        StudentStatus.ACTIVE
    }

    @TypeConverter
    fun fromPeriodStatus(value: PeriodStatus): String = value.name

    @TypeConverter
    fun toPeriodStatus(value: String): PeriodStatus = try {
        PeriodStatus.valueOf(value)
    } catch (e: Exception) {
        PeriodStatus.COMPLETED
    }

    @TypeConverter
    fun fromFeeCategory(value: FeeCategory): String = value.name

    @TypeConverter
    fun toFeeCategory(value: String): FeeCategory = try {
        FeeCategory.valueOf(value)
    } catch (e: Exception) {
        FeeCategory.TUITION_FEE
    }

    @TypeConverter
    fun fromFeeAccountStatus(value: FeeAccountStatus): String = value.name

    @TypeConverter
    fun toFeeAccountStatus(value: String): FeeAccountStatus = try {
        FeeAccountStatus.valueOf(value)
    } catch (e: Exception) {
        FeeAccountStatus.PENDING
    }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try {
        PaymentMethod.valueOf(value)
    } catch (e: Exception) {
        PaymentMethod.CASH
    }

    @TypeConverter
    fun fromSyllabusStatus(value: SyllabusStatus): String = value.name

    @TypeConverter
    fun toSyllabusStatus(value: String): SyllabusStatus = try {
        SyllabusStatus.valueOf(value)
    } catch (e: Exception) {
        SyllabusStatus.PENDING
    }
}
