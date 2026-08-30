package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExamTimetableItem(
    val id: String,
    val unitCode: String,
    val unitTitle: String,
    val examDate: String, // "16 Mar 2026"
    val dayOfWeek: String, // "Monday"
    val startTime: String, // "09:00 AM"
    val endTime: String, // "12:00 PM"
    val session: String, // "Morning Session" or "Afternoon Session"
    val venue: String, // "8-4-4 Multi-Purpose Hall"
    val campus: String, // "Main Campus"
    val faculty: String, // "Faculty of Science & Technology"
    val chiefInvigilator: String, // "Prof. R. Okoth"
    val isAuthorized: Boolean = true
)
