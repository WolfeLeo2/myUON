package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.domain.FeeRepository
import com.wolfeleo2.myuon.server.domain.FeeTransaction
import com.wolfeleo2.myuon.server.domain.TransactionType
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
data class MpesaPayRequest(
    val regNo: String,
    val amount: Double,
    val phoneNumber: String,
    val description: String = "Tuition Fee Payment via M-Pesa"
)

fun Route.feeRoutes(feeRepository: FeeRepository) {
    route("/fees") {
        get("/statement") {
            val regNo = call.request.queryParameters["regNo"]?.trim() ?: "P15/12345/2022"
            Validation.validateRegNo(regNo)
            val academicYear = call.request.queryParameters["academicYear"] ?: "2025/2026"
            val semester = call.request.queryParameters["semester"]?.toIntOrNull() ?: 2
            val statement = feeRepository.getFeeStatement(regNo, academicYear, semester)
            if (statement != null) {
                call.respond(statement)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("No fee statement found for $regNo"))
            }
        }

        get("/transactions") {
            val regNo = call.request.queryParameters["regNo"]?.trim() ?: "P15/12345/2022"
            Validation.validateRegNo(regNo)
            val txs = feeRepository.getFeeTransactions(regNo)
            call.respond(txs)
        }

        post("/mpesa-pay") {
            val request = call.receive<MpesaPayRequest>()
            Validation.validateRegNo(request.regNo)
            Validation.validateAmount(request.amount)
            Validation.validatePhone(request.phoneNumber)

            val refNo = "QK${UUID.randomUUID().toString().take(8).uppercase()}"
            val tx = FeeTransaction(
                id = UUID.randomUUID().toString(),
                regNo = request.regNo,
                referenceNumber = refNo,
                date = "2026-08-30",
                description = "${request.description} (${request.phoneNumber})",
                type = TransactionType.PAYMENT_MPESA,
                amount = request.amount,
                balanceAfter = 0.0,
                isVerified = true
            )
            val recorded = feeRepository.recordTransaction(tx)
            call.respond(HttpStatusCode.Created, recorded)
        }
    }
}
