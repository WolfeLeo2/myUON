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
}
