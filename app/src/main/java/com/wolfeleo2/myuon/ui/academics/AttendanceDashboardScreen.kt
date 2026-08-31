package com.wolfeleo2.myuon.ui.academics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.data.model.AttendanceSummary
import com.wolfeleo2.myuon.data.model.StudentProfile
import com.wolfeleo2.myuon.data.repo.AcademicRepository
import com.wolfeleo2.myuon.data.repo.AuthRepository
import com.wolfeleo2.myuon.ui.components.ExpressiveShapeBadge
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.PullToRefreshBox
import com.wolfeleo2.myuon.ui.theme.ExpressivePolygons
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AttendanceDashboardScreen(
    academicRepository: AcademicRepository,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onOpenUnitAttendance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val overview by academicRepository.attendanceOverview.collectAsState()
    val studentProfile by authRepository.studentProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val totalLectures = overview.sumOf { it.totalLecturesHeld }
    val attendedLectures = overview.sumOf { it.lecturesAttended }
    val overallPercentage = if (totalLectures > 0) (attendedLectures.toFloat() / totalLectures) * 100f else 91.5f
    val isCompliant = overallPercentage >= 75f

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Attendance Dashboard",
                subtitle = "${studentProfile?.regNo ?: ""} • Year 3 Sem 2",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                val regNo = studentProfile?.regNo
                if (!regNo.isNullOrBlank()) {
                    coroutineScope.launch {
                        isRefreshing = true
                        academicRepository.refreshFromRemote(regNo)
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Spacing.screenHorizontal,
                    end = Spacing.screenHorizontal,
                    top = Spacing.sm,
                    bottom = Spacing.xl
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxSize()
            ) {
                // Overall Semester Hero Attendance Card
                item {
                    AttendanceHeroCard(
                        overallPercentage = overallPercentage,
                        attendedSessions = attendedLectures,
                        totalSessions = totalLectures,
                        isCompliant = isCompliant
                    )
                }

                // Section Header
                item {
                    Text(
                        text = "Enrolled Units Attendance",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // List of enrolled units
                if (overview.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(Sizes.cardCornerRadius),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.xl),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No attendance sessions logged yet for this semester.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(overview, key = { it.unitCode }) { summary ->
                        AttendanceUnitRow(
                            summary = summary,
                            onClick = { onOpenUnitAttendance(summary.unitCode) }
                        )
                    }
                }

                // Senate Rule Note
                item {
                    SenateRuleNote()
                }
            }
        }
    }
}

@Composable
private fun AttendanceHeroCard(
    overallPercentage: Float,
    attendedSessions: Int,
    totalSessions: Int,
    isCompliant: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(Sizes.cardCornerRadiusLg),
        color = if (isCompliant) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SEMESTER COMPLIANCE",
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        color = if (isCompliant) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isCompliant) "Exam Eligible" else "Debarred Status",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompliant) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCompliant) Color(0xFF00875A) else Color(0xFFDE350B),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompliant) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isCompliant) "75% MET" else "< 75% FAIL",
                            style = MaterialTheme.typography.labelMediumEmphasized
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                ExpressiveShapeBadge(
                    polygon = ExpressivePolygons.Burst,
                    backgroundColor = if (isCompliant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    contentColor = if (isCompliant) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
                    size = 56.dp,
                    icon = Icons.Default.BarChart
                )

                Column {
                    Text(
                        text = String.format("%.1f%%", overallPercentage),
                        style = MaterialTheme.typography.displaySmallEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompliant) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "$attendedSessions of $totalSessions total sessions attended",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompliant) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            LinearProgressIndicator(
                progress = { (overallPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = if (isCompliant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                trackColor = (if (isCompliant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error).copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun SenateRuleNote(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Senate Regulation 4.2",
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "A candidate must attend at least 75% of prescribed lectures and practical courses to be admitted to university examinations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AttendanceUnitRow(
    summary: AttendanceSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = summary.totalLecturesHeld + summary.totalLabSessionsHeld
    val attended = summary.lecturesAttended + summary.labSessionsAttended
    val pct = if (total > 0) (attended.toFloat() / total) * 100f else 100f
    val compliant = pct >= 75f

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Sizes.cardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = summary.unitCode,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• $attended/$total Sessions",
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = summary.unitTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                LinearProgressIndicator(
                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (compliant) Color(0xFF00875A) else Color(0xFFDE350B),
                    trackColor = (if (compliant) Color(0xFF00875A) else Color(0xFFDE350B)).copy(alpha = 0.15f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = String.format("%.0f%%", pct),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = if (compliant) Color(0xFF00875A) else Color(0xFFDE350B)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
