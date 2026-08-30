package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

/**
 * A single scheduled class meeting (lecture/lab/tutorial) for a unit, and whether the
 * student attended it.
 */
@Serializable
data class ClassSessionAttendance(
    val id: String,
    val date: String,
    val timeSlot: String,
    val sessionType: String, // "Lecture", "Lab", "Tutorial"
    val topicCovered: String,
    val hours: Double,
    val isAttended: Boolean = true,
    val venue: String
)

/**
 * One calendar week's worth of scheduled sessions for a unit. UoN units typically meet
 * once or twice a week (fixed lecture/lab/tutorial slots), so [sessions] normally holds
 * 1-2 entries, not one-per-day.
 */
@Serializable
data class AttendanceWeekRecord(
    val weekLabel: String, // e.g. "Week 8"
    val weekStartDate: String, // e.g. "Mon, Oct 20"
    val sessions: List<ClassSessionAttendance>
) {
    /** Sessions attended / sessions held * 100, for this week only. 100.0 if no sessions were held. */
    val percentageAttended: Double
        get() {
            if (sessions.isEmpty()) return 100.0
            val attended = sessions.count { it.isAttended }
            return (attended.toDouble() / sessions.size) * 100.0
        }
}

/**
 * Semester-to-date attendance for a single unit.
 *
 * [weeklyBreakdown] is ordered oldest-to-newest (most recent week last), matching how
 * a semester actually plays out and how the week selector's "next" chevron should move.
 */
@Serializable
data class AttendanceSummary(
    val unitCode: String,
    val unitTitle: String,
    val totalLecturesHeld: Int,
    val lecturesAttended: Int,
    val totalLabSessionsHeld: Int,
    val labSessionsAttended: Int,
    val weeklyBreakdown: List<AttendanceWeekRecord>,
    val recentSessions: List<ClassSessionAttendance>
) {
    val overallPercentage: Double
        get() {
            val total = totalLecturesHeld + totalLabSessionsHeld
            val attended = lecturesAttended + labSessionsAttended
            return if (total > 0) (attended.toDouble() / total) * 100.0 else 100.0
        }

    /** Senate Rule: a student must attend at least 75% of scheduled sessions. */
    val isSenateThresholdMet: Boolean
        get() = overallPercentage >= 75.0
}
