package com.wolfeleo2.myuon.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wolfeleo2.myuon.data.model.ClassType
import com.wolfeleo2.myuon.data.model.CourseUnit
import com.wolfeleo2.myuon.data.model.GenderTarget
import com.wolfeleo2.myuon.data.model.GradeRecord
import com.wolfeleo2.myuon.data.model.HostelHall
import com.wolfeleo2.myuon.data.model.StudentProfile
import com.wolfeleo2.myuon.data.model.SyllabusTopic
import com.wolfeleo2.myuon.data.model.TimetableItem
import com.wolfeleo2.myuon.data.model.UnitStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val regNo: String,
    val fullName: String,
    val studentEmail: String,
    val faculty: String,
    val department: String,
    val program: String,
    val yearOfStudy: Int,
    val semester: Int,
    val campus: String,
    val nationalId: String,
    val mobileNumber: String,
    val photoUrl: String?,
    val isFeeCleared: Boolean,
    val isBiometricEnabled: Boolean,
    val currentAverage: Double
)

@Entity(tableName = "course_units")
data class CourseUnitEntity(
    @PrimaryKey val unitCode: String,
    val unitTitle: String,
    val credits: Int,
    val academicYear: String,
    val semester: Int,
    val lecturerName: String,
    val lecturerEmail: String,
    val lecturerOffice: String = "Department of Computer Science, Chiromo",
    val venueName: String,
    val campus: String,
    val isCore: Boolean,
    val prerequisitesJson: String = "[]",
    val status: UnitStatus,
    val scheduleTime: String,
    val description: String,
    val syllabusTopicsJson: String = "[]",
    val learningOutcomesJson: String = "[]",
    val recommendedTextbooksJson: String = "[]"
)

@Entity(tableName = "grade_records", primaryKeys = ["unitCode", "academicYear", "semester"])
data class GradeRecordEntity(
    val unitCode: String,
    val unitTitle: String,
    val academicYear: String,
    val semester: Int,
    val credits: Int,
    val catScore: Double,
    val examScore: Double,
    val totalScore: Double,
    val gradeLetter: String,
    val isPass: Boolean,
    val isSupplementary: Boolean,
    val isSpecial: Boolean,
    val remarks: String
)

@Entity(tableName = "timetable_slots")
data class TimetableEntity(
    @PrimaryKey val id: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val unitCode: String,
    val unitTitle: String,
    val lecturer: String,
    val lecturerEmail: String,
    val venue: String,
    val campus: String,
    val classType: ClassType,
    val isOnline: Boolean,
    val notes: String?
)

@Entity(tableName = "hostel_halls")
data class HostelHallEntity(
    @PrimaryKey val hallId: String,
    val hallName: String,
    val campus: String,
    val genderTarget: GenderTarget,
    val totalRooms: Int,
    val availableRooms: Int,
    val rentPerSemester: Double,
    val isBookingOpen: Boolean
)

private val json = Json { ignoreUnknownKeys = true }

fun StudentEntity.toDomain() = StudentProfile(
    regNo = regNo,
    fullName = fullName,
    studentEmail = studentEmail,
    faculty = faculty,
    department = department,
    program = program,
    yearOfStudy = yearOfStudy,
    semester = semester,
    campus = campus,
    nationalId = nationalId,
    mobileNumber = mobileNumber,
    photoUrl = photoUrl,
    isFeeCleared = isFeeCleared,
    isBiometricEnabled = isBiometricEnabled
)

fun StudentProfile.toEntity(currentAverage: Double = 0.0) = StudentEntity(
    regNo = regNo,
    fullName = fullName,
    studentEmail = studentEmail,
    faculty = faculty,
    department = department,
    program = program,
    yearOfStudy = yearOfStudy,
    semester = semester,
    campus = campus,
    nationalId = nationalId,
    mobileNumber = mobileNumber,
    photoUrl = photoUrl,
    isFeeCleared = isFeeCleared,
    isBiometricEnabled = isBiometricEnabled,
    currentAverage = currentAverage
)

