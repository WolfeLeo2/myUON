package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.domain.HostelRepository
import com.wolfeleo2.myuon.server.domain.HostelRoomBooking
import com.wolfeleo2.myuon.server.domain.Validation
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
data class BookHostelRequest(
    val regNo: String,
    val hallName: String,
    val roomNumber: String,
    val bedSpace: String = "A",
    val academicYear: String = "2025/2026",
    val semester: Int = 2,
    val rentAmount: Double = 6500.0
)

@Serializable
data class PayHostelRequest(
    val bookingId: String,
    val phoneNumber: String
)

fun Route.hostelRoutes(hostelRepository: HostelRepository) {
    route("/hostels") {
        get {
            val halls = hostelRepository.getHostels()
            call.respond(halls)
        }

        get("/booking") {
            val regNo = call.request.queryParameters["regNo"]
            if (regNo.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing regNo parameter"))
                return@get
            }
            Validation.validateRegNo(regNo)
            val booking = hostelRepository.getHostelBooking(regNo)
            if (booking != null) {
                call.respond(booking)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("No active hostel booking found for $regNo"))
            }
        }

        post("/book") {
            val request = call.receive<BookHostelRequest>()
            Validation.validateRegNo(request.regNo)
            Validation.validateAmount(request.rentAmount)
            require(request.hallName.isNotBlank() && request.hallName.length <= 100) { "Valid hallName is required" }
            require(request.roomNumber.isNotBlank() && request.roomNumber.length <= 20) { "Valid roomNumber is required" }

            val booking = HostelRoomBooking(
                bookingId = UUID.randomUUID().toString(),
                regNo = request.regNo,
                hallName = request.hallName,
                roomNumber = request.roomNumber,
                bedSpace = request.bedSpace,
                academicYear = request.academicYear,
                semester = request.semester,
                rentAmount = request.rentAmount,
                isPaid = false,
                isKeyIssued = false,
                bookedDate = "2026-08-30"
            )
            val created = hostelRepository.createBooking(booking)
            call.respond(HttpStatusCode.Created, created)
        }

        post("/pay") {
            val request = call.receive<PayHostelRequest>()
            require(request.bookingId.isNotBlank()) { "bookingId is required" }
            Validation.validatePhone(request.phoneNumber)
            val success = hostelRepository.markBookingPaid(request.bookingId)
            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("status" to "PAID", "bookingId" to request.bookingId))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Booking not found"))
            }
        }
    }
}
