package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class UnitStatus {
    AVAILABLE,
    DRAFT,
    SUBMITTED,
    APPROVED,
    DROPPED
}

@Serializable
data class SyllabusTopic(
    val weekNumber: Int,
    val title: String,
    val subtopics: List<String>
)

@Serializable
data class CourseUnit(
    val unitCode: String,
    val unitTitle: String,
    val credits: Int = 3,
    val academicYear: String = "2025/2026",
    val semester: Int = 2,
    val lecturerName: String,
    val lecturerEmail: String,
    val lecturerOffice: String = "Department of Computer Science, Chiromo",
    val venueName: String,
    val campus: String,
    val isCore: Boolean = true,
    val prerequisites: List<String> = emptyList(),
    val status: UnitStatus = UnitStatus.AVAILABLE,
    val scheduleTime: String = "Mon 09:00 - 11:00",
    val description: String = "",
    val syllabusTopics: List<SyllabusTopic> = emptyList(),
    val learningOutcomes: List<String> = emptyList(),
    val recommendedTextbooks: List<String> = emptyList()
)
