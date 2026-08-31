package com.wolfeleo2.myuon.ui.academics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.data.model.GradeRecord
import com.wolfeleo2.myuon.data.model.UnitStatus
import com.wolfeleo2.myuon.ui.components.AcademicsSkeleton
import com.wolfeleo2.myuon.ui.components.ExpressiveShapeBadge
import com.wolfeleo2.myuon.ui.components.ExpressiveStatCard
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.components.PullToRefreshBox
import com.wolfeleo2.myuon.ui.theme.ExpressivePolygons
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AcademicsScreen(
    viewModel: AcademicsViewModel,
    onOpenExamCard: () -> Unit,
    onOpenUnitDetail: (String) -> Unit,
    onOpenSpecialExam: (String) -> Unit,
    onOpenSupplementary: () -> Unit,
    onOpenMissingMarks: () -> Unit,
    onOpenExamTimetable: () -> Unit = {},
    onOpenAttendance: (String) -> Unit = {},
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Academic Portal",
                subtitle = "Results, Course Units & Examinations",
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick
            )
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshData,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.gradeRecords.isEmpty() && uiState.courseUnits.isEmpty()) {
                AcademicsSkeleton(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Official Material 3 Expressive Connected Button Group (Tabs)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        val tabs = listOf(
                            AcademicsTab.GRADES to "Gradebook",
                            AcademicsTab.UNIT_REGISTRATION to "Units",
                            AcademicsTab.EXAM_SERVICES to "Exams"
                        )
                        tabs.forEachIndexed { index, (tab, title) ->
                            ToggleButton(
                                checked = uiState.selectedTab == tab,
                                onCheckedChange = { viewModel.selectTab(tab) },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { role = Role.RadioButton }
                            ) {
                                Text(title, style = MaterialTheme.typography.labelMediumEmphasized)
                            }
                        }
                    }

                    when (uiState.selectedTab) {
                        AcademicsTab.GRADES -> {
                            GradesTabContent(
                                uiState = uiState,
                                onSelectYear = viewModel::selectYear,
                                onSelectSemester = viewModel::selectSemester,
                                onOpenUnitDetail = onOpenUnitDetail,
                                onOpenMissingMarks = onOpenMissingMarks,
                                onOpenAttendance = onOpenAttendance
                            )
                        }
                        AcademicsTab.UNIT_REGISTRATION -> {
                            UnitRegistrationTabContent(
                                uiState = uiState,
                                onToggleUnit = viewModel::toggleUnit,
                                onOpenUnitDetail = onOpenUnitDetail,
                                onSubmit = viewModel::submitUnitRegistration,
                                onOpenAttendance = onOpenAttendance
                            )
                        }
                        AcademicsTab.EXAM_SERVICES -> {
                            ExamServicesTabContent(
                                uiState = uiState,
                                onOpenExamCard = onOpenExamCard,
                                onOpenSpecialExam = { onOpenSpecialExam("") },
                                onOpenSupplementary = onOpenSupplementary,
                                onOpenMissingMarks = onOpenMissingMarks,
                                onOpenExamTimetable = onOpenExamTimetable
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GradesTabContent(
    uiState: AcademicsUiState,
    onSelectYear: (String) -> Unit,
    onSelectSemester: (Int) -> Unit,
    onOpenUnitDetail: (String) -> Unit,
    onOpenMissingMarks: () -> Unit,
    onOpenAttendance: (String) -> Unit
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current

    LazyColumn(
        contentPadding = PaddingValues(
            start = Spacing.screenHorizontal,
            end = Spacing.screenHorizontal,
            bottom = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.fillMaxSize()
    ) {
        // Performance Summary Header
        item {
            val avg = uiState.gradeRecords.map { it.totalScore }.average().takeIf { !it.isNaN() } ?: 74.8
            Surface(
                shape = RoundedCornerShape(Sizes.cardCornerRadius),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Semester Performance Index",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00875A),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "FIRST CLASS",
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        ExpressiveShapeBadge(
                            polygon = ExpressivePolygons.Burst,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            size = 56.dp,
                            icon = Icons.Default.Grade
                        )

                        Column {
                            Text(
                                text = String.format("%.1f%%", avg),
                                style = MaterialTheme.typography.displaySmallEmphasized,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${uiState.gradeRecords.size} Units Scored • Pass Thresholds Met",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Attendance Analytics Quick Action
        item {
            val attendanceModifier = if (sharedScope != null) {
                with(sharedScope) {
                    Modifier.fillMaxWidth().sharedBounds(
                        rememberSharedContentState(key = "attendance-general"),
                        animatedVisibilityScope = animatedScope
                    )
                }
            } else Modifier.fillMaxWidth()
            Surface(
                // Blank unitCode -> AttendanceLandingRoute (the general attendance landing
                // page), per the routing in MyUonNavDisplay. Do not hardcode a unit here.
                onClick = { onOpenAttendance("") },
                shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = attendanceModifier
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    ExpressiveShapeBadge(
                        polygon = ExpressivePolygons.Cookie9Sided,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        size = 38.dp,
                        icon = Icons.Default.BarChart
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Class Attendance Analytics (75% Senate Rule)",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View weekly session hours, target compliance & bar graphs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Academic Year & Semester Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year Selection Group
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    uiState.availableYears.forEachIndexed { index, year ->
                        ToggleButton(
                            checked = uiState.selectedYear == year,
                            onCheckedChange = { onSelectYear(year) },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                uiState.availableYears.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            modifier = Modifier.semantics { role = Role.RadioButton }
                        ) {
                            Text(year, style = MaterialTheme.typography.labelMediumEmphasized)
                        }
                    }
                }

                // Semester Selection Group
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    listOf(1, 2).forEachIndexed { index, sem ->
                        ToggleButton(
                            checked = uiState.selectedSemester == sem,
                            onCheckedChange = { onSelectSemester(sem) },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            modifier = Modifier.semantics { role = Role.RadioButton }
                        ) {
                            Text("Sem $sem", style = MaterialTheme.typography.labelMediumEmphasized)
                        }
                    }
                }
            }
        }

        // Unit Grades Breakdown
        items(uiState.gradeRecords) { grade ->
            GradeItemCard(
                grade = grade,
                onClick = { onOpenUnitDetail(grade.unitCode) }
            )
        }

        // Missing Mark Dispute Banner
        item {
            val missingMarksModifier = if (sharedScope != null) {
                with(sharedScope) {
                    Modifier.fillMaxWidth().sharedBounds(
                        rememberSharedContentState(key = "missing-marks"),
                        animatedVisibilityScope = animatedScope
                    )
                }
            } else Modifier.fillMaxWidth()
            Surface(
                onClick = onOpenMissingMarks,
                shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = missingMarksModifier
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notice a missing mark or CAT discrepancy?",
                            style = MaterialTheme.typography.titleSmallEmphasized
                        )
                        Text(
                            text = "Submit a formal dispute ticket to your department chair",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GradeItemCard(
    grade: GradeRecord,
    onClick: () -> Unit
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val cardModifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = "unit-${grade.unitCode}"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = cardModifier
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grade.unitCode,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = grade.unitTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                val gradeColor = when (grade.gradeLetter) {
                    "A" -> Color(0xFF00875A)
                    "B" -> Color(0xFF0065FF)
                    "C" -> Color(0xFFFF8B00)
                    else -> Color(0xFFDE350B)
                }

                ExpressiveShapeBadge(
                    polygon = ExpressivePolygons.Flower6Petal,
                    backgroundColor = gradeColor.copy(alpha = 0.15f),
                    contentColor = gradeColor,
                    size = 48.dp,
                    text = grade.gradeLetter
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Score Distribution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                ScoreComponentPill(
                    label = "CAT",
                    score = "${grade.catScore}/30",
                    modifier = Modifier.weight(1f)
                )
                ScoreComponentPill(
                    label = "Exam",
                    score = "${grade.examScore}/70",
                    modifier = Modifier.weight(1f)
                )
                ScoreComponentPill(
                    label = "Total",
                    score = "${grade.totalScore}%",
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScoreComponentPill(
    label: String,
    score: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = score,
                style = MaterialTheme.typography.labelMediumEmphasized,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun UnitRegistrationTabContent(
    uiState: AcademicsUiState,
    onToggleUnit: (String) -> Unit,
    onOpenUnitDetail: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenAttendance: (String) -> Unit
) {
    val registeredCount = uiState.courseUnits.count { it.status == UnitStatus.APPROVED || it.status == UnitStatus.DRAFT }
    val maxUnits = 7
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current

    LazyColumn(
        contentPadding = PaddingValues(
            start = Spacing.screenHorizontal,
            end = Spacing.screenHorizontal,
            bottom = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(Sizes.cardCornerRadius),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Semester Unit Quota",
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                        Text(
                            text = "$registeredCount / $maxUnits Units",
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    LinearProgressIndicator(
                        progress = { registeredCount.toFloat() / maxUnits },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = "Core units are mandatory. Select electives to complete your semester requirement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            val units = uiState.courseUnits
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                units.forEachIndexed { index, unit ->
                    val shape = ListItemDefaults.segmentedShapes(index = index, count = units.size).shape

                    val unitCardModifier = if (sharedScope != null) {
                        with(sharedScope) {
                            Modifier.fillMaxWidth().sharedBounds(
                                rememberSharedContentState(key = "unit-${unit.unitCode}"),
                                animatedVisibilityScope = animatedScope
                            )
                        }
                    } else Modifier.fillMaxWidth()
                    val attendanceIconModifier = if (sharedScope != null) {
                        with(sharedScope) {
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = "attendance-${unit.unitCode}"),
                                animatedVisibilityScope = animatedScope
                            )
                        }
                    } else Modifier

                    Surface(
                        onClick = { onOpenUnitDetail(unit.unitCode) },
                        shape = shape,
                        color = if (unit.status == UnitStatus.APPROVED) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = unitCardModifier
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Checkbox(
                                checked = unit.status == UnitStatus.APPROVED || unit.status == UnitStatus.DRAFT,
                                onCheckedChange = { onToggleUnit(unit.unitCode) }
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                ) {
                                    Text(
                                        text = unit.unitCode,
                                        style = MaterialTheme.typography.titleSmallEmphasized,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (unit.isCore) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "CORE",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = unit.unitTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "${unit.lecturerName}  •  ${unit.venueName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onOpenAttendance(unit.unitCode) },
                                shapes = IconButtonDefaults.shapes(),
                                modifier = attendanceIconModifier
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Attendance",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onSubmit,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Confirm & Submit Registration")
            }
        }
    }
}

private data class ExamServiceItemData(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit,
    val sharedElementKey: String
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ExamServicesTabContent(
    uiState: AcademicsUiState,
    onOpenExamCard: () -> Unit,
    onOpenSpecialExam: () -> Unit,
    onOpenSupplementary: () -> Unit,
    onOpenMissingMarks: () -> Unit,
    onOpenExamTimetable: () -> Unit
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val examCardModifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = "exam-card"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()

    val examServices = listOf(
        ExamServiceItemData(
            title = "University Examination Timetable",
            subtitle = "Browse all faculty exam schedules, morning/afternoon sessions, venues & invigilators",
            icon = Icons.Default.CalendarMonth,
            onClick = onOpenExamTimetable,
            sharedElementKey = "exam-timetable"
        ),
        ExamServiceItemData(
            title = "Request Special Examination",
            subtitle = "For students who missed exams due to certified medical or emergency cause (CMO Protocol)",
            icon = Icons.Default.MedicalServices,
            onClick = onOpenSpecialExam,
            sharedElementKey = "special-exam"
        ),
        ExamServiceItemData(
            title = "Supplementary Examination Registration",
            subtitle = "Register for failed units / resits (KES 1,000 per unit, capped at 40%)",
            icon = Icons.Default.Replay,
            onClick = onOpenSupplementary,
            sharedElementKey = "supplementary"
        ),
        ExamServiceItemData(
            title = "Missing Marks & CAT Dispute",
            subtitle = "Lodge an official query to your Chairman of Department & Faculty Examination Officer",
            icon = Icons.Default.ReportProblem,
            onClick = onOpenMissingMarks,
            sharedElementKey = "missing-marks"
        )
    )

    LazyColumn(
        contentPadding = PaddingValues(
            start = Spacing.screenHorizontal,
            end = Spacing.screenHorizontal,
            bottom = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.fillMaxSize()
    ) {
        // Digital Exam Card (Hero Card)
        item {
            Surface(
                onClick = onOpenExamCard,
                shape = RoundedCornerShape(Sizes.cardCornerRadius),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = examCardModifier
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Official Examination Card",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Fully Cleared  •  QR Verification Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Examination Services & Requests Section (Expressive Segmented List)
        item {
            Text(
                text = "Examination Services & Requests",
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                examServices.forEachIndexed { index, service ->
                    val shape = ListItemDefaults.segmentedShapes(index = index, count = examServices.size).shape
                    ExamServiceActionCard(
                        title = service.title,
                        subtitle = service.subtitle,
                        icon = service.icon,
                        onClick = service.onClick,
                        sharedElementKey = service.sharedElementKey,
                        shape = shape
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ExamServiceActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    sharedElementKey: String? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(Sizes.cardCornerRadiusSm)
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val cardModifier = if (sharedElementKey != null && sharedScope != null) {
        with(sharedScope) {
            Modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = sharedElementKey),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()

    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = cardModifier
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
