package com.wolfeleo2.myuon.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        StudentEntity::class,
        CourseUnitEntity::class,
        GradeRecordEntity::class,
        TimetableEntity::class,
        HostelHallEntity::class,
        HostelBookingEntity::class,
        AttendanceSummaryEntity::class,
        FeeStatementEntity::class,
        ExamCardEntity::class,
        ExamTimetableEntity::class,
        SpecialExamRequestEntity::class,
        SupplementaryRequestEntity::class,
        MissingMarksDisputeEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MyUonDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun courseUnitDao(): CourseUnitDao
    abstract fun gradeDao(): GradeDao
    abstract fun timetableDao(): TimetableDao
    abstract fun hostelDao(): HostelDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun feeDao(): FeeDao
    abstract fun examCardDao(): ExamCardDao
    abstract fun examTimetableDao(): ExamTimetableDao
    abstract fun academicRequestDao(): AcademicRequestDao
}
