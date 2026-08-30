package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.domain.AcademicRepository
import com.wolfeleo2.myuon.server.domain.AcademicSummary
import com.wolfeleo2.myuon.server.domain.ExamCard
import com.wolfeleo2.myuon.server.domain.ExamCardItem
import com.wolfeleo2.myuon.server.domain.MissingMarksDispute
import com.wolfeleo2.myuon.server.domain.SpecialExamRequest
import com.wolfeleo2.myuon.server.domain.StudentRepository
import com.wolfeleo2.myuon.server.domain.SupplementaryRequest
import com.wolfeleo2.myuon.server.domain.Validation
import com.wolfeleo2.myuon.server.domain.classifyDegree
import com.wolfeleo2.myuon.server.domain.cumulativeAverage
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RegisterUnitsRequest(
    val regNo: String,
    val unitCodes: List<String>,
    val academicYear: String = "2025/2026",
    val semester: Int = 2
)

fun Route.academicRoutes(
    academicRepository: AcademicRepository,
    studentRepository: StudentRepository
) {
    route("/units") {
        get {
            val academicYear = call.request.queryParameters["academicYear"] ?: "2025/2026"
            val semester = call.request.queryParameters["semester"]?.toIntOrNull() ?: 2
            val yearOfStudy = call.request.queryParameters["yearOfStudy"]?.toIntOrNull()
            require(semester in 1..3) { "Semester must be 1, 2, or 3" }
            val units = academicRepository.getAvailableUnits(academicYear, semester, yearOfStudy)
            call.respond(units)
        }

        get("/registered") {
            val regNo = call.request.queryParameters["regNo"]
            if (regNo.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing regNo parameter"))
                return@get
            }
            Validation.validateRegNo(regNo)
            val academicYear = call.request.queryParameters["academicYear"] ?: "2025/2026"
            val semester = call.request.queryParameters["semester"]?.toIntOrNull() ?: 2
            val registered = academicRepository.getRegisteredUnits(regNo, academicYear, semester)
            call.respond(registered)
        }

        post("/register") {
            val request = call.receive<RegisterUnitsRequest>()
            Validation.validateRegNo(request.regNo)
            require(request.unitCodes.isNotEmpty()) { "At least one unit code is required" }
            request.unitCodes.forEach { Validation.validateUnitCode(it) }
            val registrations = academicRepository.registerUnits(
                request.regNo,
                request.unitCodes,
                request.academicYear,
                request.semester
            )
            call.respond(HttpStatusCode.Created, registrations)
        }
    }

    get("/grades") {
        val regNo = call.request.queryParameters["regNo"]
        if (regNo.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing regNo parameter"))
            return@get
        }
        Validation.validateRegNo(regNo)
        val grades = academicRepository.getGradeRecords(regNo)
        call.respond(grades)
    }

    get("/academics/summary") {
        val regNo = call.request.queryParameters["regNo"]
        if (regNo.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing regNo parameter"))
            return@get
        }
        Validation.validateRegNo(regNo)
        val grades = academicRepository.getGradeRecords(regNo)
        val average = cumulativeAverage(grades)
        val degreeClass = classifyDegree(average)
        val passedUnits = grades.count { it.isPass }
        val earnedCredits = grades.filter { it.isPass }.sumOf { it.credits }

        val summary = AcademicSummary(
            regNo = regNo,
            cumulativeAverage = average,
            degreeClass = degreeClass,
            totalUnitsPassed = passedUnits,
            totalCreditsEarned = earnedCredits
        )
        call.respond(summary)
    }

    get("/timetable") {
        val regNo = call.request.queryParameters["regNo"] ?: ""
        if (regNo.isNotBlank()) Validation.validateRegNo(regNo)
        val items = academicRepository.getTimetable(regNo)
        call.respond(items)
    }

    get("/exams/timetable") {
        val regNo = call.request.queryParameters["regNo"] ?: ""
        if (regNo.isNotBlank()) Validation.validateRegNo(regNo)
        val items = academicRepository.getExamTimetable(regNo)
        call.respond(items)
    }

    get("/exams/card") {
        val regNo = call.request.queryParameters["regNo"]
        if (regNo.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing regNo parameter"))
            return@get
        }
        Validation.validateRegNo(regNo)
        val student = studentRepository.findByRegNo(regNo)
        val examTimetable = academicRepository.getExamTimetable(regNo)
        val examItems = examTimetable.map {
            ExamCardItem(
                unitCode = it.unitCode,
                unitTitle = it.unitTitle,
                examDate = it.examDate,
                examTime = "${it.startTime} - ${it.endTime}",
                venue = it.venue,
                deskNumber = "DK-${(10..99).random()}"
            )
        }

        val card = ExamCard(
            cardId = "EC-${UUID.randomUUID().toString().take(8).uppercase()}",
            regNo = regNo,
            studentName = student?.fullName ?: "Student",
            faculty = student?.faculty ?: "Science & Technology",
            program = student?.program ?: "BSc Computer Science",
            academicYear = "2025/2026",
            semester = 2,
            passportPhotoUrl = student?.photoUrl,
            isFeeCleared = student?.isFeeCleared ?: true,
            isUnitsApproved = true,
            qrVerificationToken = "UON-VERIFY-${UUID.randomUUID().toString().take(12)}",
            units = examItems,
            generatedDate = "2026-08-30"
        )
        call.respond(card)
    }

    get("/attendance/summary") {
        val regNo = call.request.queryParameters["regNo"]
        val unitCode = call.request.queryParameters["unitCode"]
        if (regNo.isNullOrBlank() || unitCode.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("regNo and unitCode parameters required"))
            return@get
        }
        Validation.validateRegNo(regNo)
        Validation.validateUnitCode(unitCode)
        val summary = academicRepository.getStudentAttendance(regNo, unitCode)
        if (summary != null) {
            call.respond(summary)
        } else {
            call.respond(HttpStatusCode.NotFound, ErrorResponse("No attendance record found for $unitCode"))
        }
    }

    route("/requests") {
        get {
            val regNo = call.request.queryParameters["regNo"]
            if (regNo.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing regNo parameter"))
                return@get
            }
            Validation.validateRegNo(regNo)
            val requests = academicRepository.getRequests(regNo)
            call.respond(requests)
        }

        post("/special-exam") {
            val request = call.receive<SpecialExamRequest>()
            Validation.validateRegNo(request.regNo)
            Validation.validateUnitCode(request.unitCode)
            require(request.explanation.isNotBlank()) { "Explanation is required" }
            val saved = academicRepository.submitSpecialExamRequest(request)
            call.respond(HttpStatusCode.Created, saved)
        }

        post("/supplementary") {
            val request = call.receive<SupplementaryRequest>()
            Validation.validateRegNo(request.regNo)
            Validation.validateUnitCode(request.unitCode)
            val saved = academicRepository.submitSupplementaryRequest(request)
            call.respond(HttpStatusCode.Created, saved)
        }

        post("/missing-marks") {
            val request = call.receive<MissingMarksDispute>()
            Validation.validateRegNo(request.regNo)
            Validation.validateUnitCode(request.unitCode)
            require(request.evidenceNote.isNotBlank()) { "Evidence note is required" }
            val saved = academicRepository.submitMissingMarksDispute(request)
            call.respond(HttpStatusCode.Created, saved)
        }
    }
}
