package com.wolfeleo2.myuon.di

import android.content.Context
import androidx.room.Room
import com.wolfeleo2.myuon.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyUonDatabase {
        return Room.databaseBuilder(
            context,
            MyUonDatabase::class.java,
            "myuon.db"
        ).fallbackToDestructiveMigrationOnDowngrade(true)
        .build()
    }

    @Provides
    fun provideStudentDao(db: MyUonDatabase): StudentDao = db.studentDao()

    @Provides
    fun provideCourseUnitDao(db: MyUonDatabase): CourseUnitDao = db.courseUnitDao()

    @Provides
    fun provideGradeDao(db: MyUonDatabase): GradeDao = db.gradeDao()

    @Provides
    fun provideTimetableDao(db: MyUonDatabase): TimetableDao = db.timetableDao()

    @Provides
    fun provideHostelDao(db: MyUonDatabase): HostelDao = db.hostelDao()

    @Provides
    fun provideAttendanceDao(db: MyUonDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    fun provideFeeDao(db: MyUonDatabase): FeeDao = db.feeDao()

    @Provides
    fun provideExamCardDao(db: MyUonDatabase): ExamCardDao = db.examCardDao()

    @Provides
    fun provideExamTimetableDao(db: MyUonDatabase): ExamTimetableDao = db.examTimetableDao()

    @Provides
    fun provideAcademicRequestDao(db: MyUonDatabase): AcademicRequestDao = db.academicRequestDao()
}
