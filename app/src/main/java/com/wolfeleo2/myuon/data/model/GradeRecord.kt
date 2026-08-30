package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class DegreeClass(val label: String) {
    FIRST_CLASS("First Class Honours"),
    SECOND_UPPER("Second Class Honours (Upper Division)"),
    SECOND_LOWER("Second Class Honours (Lower Division)"),
    PASS("Pass"),
    FAIL("Fail")
}

@Serializable
data class GradeRecord(
    val unitCode: String,
    val unitTitle: String,
    val academicYear: String,
    val semester: Int,
    val credits: Int = 3,
    val catScore: Double,      // Out of 30
    val examScore: Double,     // Out of 70
    val totalScore: Double,    // Total percentage out of 100
    val gradeLetter: String,   // A, B, C, D, E/F
    val isPass: Boolean,
    val isSupplementary: Boolean = false,
    val isSpecial: Boolean = false,
    val remarks: String = "Satisfactory"
)

/** Credit-weighted mean of [GradeRecord.totalScore] across a list of unit results, 0.0 if empty. */
fun cumulativeAverage(records: List<GradeRecord>): Double {
    val totalCredits = records.sumOf { it.credits }
    if (totalCredits == 0) return 0.0
    return records.sumOf { it.totalScore * it.credits } / totalCredits
}

/** Maps a cumulative average percentage to its university [DegreeClass] band. */
fun classifyDegree(average: Double): DegreeClass = when {
    average >= 70 -> DegreeClass.FIRST_CLASS
    average >= 60 -> DegreeClass.SECOND_UPPER
    average >= 50 -> DegreeClass.SECOND_LOWER
    average >= 40 -> DegreeClass.PASS
    else -> DegreeClass.FAIL
}

@Serializable
data class AcademicSummary(
    val cumulativeAverage: Double = 0.0,
    val degreeClass: DegreeClass = DegreeClass.FAIL
) {
    companion object {
        operator fun invoke(records: List<GradeRecord>): AcademicSummary {
            val average = cumulativeAverage(records)
            return AcademicSummary(average, classifyDegree(average))
        }
    }
}
