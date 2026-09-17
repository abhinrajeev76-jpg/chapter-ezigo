package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        StudentEntity::class,
        BatchEntity::class,
        FacultyEntity::class,
        AttendanceEntity::class,
        FacultyAttendanceEntity::class,
        PeriodLogEntity::class,
        FeeStructureEntity::class,
        StudentFeeAccountEntity::class,
        PaymentEntity::class,
        FeeAdjustmentEntity::class,
        ExamEntity::class,
        SubjectEntity::class,
        ExamMarkEntity::class,
        SyllabusChapterEntity::class,
        AuditLogEntity::class,
        SystemConfigEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun batchDao(): BatchDao
    abstract fun facultyDao(): FacultyDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun facultyAttendanceDao(): FacultyAttendanceDao
    abstract fun periodLogDao(): PeriodLogDao
    abstract fun feeDao(): FeeDao
    abstract fun examDao(): ExamDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun auditDao(): AuditDao
    abstract fun systemConfigDao(): SystemConfigDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ezigo_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        PrepopulateData.populateDatabase(database)
                    }
                }
            }
        }
    }
}
