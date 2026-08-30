package com.wolfeleo2.myuon.server.domain

import kotlinx.serialization.Serializable

@Serializable
enum class GenderTarget {
    MALE,
    FEMALE,
    CO_ED
}

@Serializable
data class HostelHall(
    val hallId: String,
    val hallName: String,
    val campus: String,
    val genderTarget: GenderTarget,
    val totalRooms: Int,
    val availableRooms: Int,
    val rentPerSemester: Double,
    val amenities: List<String> = emptyList(),
    val imageUrl: String? = null,
    val isBookingOpen: Boolean = true
)

@Serializable
data class HostelRoomBooking(
    val bookingId: String,
    val regNo: String,
    val hallName: String,
    val roomNumber: String,
    val bedSpace: String,
    val academicYear: String,
    val semester: Int,
    val rentAmount: Double,
    val isPaid: Boolean,
    val isKeyIssued: Boolean,
    val bookedDate: String
)
