package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.*
import com.example.data.model.AttendanceSession
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY studentId ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE batchId = :batchId ORDER BY studentId ASC")
    fun getStudentsByBatch(batchId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE batchId = :batchId ORDER BY studentId ASC")
    suspend fun getStudentsByBatchDirect(batchId: Long): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Long): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentByIdDirect(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentByStudentId(studentId: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'")
    fun getActiveStudentCount(): Flow<Int>
}

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY batchName ASC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: Long): BatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Delete
    suspend fun deleteBatch(batch: BatchEntity)
}

@Dao
interface FacultyDao {
    @Query("SELECT * FROM faculty ORDER BY fullName ASC")
    fun getAllFaculty(): Flow<List<FacultyEntity>>

    @Query("SELECT * FROM faculty WHERE id = :id LIMIT 1")
    suspend fun getFacultyById(id: Long): FacultyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculty(faculty: FacultyEntity): Long

    @Update
    suspend fun updateFaculty(faculty: FacultyEntity)

    @Delete
    suspend fun deleteFaculty(faculty: FacultyEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE batchId = :batchId AND date = :date AND session = :session")
    fun getAttendanceForBatchAndDate(batchId: Long, date: String, session: AttendanceSession): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE batchId = :batchId AND date = :date AND session = :session")
    suspend fun getAttendanceForBatchAndDateDirect(batchId: Long, date: String, session: AttendanceSession): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date AND session = :session")
    fun getAttendanceForDateAndSession(date: String, session: AttendanceSession): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)
}

@Dao
interface FacultyAttendanceDao {
    @Query("SELECT * FROM faculty_attendance WHERE date = :date")
    fun getFacultyAttendanceForDate(date: String): Flow<List<FacultyAttendanceEntity>>

    @Query("SELECT * FROM faculty_attendance ORDER BY date DESC")
    fun getAllFacultyAttendance(): Flow<List<FacultyAttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacultyAttendance(att: FacultyAttendanceEntity): Long

    @Update
    suspend fun updateFacultyAttendance(att: FacultyAttendanceEntity)
}

@Dao
interface PeriodLogDao {
    @Query("SELECT * FROM period_logs ORDER BY date DESC, id DESC")
    fun getAllPeriodLogs(): Flow<List<PeriodLogEntity>>

    @Query("SELECT * FROM period_logs WHERE facultyId = :facultyId ORDER BY date DESC")
    fun getPeriodLogsByFaculty(facultyId: Long): Flow<List<PeriodLogEntity>>

    @Query("SELECT * FROM period_logs WHERE batchId = :batchId ORDER BY date DESC")
    fun getPeriodLogsByBatch(batchId: Long): Flow<List<PeriodLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriodLog(log: PeriodLogEntity): Long

    @Update
    suspend fun updatePeriodLog(log: PeriodLogEntity)

    @Delete
    suspend fun deletePeriodLog(log: PeriodLogEntity)
}

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_structures ORDER BY id ASC")
    fun getAllFeeStructures(): Flow<List<FeeStructureEntity>>

    @Query("SELECT * FROM fee_structures WHERE batchId = :batchId")
    fun getFeeStructuresForBatch(batchId: Long): Flow<List<FeeStructureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeStructure(feeStructure: FeeStructureEntity): Long

    @Update
    suspend fun updateFeeStructure(feeStructure: FeeStructureEntity)

    @Query("SELECT * FROM student_fee_accounts WHERE studentId = :studentId")
    fun getStudentFeeAccounts(studentId: Long): Flow<List<StudentFeeAccountEntity>>

    @Query("SELECT * FROM student_fee_accounts WHERE studentId = :studentId")
    suspend fun getStudentFeeAccountsDirect(studentId: Long): List<StudentFeeAccountEntity>

    @Query("SELECT * FROM student_fee_accounts ORDER BY id ASC")
    fun getAllStudentFeeAccounts(): Flow<List<StudentFeeAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentFeeAccount(account: StudentFeeAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentFeeAccounts(accounts: List<StudentFeeAccountEntity>)

    @Update
    suspend fun updateStudentFeeAccount(account: StudentFeeAccountEntity)

    @Query("SELECT * FROM student_fee_accounts WHERE id = :id LIMIT 1")
    suspend fun getStudentFeeAccountById(id: Long): StudentFeeAccountEntity?

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Query("SELECT * FROM fee_adjustments ORDER BY timestamp DESC")
    fun getAllAdjustments(): Flow<List<FeeAdjustmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: FeeAdjustmentEntity): Long
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY examDate DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE batchId = :batchId ORDER BY examDate DESC")
    fun getExamsByBatch(batchId: Long): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    @Query("SELECT * FROM subjects ORDER BY subjectName ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE batchId = :batchId ORDER BY subjectName ASC")
    fun getSubjectsByBatch(batchId: Long): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Query("SELECT * FROM exam_marks WHERE examId = :examId")
    fun getMarksForExam(examId: Long): Flow<List<ExamMarkEntity>>

    @Query("SELECT * FROM exam_marks WHERE examId = :examId")
    suspend fun getMarksForExamDirect(examId: Long): List<ExamMarkEntity>

    @Query("SELECT * FROM exam_marks WHERE studentId = :studentId")
    fun getMarksForStudent(studentId: Long): Flow<List<ExamMarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamMark(mark: ExamMarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamMarksList(marks: List<ExamMarkEntity>)

    @Update
    suspend fun updateExamMark(mark: ExamMarkEntity)
}

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabus_chapters ORDER BY subjectId ASC, chapterNumber ASC")
    fun getAllChapters(): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE subjectId = :subjectId ORDER BY chapterNumber ASC")
    fun getChaptersBySubject(subjectId: Long): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT * FROM syllabus_chapters WHERE batchId = :batchId ORDER BY chapterNumber ASC")
    fun getChaptersByBatch(batchId: Long): Flow<List<SyllabusChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: SyllabusChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChaptersList(chapters: List<SyllabusChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: SyllabusChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: SyllabusChapterEntity)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long
}

@Dao
interface SystemConfigDao {
    @Query("SELECT * FROM system_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<SystemConfigEntity?>

    @Query("SELECT * FROM system_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): SystemConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: SystemConfigEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}
