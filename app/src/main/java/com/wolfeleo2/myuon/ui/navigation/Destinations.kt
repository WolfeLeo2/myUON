package com.wolfeleo2.myuon.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Top-level destinations represented on the navigation bar.
 */
@Serializable
sealed interface TopLevelDestination : NavKey

@Serializable
data object Dashboard : TopLevelDestination

@Serializable
data object Academics : TopLevelDestination

@Serializable
data object Fees : TopLevelDestination

@Serializable
data object Timetable : TopLevelDestination

@Serializable
data object Hostels : TopLevelDestination

/**
 * Sub-destinations and modal workflows.
 */
@Serializable
data object Login : NavKey

@Serializable
data class UnitDetail(
    val unitCode: String
) : NavKey

@Serializable
data object ExamCardScreen : NavKey

@Serializable
data class SpecialExamRequestScreen(
    val prefillUnitCode: String = ""
) : NavKey

@Serializable
data object SupplementaryRequestScreen : NavKey

@Serializable
data class VenueMapScreen(
    val venueCode: String = ""
) : NavKey

@Serializable
data object MissingMarksDisputeScreen : NavKey

@Serializable
data object ExamTimetableRoute : NavKey

@Serializable
data class AttendanceAnalyticsRoute(
    val unitCode: String = "CSC 311"
) : NavKey
