package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class RequestStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}

@Serializable
data class SpecialExamRequest(
    val requestId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val academicYear: String,
    val semester: Int,
    val reasonCategory: String, // Medical, Bereavement, Compassionate
    val explanation: String,
    val medicalOfficerApproval: Boolean = false,
    val deanApproval: Boolean = false,
    val status: RequestStatus = RequestStatus.PENDING,
    val submissionDate: String
)

@Serializable
data class SupplementaryRequest(
    val requestId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val previousScore: Double,
    val feeAmount: Double = 1000.0,
    val paymentReference: String? = null,
    val isPaid: Boolean = false,
    val status: RequestStatus = RequestStatus.PENDING,
    val submissionDate: String
)

@Serializable
data class MissingMarksDispute(
    val disputeId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val lecturerName: String,
    val academicYear: String,
    val semester: Int,
    val missingComponent: String, // CAT, Exam, Both
    val evidenceNote: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val submittedDate: String
)
