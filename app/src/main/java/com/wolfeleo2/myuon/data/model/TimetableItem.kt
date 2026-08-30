package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ClassType {
    LECTURE,
    LABORATORY,
    TUTORIAL,
    SEMINAR
}

@Serializable
data class TimetableItem(
    val id: String,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val startTime: String, // "09:00"
    val endTime: String,   // "11:00"
    val unitCode: String,
    val unitTitle: String,
    val lecturer: String,
    val lecturerEmail: String,
    val venue: String,
    val campus: String,
    val classType: ClassType = ClassType.LECTURE,
    val isOnline: Boolean = false,
    val notes: String? = null
)