fun CourseUnitEntity.toDomain(): CourseUnit {
    val topics = runCatching { json.decodeFromString<List<SyllabusTopic>>(syllabusTopicsJson) }.getOrDefault(emptyList())
    val outcomes = runCatching { json.decodeFromString<List<String>>(learningOutcomesJson) }.getOrDefault(emptyList())
    val books = runCatching { json.decodeFromString<List<String>>(recommendedTextbooksJson) }.getOrDefault(emptyList())
    val prereqs = runCatching { json.decodeFromString<List<String>>(prerequisitesJson) }.getOrDefault(emptyList())

    return CourseUnit(
        unitCode = unitCode,
        unitTitle = unitTitle,
        credits = credits,
        academicYear = academicYear,
        semester = semester,
        lecturerName = lecturerName,
        lecturerEmail = lecturerEmail,
        lecturerOffice = lecturerOffice,
        venueName = venueName,
        campus = campus,
        isCore = isCore,
        prerequisites = prereqs,
        status = status,
        scheduleTime = scheduleTime,
        description = description,
        syllabusTopics = topics,
        learningOutcomes = outcomes,
        recommendedTextbooks = books
    )
}

fun CourseUnit.toEntity() = CourseUnitEntity(
    unitCode = unitCode,
    unitTitle = unitTitle,
    credits = credits,
    academicYear = academicYear,
    semester = semester,
    lecturerName = lecturerName,
    lecturerEmail = lecturerEmail,
    lecturerOffice = lecturerOffice,
    venueName = venueName,
    campus = campus,
    isCore = isCore,
    prerequisitesJson = json.encodeToString(prerequisites),
    status = status,
    scheduleTime = scheduleTime,
    description = description,
    syllabusTopicsJson = json.encodeToString(syllabusTopics),
    learningOutcomesJson = json.encodeToString(learningOutcomes),
    recommendedTextbooksJson = json.encodeToString(recommendedTextbooks)
)

fun GradeRecordEntity.toDomain() = GradeRecord(
    unitCode = unitCode,
    unitTitle = unitTitle,
    academicYear = academicYear,
    semester = semester,
    credits = credits,
    catScore = catScore,
    examScore = examScore,
    totalScore = totalScore,
    gradeLetter = gradeLetter,
    isPass = isPass,
    isSupplementary = isSupplementary,
    isSpecial = isSpecial,
    remarks = remarks
)

fun GradeRecord.toEntity() = GradeRecordEntity(
    unitCode = unitCode,
    unitTitle = unitTitle,
    academicYear = academicYear,
    semester = semester,
    credits = credits,
    catScore = catScore,
    examScore = examScore,
    totalScore = totalScore,
    gradeLetter = gradeLetter,
    isPass = isPass,
    isSupplementary = isSupplementary,
    isSpecial = isSpecial,
    remarks = remarks
)

fun TimetableEntity.toDomain() = TimetableItem(
    id = id,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    unitCode = unitCode,
    unitTitle = unitTitle,
    lecturer = lecturer,
    lecturerEmail = lecturerEmail,
    venue = venue,
    campus = campus,
    classType = classType,
    isOnline = isOnline,
    notes = notes
)

fun TimetableItem.toEntity() = TimetableEntity(
    id = id,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    unitCode = unitCode,
    unitTitle = unitTitle,
    lecturer = lecturer,
    lecturerEmail = lecturerEmail,
    venue = venue,
    campus = campus,
    classType = classType,
    isOnline = isOnline,
    notes = notes
)

fun HostelHallEntity.toDomain() = HostelHall(
    hallId = hallId,
    hallName = hallName,
    campus = campus,
    genderTarget = genderTarget,
    totalRooms = totalRooms,
    availableRooms = availableRooms,
    rentPerSemester = rentPerSemester,
    isBookingOpen = isBookingOpen
)

fun HostelHall.toEntity() = HostelHallEntity(
    hallId = hallId,
    hallName = hallName,
    campus = campus,
    genderTarget = genderTarget,
    totalRooms = totalRooms,
    availableRooms = availableRooms,
    rentPerSemester = rentPerSemester,
    isBookingOpen = isBookingOpen
)
