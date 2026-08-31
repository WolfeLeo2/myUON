package com.wolfeleo2.myuon.data.remote

import com.wolfeleo2.myuon.BuildConfig
import com.wolfeleo2.myuon.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SignUpApiRequest(
    val email: String,
    val password: String,
    val regNo: String,
    val fullName: String,
    val faculty: String,
    val department: String,
    val program: String,
    val yearOfStudy: Int,
    val semester: Int,
    val campus: String,
    val nationalId: String,
    val mobileNumber: String,
    val photoUrl: String? = null,
    val isFeeCleared: Boolean = true
)

@Serializable
data class LoginApiRequest(val mode: String, val identifier: String, val password: String)

@Serializable
data class AuthApiResponse(val userId: String, val sessionToken: String)

@Serializable
data class RegisterUnitsApiRequest(
    val regNo: String,
    val unitCodes: List<String>,
    val academicYear: String = "2025/2026",
    val semester: Int = 2
)

@Serializable
data class MpesaPayApiRequest(
    val regNo: String,
    val amount: Double,
    val phoneNumber: String,
    val description: String = "Tuition Fee Payment via M-Pesa"
)

@Serializable
data class BookHostelApiRequest(
    val regNo: String,
    val hallName: String,
    val roomNumber: String,
    val bedSpace: String = "A",
    val academicYear: String = "2025/2026",
    val semester: Int = 2,
    val rentAmount: Double = 6500.0
)

@Serializable
data class PayHostelApiRequest(
    val bookingId: String,
    val phoneNumber: String
)

@Singleton
class MyUonApiClient @Inject constructor() {

    private val baseUrl: String = BuildConfig.API_BASE_URL

    val httpClient: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    suspend fun login(mode: String, identifier: String, pass: String): AuthApiResponse? {
        return try {
            val response = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginApiRequest(mode, identifier, pass))
            }
            if (response.status.isSuccess()) response.body<AuthApiResponse>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun signUp(request: SignUpApiRequest): AuthApiResponse? {
        return try {
            val response = httpClient.post("$baseUrl/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) response.body<AuthApiResponse>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getAvailableUnits(academicYear: String = "2025/2026", semester: Int = 0): List<CourseUnit>? {
        return try {
            val query = if (semester in 1..3) "?academicYear=$academicYear&semester=$semester" else ""
            val response = httpClient.get("$baseUrl/units$query")
            if (response.status.isSuccess()) response.body<List<CourseUnit>>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getUnitByCode(unitCode: String): CourseUnit? {
        return try {
            val response = httpClient.get("$baseUrl/units/$unitCode")
            if (response.status.isSuccess()) response.body<CourseUnit>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun registerUnits(regNo: String, unitCodes: List<String>): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/units/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterUnitsApiRequest(regNo = regNo, unitCodes = unitCodes))
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getGrades(regNo: String): List<GradeRecord>? {
        return try {
            val response = httpClient.get("$baseUrl/grades?regNo=$regNo")
            if (response.status.isSuccess()) response.body<List<GradeRecord>>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getTimetable(regNo: String): List<TimetableItem>? {
        return try {
            val response = httpClient.get("$baseUrl/timetable?regNo=$regNo")
            if (response.status.isSuccess()) response.body<List<TimetableItem>>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getExamTimetable(regNo: String): List<ExamTimetableItem>? {
        return try {
            val response = httpClient.get("$baseUrl/exams/timetable?regNo=$regNo")
            if (response.status.isSuccess()) response.body<List<ExamTimetableItem>>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getExamCard(regNo: String): ExamCard? {
        return try {
            val response = httpClient.get("$baseUrl/exams/card?regNo=$regNo")
            if (response.status.isSuccess()) response.body<ExamCard>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getAcademicRequests(regNo: String): AcademicRequestsResponse? {
        return try {
            val response = httpClient.get("$baseUrl/requests?regNo=$regNo")
            if (response.status.isSuccess()) response.body<AcademicRequestsResponse>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun submitSpecialExamRequest(request: SpecialExamRequest): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/requests/special-exam") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun submitSupplementaryRequest(request: SupplementaryRequest): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/requests/supplementary") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun submitMissingMarksDispute(dispute: MissingMarksDispute): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/requests/missing-marks") {
                contentType(ContentType.Application.Json)
                setBody(dispute)
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getAttendanceOverview(regNo: String): List<AttendanceSummary>? {
        return try {
            val response = httpClient.get("$baseUrl/attendance/overview?regNo=$regNo")
            if (response.status.isSuccess()) response.body<List<AttendanceSummary>>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getStudentAttendance(regNo: String, unitCode: String): AttendanceSummary? {
        return try {
            val response = httpClient.get("$baseUrl/attendance/summary?regNo=$regNo&unitCode=$unitCode")
            if (response.status.isSuccess()) response.body<AttendanceSummary>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getFeeStatement(regNo: String, year: String = "2025/2026", sem: Int = 2): FeeStatement? {
        return try {
            val response = httpClient.get("$baseUrl/fees/statement?regNo=$regNo&academicYear=$year&semester=$sem")
            if (response.status.isSuccess()) response.body<FeeStatement>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun recordMpesaPayment(regNo: String, amount: Double, phone: String): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/fees/mpesa-pay") {
                contentType(ContentType.Application.Json)
                setBody(MpesaPayApiRequest(regNo = regNo, amount = amount, phoneNumber = phone))
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getHostels(): List<HostelHall>? {
        return try {
            val response = httpClient.get("$baseUrl/hostels")
            if (response.status.isSuccess()) response.body<List<HostelHall>>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getHostelBooking(regNo: String): HostelRoomBooking? {
        return try {
            val response = httpClient.get("$baseUrl/hostels/booking?regNo=$regNo")
            if (response.status.isSuccess()) response.body<HostelRoomBooking>() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun bookHostel(regNo: String, hallName: String, roomNumber: String, bedSpace: String, rent: Double): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/hostels/book") {
                contentType(ContentType.Application.Json)
                setBody(BookHostelApiRequest(regNo, hallName, roomNumber, bedSpace, rentAmount = rent))
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun payHostel(bookingId: String, phone: String = "0712345678"): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/hostels/pay") {
                contentType(ContentType.Application.Json)
                setBody(PayHostelApiRequest(bookingId, phone))
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchDelta(since: String? = null, regNo: String? = null): com.wolfeleo2.myuon.data.model.SyncDeltaPayload? {
        return try {
            val queryParams = mutableListOf<String>()
            if (!since.isNullOrBlank()) queryParams.add("since=$since")
            if (!regNo.isNullOrBlank()) queryParams.add("regNo=$regNo")
            val queryString = if (queryParams.isNotEmpty()) "?" + queryParams.joinToString("&") else ""
            val response = httpClient.get("$baseUrl/sync/delta$queryString")
            if (response.status.isSuccess()) response.body<com.wolfeleo2.myuon.data.model.SyncDeltaPayload>() else null
        } catch (_: Exception) {
            null
        }
    }
}
