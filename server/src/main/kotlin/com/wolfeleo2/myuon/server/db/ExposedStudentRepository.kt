package com.wolfeleo2.myuon.server.db

import com.wolfeleo2.myuon.server.domain.StudentProfile
import com.wolfeleo2.myuon.server.domain.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedStudentRepository(private val database: Database) : StudentRepository {

    override suspend fun create(profile: StudentProfile): StudentProfile = dbQuery {
        StudentsTable.insert {
            it[userId] = profile.userId
            it[regNo] = profile.regNo
            it[fullName] = profile.fullName
            it[studentEmail] = profile.studentEmail
            it[faculty] = profile.faculty
            it[department] = profile.department
            it[program] = profile.program
            it[yearOfStudy] = profile.yearOfStudy
            it[semester] = profile.semester
            it[campus] = profile.campus
            it[nationalId] = profile.nationalId
            it[mobileNumber] = profile.mobileNumber
            it[photoUrl] = profile.photoUrl
            it[isFeeCleared] = profile.isFeeCleared
        }
        profile
    }

    override suspend fun findByUserId(userId: String): StudentProfile? = dbQuery {
        StudentsTable.selectAll().where { StudentsTable.userId eq userId }
            .map { it.toStudentProfile() }
            .singleOrNull()
    }

    override suspend fun findByRegNo(regNo: String): StudentProfile? = dbQuery {
        StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }
            .map { it.toStudentProfile() }
            .singleOrNull()
    }

    private fun ResultRow.toStudentProfile() = StudentProfile(
        userId = this[StudentsTable.userId],
        regNo = this[StudentsTable.regNo],
        fullName = this[StudentsTable.fullName],
        studentEmail = this[StudentsTable.studentEmail],
        faculty = this[StudentsTable.faculty],
        department = this[StudentsTable.department],
        program = this[StudentsTable.program],
        yearOfStudy = this[StudentsTable.yearOfStudy],
        semester = this[StudentsTable.semester],
        campus = this[StudentsTable.campus],
        nationalId = this[StudentsTable.nationalId],
        mobileNumber = this[StudentsTable.mobileNumber],
        photoUrl = this[StudentsTable.photoUrl],
        isFeeCleared = this[StudentsTable.isFeeCleared],
    )

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction(database) { block() } }
}
