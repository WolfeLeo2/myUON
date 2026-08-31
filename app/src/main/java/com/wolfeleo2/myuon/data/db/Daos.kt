package com.wolfeleo2.myuon.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE regNo = :regNo LIMIT 1")
    fun getStudent(regNo: String): Flow<StudentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStudent(student: StudentEntity)
}

@Dao
interface CourseUnitDao {
    @Query("SELECT * FROM course_units")
    fun getAllUnits(): Flow<List<CourseUnitEntity>>

    @Query("SELECT * FROM course_units WHERE semester = :semester")
    fun getUnitsForSemester(semester: Int): Flow<List<CourseUnitEntity>>

    @Query("SELECT * FROM course_units WHERE unitCode = :code LIMIT 1")
    fun getUnit(code: String): Flow<CourseUnitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<CourseUnitEntity>)

    @Update
    suspend fun updateUnit(unit: CourseUnitEntity)
}

@Dao
interface GradeDao {
    @Query("SELECT * FROM grade_records ORDER BY academicYear DESC, semester DESC")
    fun getAllGrades(): Flow<List<GradeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<GradeRecordEntity>)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_summaries")
    fun getAllAttendance(): Flow<List<AttendanceSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(summaries: List<AttendanceSummaryEntity>)
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_slots")
    fun getAllSlots(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :day")
    fun getSlotsByDay(day: String): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<TimetableEntity>)
}

@Dao
interface HostelDao {
    @Query("SELECT * FROM hostel_halls")
    fun getAllHalls(): Flow<List<HostelHallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHalls(halls: List<HostelHallEntity>)

    @Query("SELECT * FROM hostel_bookings WHERE regNo = :regNo LIMIT 1")
    fun getActiveBooking(regNo: String): Flow<HostelBookingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: HostelBookingEntity)
}

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_statements WHERE academicYear = :academicYear AND semester = :semester LIMIT 1")
    fun getFeeStatement(academicYear: String, semester: Int): Flow<FeeStatementEntity?>

    @Query("SELECT * FROM fee_statements ORDER BY academicYear DESC, semester DESC")
    fun getAllFeeStatements(): Flow<List<FeeStatementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatement(statement: FeeStatementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatements(statements: List<FeeStatementEntity>)
}

@Dao
interface ExamCardDao {
    @Query("SELECT * FROM exam_cards WHERE regNo = :regNo LIMIT 1")
    fun getExamCard(regNo: String): Flow<ExamCardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamCard(card: ExamCardEntity)
}

@Dao
interface ExamTimetableDao {
    @Query("SELECT * FROM exam_timetable_slots ORDER BY examDate ASC, startTime ASC")
    fun getAllExamSlots(): Flow<List<ExamTimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamSlots(slots: List<ExamTimetableEntity>)
}

@Dao
interface AcademicRequestDao {
    @Query("SELECT * FROM special_exam_requests WHERE regNo = :regNo ORDER BY submissionDate DESC")
    fun getSpecialExams(regNo: String): Flow<List<SpecialExamRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecialExams(requests: List<SpecialExamRequestEntity>)

    @Query("SELECT * FROM supplementary_requests WHERE regNo = :regNo ORDER BY submissionDate DESC")
    fun getSupplementaryRequests(regNo: String): Flow<List<SupplementaryRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementaryRequests(requests: List<SupplementaryRequestEntity>)

    @Query("SELECT * FROM missing_marks_disputes WHERE regNo = :regNo ORDER BY submittedDate DESC")
    fun getMissingMarksDisputes(regNo: String): Flow<List<MissingMarksDisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissingMarksDisputes(disputes: List<MissingMarksDisputeEntity>)
}
