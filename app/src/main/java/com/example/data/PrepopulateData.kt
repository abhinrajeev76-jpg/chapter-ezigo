package com.example.data

import com.example.data.entity.*
import com.example.data.model.*

object PrepopulateData {
    suspend fun populateDatabase(db: AppDatabase) {
        // 1. System Config
        val configDao = db.systemConfigDao()
        configDao.insertOrUpdateConfig(
            SystemConfigEntity(
                id = 1,
                instituteName = "Chapter Educational Institute",
                location = "Thazhava",
                systemName = "EZIGO",
                tagline = "Integrated Institute Management System",
                contactNumber = "+91 94470 12345",
                academicYear = "2026-2027",
                studentIdPrefix = "EZ",
                attendanceThreshold = 75.0,
                currencySymbol = "₹",
                reportSignatureText = "Director / Principal, Chapter Educational Institute"
            )
        )

        // 2. Users (all roles)
        val userDao = db.userDao()
        userDao.insertUser(
            UserEntity(
                id = 1,
                fullName = "Dr. K. R. Nair (Director)",
                email = "admin@ezigo.edu",
                passwordHash = "admin123",
                role = UserRole.ADMIN,
                status = "Active"
            )
        )
        userDao.insertUser(
            UserEntity(
                id = 2,
                fullName = "Anila Miss (Senior Faculty)",
                email = "faculty@ezigo.edu",
                passwordHash = "faculty123",
                role = UserRole.FACULTY,
                status = "Active"
            )
        )
        userDao.insertUser(
            UserEntity(
                id = 3,
                fullName = "M. S. Pillai (Chief Accountant)",
                email = "accountant@ezigo.edu",
                passwordHash = "account123",
                role = UserRole.ACCOUNTANT,
                status = "Active"
            )
        )
        userDao.insertUser(
            UserEntity(
                id = 4,
                fullName = "Sunitha R. (Senior Clerk)",
                email = "staff@ezigo.edu",
                passwordHash = "staff123",
                role = UserRole.STAFF,
                status = "Active"
            )
        )

        // 3. Batches
        val batchDao = db.batchDao()
        val batch1Id = batchDao.insertBatch(
            BatchEntity(
                id = 1,
                batchName = "Plus One Science",
                course = "Higher Secondary Science",
                academicYear = "2026-2027",
                sessionType = "Regular & Evening",
                startDate = "2026-06-01",
                endDate = "2027-03-31",
                assignedFaculty = "Anila Miss, Rahul Sir, Suresh Sir",
                status = "Active"
            )
        )
        val batch2Id = batchDao.insertBatch(
            BatchEntity(
                id = 2,
                batchName = "Plus Two Science",
                course = "Higher Secondary Science",
                academicYear = "2026-2027",
                sessionType = "Regular & Evening",
                startDate = "2026-06-01",
                endDate = "2027-03-31",
                assignedFaculty = "Anila Miss, Rahul Sir",
                status = "Active"
            )
        )
        val batch3Id = batchDao.insertBatch(
            BatchEntity(
                id = 3,
                batchName = "Plus One Commerce",
                course = "Higher Secondary Commerce",
                academicYear = "2026-2027",
                sessionType = "Regular",
                startDate = "2026-06-01",
                endDate = "2027-03-31",
                assignedFaculty = "Suresh Sir",
                status = "Active"
            )
        )
        val batch4Id = batchDao.insertBatch(
            BatchEntity(
                id = 4,
                batchName = "Plus Two Commerce",
                course = "Higher Secondary Commerce",
                academicYear = "2026-2027",
                sessionType = "Regular",
                startDate = "2026-06-01",
                endDate = "2027-03-31",
                assignedFaculty = "Suresh Sir",
                status = "Active"
            )
        )

        // 4. Faculty
        val facultyDao = db.facultyDao()
        val fac1Id = facultyDao.insertFaculty(
            FacultyEntity(
                id = 1,
                facultyId = "FAC-001",
                fullName = "Anila Miss",
                phone = "+91 98471 23456",
                email = "anila@chapterthazhava.edu",
                assignedSubjects = "Mathematics",
                assignedBatches = "Plus One Science, Plus Two Science",
                joiningDate = "2022-05-15",
                qualification = "M.Sc Mathematics, B.Ed"
            )
        )
        val fac2Id = facultyDao.insertFaculty(
            FacultyEntity(
                id = 2,
                facultyId = "FAC-002",
                fullName = "Rahul Sir",
                phone = "+91 98472 34567",
                email = "rahul@chapterthazhava.edu",
                assignedSubjects = "Physics",
                assignedBatches = "Plus One Science, Plus Two Science",
                joiningDate = "2023-01-10",
                qualification = "M.Sc Physics, M.Phil"
            )
        )
        val fac3Id = facultyDao.insertFaculty(
            FacultyEntity(
                id = 3,
                facultyId = "FAC-003",
                fullName = "Suresh Sir",
                phone = "+91 98473 45678",
                email = "suresh@chapterthazhava.edu",
                assignedSubjects = "Chemistry, Accountancy",
                assignedBatches = "Plus One Science, Plus One Commerce",
                joiningDate = "2021-08-01",
                qualification = "M.Sc Chemistry, B.Ed"
            )
        )

        // 5. Students (Abhin S., Ashtami R., Rahul M., Kiran Kumar)
        val studentDao = db.studentDao()
        val s1Id = studentDao.insertStudent(
            StudentEntity(
                id = 1,
                studentId = "EZ-2026-001",
                fullName = "Abhin S.",
                photograph = "",
                dateOfBirth = "2009-04-12",
                gender = "Male",
                parentGuardianName = "Suresh Kumar",
                parentPhone = "+91 94471 11223",
                whatsappNumber = "+91 94471 11223",
                residentialAddress = "Anugraha, Thazhava North, Karunagappally",
                schoolCollegeName = "BHS Thazhava",
                course = "Higher Secondary Science",
                batchId = batch1Id,
                admissionDate = "2026-06-05",
                academicYear = "2026-2027",
                assignedSubjects = "Mathematics, Physics, Chemistry, Biology",
                status = StudentStatus.ACTIVE,
                notes = "High academic aptitude, active in science club."
            )
        )

        val s2Id = studentDao.insertStudent(
            StudentEntity(
                id = 2,
                studentId = "EZ-2026-002",
                fullName = "Ashtami R.",
                photograph = "",
                dateOfBirth = "2009-08-25",
                gender = "Female",
                parentGuardianName = "Radhakrishnan P.",
                parentPhone = "+91 94472 22334",
                whatsappNumber = "+91 94472 22334",
                residentialAddress = "Revathy Bhavan, Kadathur, Thazhava",
                schoolCollegeName = "Govt HSS Vallikunnam",
                course = "Higher Secondary Science",
                batchId = batch1Id,
                admissionDate = "2026-06-06",
                academicYear = "2026-2027",
                assignedSubjects = "Mathematics, Physics, Chemistry, Biology",
                status = StudentStatus.ACTIVE,
                notes = "Consistently 90%+ in terminal exams."
            )
        )

        val s3Id = studentDao.insertStudent(
            StudentEntity(
                id = 3,
                studentId = "EZ-2026-003",
                fullName = "Rahul M.",
                photograph = "",
                dateOfBirth = "2009-11-03",
                gender = "Male",
                parentGuardianName = "Muraleedharan Nair",
                parentPhone = "+91 94473 33445",
                whatsappNumber = "+91 94473 33445",
                residentialAddress = "Sree Padmam, Manappally, Thazhava",
                schoolCollegeName = "GMMHSS Karunagappally",
                course = "Higher Secondary Science",
                batchId = batch1Id,
                admissionDate = "2026-06-10",
                academicYear = "2026-2027",
                assignedSubjects = "Mathematics, Physics, Chemistry, Biology",
                status = StudentStatus.ACTIVE,
                notes = "Needs attendance monitoring, parent notified."
            )
        )

        val s4Id = studentDao.insertStudent(
            StudentEntity(
                id = 4,
                studentId = "EZ-2026-004",
                fullName = "Kiran Kumar",
                photograph = "",
                dateOfBirth = "2009-02-18",
                gender = "Male",
                parentGuardianName = "Vijayan K.",
                parentPhone = "+91 94474 44556",
                whatsappNumber = "+91 94474 44556",
                residentialAddress = "Kiran Nivas, Pavumba, Thazhava",
                schoolCollegeName = "KSRTC Jn Model School",
                course = "Higher Secondary Science",
                batchId = batch1Id,
                admissionDate = "2026-06-12",
                academicYear = "2026-2027",
                assignedSubjects = "Mathematics, Physics, Chemistry, Biology",
                status = StudentStatus.ACTIVE,
                notes = "Special remedial coaching enrolled for Physics."
            )
        )

        // 6. Subjects
        val examDao = db.examDao()
        val subMath = examDao.insertSubject(SubjectEntity(id = 1, subjectName = "Mathematics", course = "Higher Secondary Science", batchId = batch1Id, maximumMarks = 100.0, assignedFaculty = "Anila Miss"))
        val subPhys = examDao.insertSubject(SubjectEntity(id = 2, subjectName = "Physics", course = "Higher Secondary Science", batchId = batch1Id, maximumMarks = 100.0, assignedFaculty = "Rahul Sir"))
        val subChem = examDao.insertSubject(SubjectEntity(id = 3, subjectName = "Chemistry", course = "Higher Secondary Science", batchId = batch1Id, maximumMarks = 100.0, assignedFaculty = "Suresh Sir"))
        val subBio  = examDao.insertSubject(SubjectEntity(id = 4, subjectName = "Biology", course = "Higher Secondary Science", batchId = batch1Id, maximumMarks = 100.0, assignedFaculty = "Dr. Bindu"))
        val subAcc  = examDao.insertSubject(SubjectEntity(id = 5, subjectName = "Accountancy", course = "Higher Secondary Commerce", batchId = batch3Id, maximumMarks = 100.0, assignedFaculty = "Suresh Sir"))

        // 7. Exams & Marks (From Prompt Section 10 Mark Matrix)
        val examId = examDao.insertExam(
            ExamEntity(
                id = 1,
                examName = "Terminal Examination 2026",
                examType = "Terminal",
                batchId = batch1Id,
                academicYear = "2026-2027",
                examDate = "2026-09-10",
                totalMarks = 300.0,
                status = "Completed"
            )
        )

        examDao.insertExamMarksList(
            listOf(
                // Abhin S. : Maths 85, Physics 78, Chemistry 90 = 253 (84.33%) A+ Passed
                ExamMarkEntity(examId = examId, studentId = s1Id, subjectId = subMath, marksObtained = 85.0, maximumMarks = 100.0, remarks = "Very Good", enteredBy = "Anila Miss"),
                ExamMarkEntity(examId = examId, studentId = s1Id, subjectId = subPhys, marksObtained = 78.0, maximumMarks = 100.0, remarks = "Good", enteredBy = "Rahul Sir"),
                ExamMarkEntity(examId = examId, studentId = s1Id, subjectId = subChem, marksObtained = 90.0, maximumMarks = 100.0, remarks = "Excellent", enteredBy = "Suresh Sir"),

                // Ashtami R. : Maths 90, Physics 88, Chemistry 92 = 270 (90.00%) A+ Passed
                ExamMarkEntity(examId = examId, studentId = s2Id, subjectId = subMath, marksObtained = 90.0, maximumMarks = 100.0, remarks = "Outstanding", enteredBy = "Anila Miss"),
                ExamMarkEntity(examId = examId, studentId = s2Id, subjectId = subPhys, marksObtained = 88.0, maximumMarks = 100.0, remarks = "Outstanding", enteredBy = "Rahul Sir"),
                ExamMarkEntity(examId = examId, studentId = s2Id, subjectId = subChem, marksObtained = 92.0, maximumMarks = 100.0, remarks = "Batch Topper", enteredBy = "Suresh Sir"),

                // Kiran Kumar : Maths 42, Physics 38, Chemistry 45 = 125 (41.67%) C Needs Improvement
                ExamMarkEntity(examId = examId, studentId = s4Id, subjectId = subMath, marksObtained = 42.0, maximumMarks = 100.0, remarks = "Needs Focus", enteredBy = "Anila Miss"),
                ExamMarkEntity(examId = examId, studentId = s4Id, subjectId = subPhys, marksObtained = 38.0, maximumMarks = 100.0, remarks = "Remedial Needed", enteredBy = "Rahul Sir"),
                ExamMarkEntity(examId = examId, studentId = s4Id, subjectId = subChem, marksObtained = 45.0, maximumMarks = 100.0, remarks = "Practice Problems", enteredBy = "Suresh Sir"),

                // Rahul M.
                ExamMarkEntity(examId = examId, studentId = s3Id, subjectId = subMath, marksObtained = 64.0, maximumMarks = 100.0, remarks = "Average", enteredBy = "Anila Miss"),
                ExamMarkEntity(examId = examId, studentId = s3Id, subjectId = subPhys, marksObtained = 59.0, maximumMarks = 100.0, remarks = "Average", enteredBy = "Rahul Sir"),
                ExamMarkEntity(examId = examId, studentId = s3Id, subjectId = subChem, marksObtained = 68.0, maximumMarks = 100.0, remarks = "Good", enteredBy = "Suresh Sir")
            )
        )

        // 8. Attendance (Independent Morning and Evening sessions as specified)
        val attDao = db.attendanceDao()
        val today = "2026-09-16"
        val yesterday = "2026-09-15"

        // Today's attendance demonstrating independent morning & evening:
        // Abhin S. : Morning = Present, Evening = Absent -> Partial 50%
        attDao.insertAttendance(AttendanceEntity(studentId = s1Id, batchId = batch1Id, date = today, session = AttendanceSession.MORNING, status = AttendanceStatus.PRESENT, markedBy = "Anila Miss"))
        attDao.insertAttendance(AttendanceEntity(studentId = s1Id, batchId = batch1Id, date = today, session = AttendanceSession.EVENING, status = AttendanceStatus.ABSENT, markedBy = "Anila Miss", remarks = "Informed night class absence"))

        // Ashtami R. : Morning = Present, Evening = Present -> Full 100%
        attDao.insertAttendance(AttendanceEntity(studentId = s2Id, batchId = batch1Id, date = today, session = AttendanceSession.MORNING, status = AttendanceStatus.PRESENT, markedBy = "Anila Miss"))
        attDao.insertAttendance(AttendanceEntity(studentId = s2Id, batchId = batch1Id, date = today, session = AttendanceSession.EVENING, status = AttendanceStatus.PRESENT, markedBy = "Anila Miss"))

        // Rahul M. : Morning = Absent, Evening = Absent -> Absent 0%
        attDao.insertAttendance(AttendanceEntity(studentId = s3Id, batchId = batch1Id, date = today, session = AttendanceSession.MORNING, status = AttendanceStatus.ABSENT, markedBy = "Anila Miss", remarks = "Fever"))
        attDao.insertAttendance(AttendanceEntity(studentId = s3Id, batchId = batch1Id, date = today, session = AttendanceSession.EVENING, status = AttendanceStatus.ABSENT, markedBy = "Anila Miss"))

        // Kiran Kumar : Morning = Present, Evening = Present
        attDao.insertAttendance(AttendanceEntity(studentId = s4Id, batchId = batch1Id, date = today, session = AttendanceSession.MORNING, status = AttendanceStatus.PRESENT, markedBy = "Anila Miss"))
        attDao.insertAttendance(AttendanceEntity(studentId = s4Id, batchId = batch1Id, date = today, session = AttendanceSession.EVENING, status = AttendanceStatus.PRESENT, markedBy = "Anila Miss"))

        // Yesterday's attendance
        attDao.insertAttendance(AttendanceEntity(studentId = s1Id, batchId = batch1Id, date = yesterday, session = AttendanceSession.MORNING, status = AttendanceStatus.PRESENT, markedBy = "Sunitha R."))
        attDao.insertAttendance(AttendanceEntity(studentId = s1Id, batchId = batch1Id, date = yesterday, session = AttendanceSession.EVENING, status = AttendanceStatus.PRESENT, markedBy = "Sunitha R."))
        attDao.insertAttendance(AttendanceEntity(studentId = s2Id, batchId = batch1Id, date = yesterday, session = AttendanceSession.MORNING, status = AttendanceStatus.PRESENT, markedBy = "Sunitha R."))
        attDao.insertAttendance(AttendanceEntity(studentId = s2Id, batchId = batch1Id, date = yesterday, session = AttendanceSession.EVENING, status = AttendanceStatus.PRESENT, markedBy = "Sunitha R."))
        attDao.insertAttendance(AttendanceEntity(studentId = s3Id, batchId = batch1Id, date = yesterday, session = AttendanceSession.MORNING, status = AttendanceStatus.PRESENT, markedBy = "Sunitha R."))
        attDao.insertAttendance(AttendanceEntity(studentId = s3Id, batchId = batch1Id, date = yesterday, session = AttendanceSession.EVENING, status = AttendanceStatus.ABSENT, markedBy = "Sunitha R."))
        attDao.insertAttendance(AttendanceEntity(studentId = s4Id, batchId = batch1Id, date = yesterday, session = AttendanceSession.MORNING, status = AttendanceStatus.PRESENT, markedBy = "Sunitha R."))
        attDao.insertAttendance(AttendanceEntity(studentId = s4Id, batchId = batch1Id, date = yesterday, session = AttendanceSession.EVENING, status = AttendanceStatus.PRESENT, markedBy = "Sunitha R."))

        // 9. Faculty Attendance & Period Logs (Prompt Section 8 Example)
        val facAttDao = db.facultyAttendanceDao()
        facAttDao.insertFacultyAttendance(FacultyAttendanceEntity(facultyId = fac1Id, date = today, checkIn = "08:45 AM", checkOut = "06:15 PM", status = "Present", remarks = "On duty"))
        facAttDao.insertFacultyAttendance(FacultyAttendanceEntity(facultyId = fac2Id, date = today, checkIn = "09:00 AM", checkOut = "05:30 PM", status = "Present", remarks = "Lab sessions"))
        facAttDao.insertFacultyAttendance(FacultyAttendanceEntity(facultyId = fac3Id, date = today, checkIn = "09:15 AM", checkOut = "05:00 PM", status = "Present", remarks = "Commerce batch"))

        val periodDao = db.periodLogDao()
        periodDao.insertPeriodLog(
            PeriodLogEntity(
                date = today,
                facultyId = fac1Id,
                batchId = batch1Id,
                subject = "Mathematics",
                periodNumber = "1st Hour",
                startTime = "09:15 AM",
                endTime = "10:15 AM",
                topicCovered = "Integration by Parts",
                status = PeriodStatus.COMPLETED,
                remarks = "Exercises 7.6 completed, homework assigned"
            )
        )
        periodDao.insertPeriodLog(
            PeriodLogEntity(
                date = today,
                facultyId = fac2Id,
                batchId = batch1Id,
                subject = "Physics",
                periodNumber = "2nd Hour",
                startTime = "10:30 AM",
                endTime = "11:30 AM",
                topicCovered = "Electromagnetic Induction - Faraday's Laws",
                status = PeriodStatus.COMPLETED,
                remarks = "Demonstration with coil and magnet"
            )
        )
        periodDao.insertPeriodLog(
            PeriodLogEntity(
                date = today,
                facultyId = fac1Id,
                batchId = batch1Id,
                subject = "Mathematics",
                periodNumber = "Evening 1st Hour",
                startTime = "05:00 PM",
                endTime = "06:15 PM",
                topicCovered = "Definite Integrals - Special Properties",
                status = PeriodStatus.COMPLETED,
                remarks = "Night class session"
            )
        )

        // 10. Fee Structures & Student Fee Accounts (Exact table in Section 9)
        val feeDao = db.feeDao()
        val fee1Id = feeDao.insertFeeStructure(FeeStructureEntity(course = "Higher Secondary Science", batchId = batch1Id, academicYear = "2026-2027", feeCategory = FeeCategory.ADMISSION_FEE, fixedAmount = 5000.0, dueDate = "2026-06-15", description = "One-time Admission Fee"))
        val fee2Id = feeDao.insertFeeStructure(FeeStructureEntity(course = "Higher Secondary Science", batchId = batch1Id, academicYear = "2026-2027", feeCategory = FeeCategory.TUITION_FEE, fixedAmount = 20000.0, dueDate = "2026-10-15", description = "Annual Academic Tuition Fee"))
        val fee3Id = feeDao.insertFeeStructure(FeeStructureEntity(course = "Higher Secondary Science", batchId = batch1Id, academicYear = "2026-2027", feeCategory = FeeCategory.NIGHT_CLASS_SPECIAL_FEE, fixedAmount = 3000.0, dueDate = "2026-08-30", description = "Evening & Night Class Intensive Fee"))
        val fee4Id = feeDao.insertFeeStructure(FeeStructureEntity(course = "Higher Secondary Science", batchId = batch1Id, academicYear = "2026-2027", feeCategory = FeeCategory.STUDY_MATERIAL_FEE, fixedAmount = 500.0, dueDate = "2026-09-01", description = "Study Material & Exam Fee"))

        // For Abhin S. :
        // Admission Fee: 5,000 paid, 0 pending, Cleared
        // Tuition Fee: 12,000 paid, 8,000 pending, Partial
        // Night Class Special Fee: 3,000 paid, 0 pending, Cleared
        // Study Material & Exam Fee: 0 paid, 500 pending, Pending
        // Total: Fixed 28,500, Paid 20,000, Pending 8,500
        val acc1Id = feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s1Id, feeStructureId = fee1Id, feeCategory = FeeCategory.ADMISSION_FEE, totalAmount = 5000.0, paidAmount = 5000.0, pendingAmount = 0.0, dueDate = "2026-06-15", status = FeeAccountStatus.CLEARED))
        val acc2Id = feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s1Id, feeStructureId = fee2Id, feeCategory = FeeCategory.TUITION_FEE, totalAmount = 20000.0, paidAmount = 12000.0, pendingAmount = 8000.0, dueDate = "2026-10-15", status = FeeAccountStatus.PARTIAL))
        val acc3Id = feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s1Id, feeStructureId = fee3Id, feeCategory = FeeCategory.NIGHT_CLASS_SPECIAL_FEE, totalAmount = 3000.0, paidAmount = 3000.0, pendingAmount = 0.0, dueDate = "2026-08-30", status = FeeAccountStatus.CLEARED))
        val acc4Id = feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s1Id, feeStructureId = fee4Id, feeCategory = FeeCategory.STUDY_MATERIAL_FEE, totalAmount = 500.0, paidAmount = 0.0, pendingAmount = 500.0, dueDate = "2026-09-01", status = FeeAccountStatus.PENDING))

        // Payments for Abhin S.
        feeDao.insertPayment(PaymentEntity(studentId = s1Id, feeAccountId = acc1Id, receiptNumber = "REC-2026-0001", paymentDate = "2026-06-05", amount = 5000.0, paymentMethod = PaymentMethod.UPI, feeCategory = FeeCategory.ADMISSION_FEE, collectedBy = "M. S. Pillai", remarks = "Full Admission Fee settled via GooglePay"))
        feeDao.insertPayment(PaymentEntity(studentId = s1Id, feeAccountId = acc2Id, receiptNumber = "REC-2026-0002", paymentDate = "2026-07-10", amount = 12000.0, paymentMethod = PaymentMethod.BANK_TRANSFER, feeCategory = FeeCategory.TUITION_FEE, collectedBy = "M. S. Pillai", remarks = "Tuition 1st Installment NEFT"))
        feeDao.insertPayment(PaymentEntity(studentId = s1Id, feeAccountId = acc3Id, receiptNumber = "REC-2026-0003", paymentDate = "2026-08-15", amount = 3000.0, paymentMethod = PaymentMethod.CASH, feeCategory = FeeCategory.NIGHT_CLASS_SPECIAL_FEE, collectedBy = "Sunitha R.", remarks = "Night class term fee cash"))

        // Fee accounts for other students
        feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s2Id, feeStructureId = fee1Id, feeCategory = FeeCategory.ADMISSION_FEE, totalAmount = 5000.0, paidAmount = 5000.0, pendingAmount = 0.0, dueDate = "2026-06-15", status = FeeAccountStatus.CLEARED))
        feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s2Id, feeStructureId = fee2Id, feeCategory = FeeCategory.TUITION_FEE, totalAmount = 20000.0, paidAmount = 20000.0, pendingAmount = 0.0, dueDate = "2026-10-15", status = FeeAccountStatus.CLEARED))
        feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s3Id, feeStructureId = fee1Id, feeCategory = FeeCategory.ADMISSION_FEE, totalAmount = 5000.0, paidAmount = 5000.0, pendingAmount = 0.0, dueDate = "2026-06-15", status = FeeAccountStatus.CLEARED))
        feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s3Id, feeStructureId = fee2Id, feeCategory = FeeCategory.TUITION_FEE, totalAmount = 20000.0, paidAmount = 5000.0, pendingAmount = 15000.0, dueDate = "2026-10-15", status = FeeAccountStatus.PARTIAL))
        feeDao.insertStudentFeeAccount(StudentFeeAccountEntity(studentId = s4Id, feeStructureId = fee1Id, feeCategory = FeeCategory.ADMISSION_FEE, totalAmount = 5000.0, paidAmount = 2500.0, pendingAmount = 2500.0, dueDate = "2026-06-15", status = FeeAccountStatus.PARTIAL))

        // 11. Syllabus Chapters (Prompt Section 11 Example)
        // Mathematics: 10 Total, 7 Completed, 1 Revision, 2 Pending = 70.0%
        val sylDao = db.syllabusDao()
        val mathChapters = listOf(
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Relations and Functions", chapterNumber = 1, totalTopics = 8, completedTopics = 8, status = SyllabusStatus.REVISION, completionDate = "2026-06-25"),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Inverse Trigonometric Functions", chapterNumber = 2, totalTopics = 6, completedTopics = 6, status = SyllabusStatus.COMPLETED, completionDate = "2026-07-08"),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Matrices", chapterNumber = 3, totalTopics = 10, completedTopics = 10, status = SyllabusStatus.COMPLETED, completionDate = "2026-07-22"),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Determinants", chapterNumber = 4, totalTopics = 8, completedTopics = 8, status = SyllabusStatus.COMPLETED, completionDate = "2026-08-05"),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Continuity and Differentiability", chapterNumber = 5, totalTopics = 12, completedTopics = 12, status = SyllabusStatus.COMPLETED, completionDate = "2026-08-20"),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Application of Derivatives", chapterNumber = 6, totalTopics = 9, completedTopics = 9, status = SyllabusStatus.COMPLETED, completionDate = "2026-09-02"),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Integrals", chapterNumber = 7, totalTopics = 14, completedTopics = 14, status = SyllabusStatus.COMPLETED, completionDate = "2026-09-15"),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Application of Integrals", chapterNumber = 8, totalTopics = 6, completedTopics = 6, status = SyllabusStatus.COMPLETED, completionDate = "2026-09-16"),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Differential Equations", chapterNumber = 9, totalTopics = 10, completedTopics = 3, status = SyllabusStatus.IN_PROGRESS),
            SyllabusChapterEntity(subjectId = subMath, batchId = batch1Id, chapterName = "Vector Algebra & 3D Geometry", chapterNumber = 10, totalTopics = 12, completedTopics = 0, status = SyllabusStatus.PENDING)
        )
        sylDao.insertChaptersList(mathChapters)

        // Physics: 8 Total, 5 Completed, 1 Revision, 2 Pending = 62.5%
        val physChapters = listOf(
            SyllabusChapterEntity(subjectId = subPhys, batchId = batch1Id, chapterName = "Electric Charges and Fields", chapterNumber = 1, totalTopics = 10, completedTopics = 10, status = SyllabusStatus.REVISION, completionDate = "2026-06-30"),
            SyllabusChapterEntity(subjectId = subPhys, batchId = batch1Id, chapterName = "Electrostatic Potential", chapterNumber = 2, totalTopics = 8, completedTopics = 8, status = SyllabusStatus.COMPLETED, completionDate = "2026-07-15"),
            SyllabusChapterEntity(subjectId = subPhys, batchId = batch1Id, chapterName = "Current Electricity", chapterNumber = 3, totalTopics = 12, completedTopics = 12, status = SyllabusStatus.COMPLETED, completionDate = "2026-08-01"),
            SyllabusChapterEntity(subjectId = subPhys, batchId = batch1Id, chapterName = "Moving Charges & Magnetism", chapterNumber = 4, totalTopics = 10, completedTopics = 10, status = SyllabusStatus.COMPLETED, completionDate = "2026-08-18"),
            SyllabusChapterEntity(subjectId = subPhys, batchId = batch1Id, chapterName = "Magnetism and Matter", chapterNumber = 5, totalTopics = 6, completedTopics = 6, status = SyllabusStatus.COMPLETED, completionDate = "2026-09-02"),
            SyllabusChapterEntity(subjectId = subPhys, batchId = batch1Id, chapterName = "Electromagnetic Induction", chapterNumber = 6, totalTopics = 8, completedTopics = 8, status = SyllabusStatus.COMPLETED, completionDate = "2026-09-16"),
            SyllabusChapterEntity(subjectId = subPhys, batchId = batch1Id, chapterName = "Alternating Currents", chapterNumber = 7, totalTopics = 9, completedTopics = 2, status = SyllabusStatus.IN_PROGRESS),
            SyllabusChapterEntity(subjectId = subPhys, batchId = batch1Id, chapterName = "Electromagnetic Waves & Optics", chapterNumber = 8, totalTopics = 15, completedTopics = 0, status = SyllabusStatus.PENDING)
        )
        sylDao.insertChaptersList(physChapters)

        // Chemistry: 9 Total, 8 Completed, 0 Revision, 1 Pending = 88.9%
        val chemChapters = (1..9).map { idx ->
            val isCompleted = idx <= 8
            SyllabusChapterEntity(
                subjectId = subChem,
                batchId = batch1Id,
                chapterName = when(idx) {
                    1 -> "Solutions"
                    2 -> "Electrochemistry"
                    3 -> "Chemical Kinetics"
                    4 -> "d and f Block Elements"
                    5 -> "Coordination Compounds"
                    6 -> "Haloalkanes and Haloarenes"
                    7 -> "Alcohols, Phenols and Ethers"
                    8 -> "Aldehydes, Ketones & Carboxylic Acids"
                    else -> "Biomolecules"
                },
                chapterNumber = idx,
                totalTopics = 8,
                completedTopics = if (isCompleted) 8 else 0,
                status = if (isCompleted) SyllabusStatus.COMPLETED else SyllabusStatus.PENDING,
                completionDate = if (isCompleted) "2026-09-0$idx" else ""
            )
        }
        sylDao.insertChaptersList(chemChapters)

        // 12. Initial Audit Logs
        val auditDao = db.auditDao()
        auditDao.insertAuditLog(AuditLogEntity(userId = "1 (Director)", action = "INITIALIZATION", module = "System", recordId = "SYS-001", newValue = "Initialized EZIGO ERP with Chapter Educational Institute database", reason = "System Deployment"))
        auditDao.insertAuditLog(AuditLogEntity(userId = "3 (Accountant)", action = "PAYMENT", module = "Fees", recordId = "REC-2026-0001", newValue = "Received ₹5000 Admission Fee for Abhin S.", reason = "Admission clearance"))
        auditDao.insertAuditLog(AuditLogEntity(userId = "2 (Faculty)", action = "ATTENDANCE", module = "Attendance", recordId = "ATT-2026-09-16", newValue = "Recorded Morning & Evening attendance for Plus One Science", reason = "Daily routine"))

        // 13. Notifications
        val notifDao = db.notificationDao()
        notifDao.insertNotification(NotificationEntity(title = "Low Attendance Notice", message = "Student Rahul M. attendance has dropped below 75% threshold in Plus One Science.", type = "ALERT"))
        notifDao.insertNotification(NotificationEntity(title = "Pending Tuition Fees", message = "Plus One Science Tuition Fee installment due on Oct 15, 2026. Review outstanding balances.", type = "WARNING"))
        notifDao.insertNotification(NotificationEntity(title = "Marks Entry Complete", message = "Terminal Examination 2026 marks matrix generated successfully for Plus One Science.", type = "SUCCESS"))
    }
}
