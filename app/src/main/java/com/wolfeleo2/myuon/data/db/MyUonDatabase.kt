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
        HostelHallEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyUonDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun courseUnitDao(): CourseUnitDao
    abstract fun gradeDao(): GradeDao
    abstract fun timetableDao(): TimetableDao
    abstract fun hostelDao(): HostelDao
}
