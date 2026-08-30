package com.wolfeleo2.myuon.server.domain

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class StudentProfileTest {
    private val profile = StudentProfile(
        userId = "user-1",
        regNo = "P15/12345/2022",
        fullName = "Jane Student",
        studentEmail = "jane.student@students.uonbi.ac.ke",
        faculty = "Science",
        department = "Computing",
        program = "BSc Computer Science",
        yearOfStudy = 3,
        semester = 1,
        campus = "Main",
        nationalId = "12345678",
        mobileNumber = "0700000000",
        photoUrl = null,
        isFeeCleared = true,
    )

    @Test
    fun `round-trips through JSON unchanged`() {
        val json = Json.encodeToString(profile)
        val decoded = Json.decodeFromString<StudentProfile>(json)
        assertEquals(profile, decoded)
    }
}
