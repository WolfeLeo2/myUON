package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExamCardItem(
    val unitCode: String,
    val unitTitle: String,
    val examDate: String,
    val examTime: String,
    val venue: String,
    val deskNumber: String? = null
)

@Serializable
data class ExamCard(
    val cardId: String,
    val regNo: String,
    val studentName: String,
    val faculty: String,
    val program: String,
    val academicYear: String,
    val semester: Int,
    val passportPhotoUrl: String? = null,
    val isFeeCleared: Boolean,
    val isUnitsApproved: Boolean,
    val qrVerificationToken: String,
    val units: List<ExamCardItem>,
    val generatedDate: String,
    val authorizedBy: String = "Academic Registrar (Examinations)"
)
