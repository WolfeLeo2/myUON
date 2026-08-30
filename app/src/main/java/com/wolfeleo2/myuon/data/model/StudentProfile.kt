package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StudentProfile(
    val regNo: String,
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
    val photoUrl: String? = null,
    val isFeeCleared: Boolean = true,
    val isBiometricEnabled: Boolean = false,
)
