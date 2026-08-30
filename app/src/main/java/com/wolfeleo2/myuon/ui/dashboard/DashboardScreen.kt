package com.wolfeleo2.myuon.ui.dashboard

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.data.util.daysUntil
import com.wolfeleo2.myuon.data.util.relativeDayLabel
import com.wolfeleo2.myuon.ui.components.DashboardSkeleton
import com.wolfeleo2.myuon.ui.components.ExpressiveShapeBadge
import com.wolfeleo2.myuon.ui.components.ExpressiveStatCard
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.components.PullToRefreshBox
import com.wolfeleo2.myuon.ui.components.TodayClassCard
import com.wolfeleo2.myuon.ui.theme.ExpressivePolygons
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenExamCard: () -> Unit,
    onOpenAcademics: () -> Unit,
    onOpenFees: () -> Unit,
    onOpenTimetable: () -> Unit,
    onOpenHostels: () -> Unit,
    onOpenUnitDetail: (String) -> Unit,
    onOpenVenueMap: (String) -> Unit,
    onOpenExamTimetable: () -> Unit = {},
    onOpenAttendance: () -> Unit = {},
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.student

    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "myUON",
                subtitle = "University of Nairobi Student Portal",
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick
            )
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isSyncing,
            onRefresh = viewModel::refreshPortalData,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (student == null) {
                DashboardSkeleton(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = Spacing.screenHorizontal,
                        end = Spacing.screenHorizontal,
                        top = Spacing.sm,
                        bottom = Spacing.xxl
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // University of Nairobi Digital Student Smart ID Hero Banner
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Top UoN Header Stripe
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = Spacing.md, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                        ) {
                                            ExpressiveShapeBadge(
                                                polygon = ExpressivePolygons.Sunny,
                                                backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                                size = 24.dp,
                                                icon = Icons.Default.School
                                            )
                                            Column {
                                                Text(
                                                    text = "UNIVERSITY OF NAIROBI",
                                                    style = MaterialTheme.typography.labelMediumEmphasized,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Text(
                                                    text = "STUDENT IDENTIFICATION PASS",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF00875A),
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmallEmphasized,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // ID Card Body (Photo Avatar + Credentials)
                                Column(modifier = Modifier.padding(Spacing.md)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                                    ) {
                                        // Left: Photo framed in Cookie9Sided with Verified Chip
                                        Box(contentAlignment = Alignment.BottomEnd) {
                                            ExpressiveShapeBadge(
                                                polygon = ExpressivePolygons.Cookie9Sided,
                                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.primary,
                                                size = 56.dp,
                                                text = student.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF00875A),
                                                contentColor = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Check, contentDescription = "Verified", modifier = Modifier.size(11.dp))
                                                }
                                            }
                                        }

                                        // Right: Student Details
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = student.fullName,
                                                style = MaterialTheme.typography.titleMediumEmphasized,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = student.regNo,
                                                style = MaterialTheme.typography.labelMediumEmphasized,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = student.program,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Year ${student.yearOfStudy} • Semester ${student.semester}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(Spacing.sm))

                                    // Bottom ID Metadata Card (Clean 2-line layout that never overflows)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "${student.campus} • ${student.faculty}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Valid: 2022 — 2026",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Smart Chip ID: UON-NFC-${student.regNo.takeLast(4)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quick Actions Row
                    item {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            ExpressiveQuickActionButton(
                                label = "Exam Card",
                                icon = Icons.Default.Badge,
                                onClick = onOpenExamCard,
                                modifier = Modifier.weight(1f)
                            )
                            ExpressiveQuickActionButton(
                                label = "Attendance",
                                icon = Icons.Default.BarChart,
                                onClick = onOpenAttendance,
                                modifier = Modifier.weight(1f)
                            )
                            ExpressiveQuickActionButton(
                                label = "Fee Balance",
                                icon = Icons.Default.AccountBalanceWallet,
                                onClick = onOpenFees,
                                modifier = Modifier.weight(1f)
                            )
                            ExpressiveQuickActionButton(
                                label = "Hostel",
                                icon = Icons.Default.Hotel,
                                onClick = onOpenHostels,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Upcoming Exam Banner
                    item {
                        val nextExam = uiState.upcomingExam
                        if (nextExam != null) {
                            Surface(
                                shape = RoundedCornerShape(Sizes.cardCornerRadius),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(Spacing.md)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                        ) {
                                            ExpressiveShapeBadge(
                                                polygon = ExpressivePolygons.Sunny,
                                                backgroundColor = MaterialTheme.colorScheme.tertiary,
                                                contentColor = MaterialTheme.colorScheme.onTertiary,
                                                size = 32.dp,
                                                icon = Icons.Default.Event
                                            )
                                            Text(
                                                text = "Next University Examination",
                                                style = MaterialTheme.typography.labelLargeEmphasized,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.tertiary,
                                                contentColor = MaterialTheme.colorScheme.onTertiary
                                            ) {
                                                Text(
                                                    text = nextExam.examDate,
                                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            val relativeLabel = relativeDayLabel(daysUntil(nextExam.examDate))
                                            if (relativeLabel.isNotEmpty()) {
                                                Text(
                                                    text = relativeLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(Spacing.xs))

                                    // Main Title in Bold
                                    Text(
                                        text = "${nextExam.unitCode}: ${nextExam.unitTitle}",
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )

                                    Spacer(modifier = Modifier.height(Spacing.xs))

                                    // Time on Line 1 with Proper Clock Icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = nextExam.examTime,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Location / Venue on its Own Line Below Time with Proper Location Icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = nextExam.venue,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(Spacing.sm))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        val examCardModifier = if (sharedScope != null) {
                                            with(sharedScope) {
                                                Modifier.sharedBounds(
                                                    rememberSharedContentState(key = "exam-card"),
                                                    animatedVisibilityScope = animatedScope
                                                )
                                            }
                                        } else Modifier
                                        Button(
                                            onClick = onOpenExamCard,
                                            shapes = ButtonDefaults.shapes(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiary,
                                                contentColor = MaterialTheme.colorScheme.onTertiary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = examCardModifier
                                        ) {
                                            Text("Exam Card")
                                        }

                                        val examTimetableModifier = if (sharedScope != null) {
                                            with(sharedScope) {
                                                Modifier.sharedBounds(
                                                    rememberSharedContentState(key = "exam-timetable"),
                                                    animatedVisibilityScope = animatedScope
                                                )
                                            }
                                        } else Modifier
                                        OutlinedButton(
                                            onClick = onOpenExamTimetable,
                                            shapes = ButtonDefaults.shapes(),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = examTimetableModifier
                                        ) {
                                            Text("Exam Timetable")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Academic & Financial Metrics Grid
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            ExpressiveStatCard(
                                title = "Cumulative Average",
                                value = "${"%.1f".format(uiState.academicSummary.cumulativeAverage)}%",
                                subtitle = uiState.academicSummary.degreeClass.label,
                                badgeText = "On Track",
                                badgeColor = Color(0xFF00875A),
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                onClick = onOpenAcademics,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )

                            val balance = uiState.feeStatement?.outstandingBalance ?: 0.0
                            ExpressiveStatCard(
                                title = "Fee Balance",
                                value = if (balance <= 0) "KES 0" else "KES ${balance.toInt()}",
                                subtitle = if (balance <= 0) "100% Cleared" else "Pending Payment",
                                badgeText = if (balance <= 0) "CLEARED" else "DUE",
                                badgeColor = if (balance <= 0) Color(0xFF00875A) else Color(0xFFDE350B),
                                icon = Icons.Default.AccountBalance,
                                onClick = onOpenFees,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }

                    // Today's Timetable Section
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Today's Schedule",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = onOpenTimetable,
                                shapes = ButtonDefaults.shapes()
                            ) {
                                Text("Full Timetable")
                            }
                        }
                    }

                    items(uiState.todayClasses) { slot ->
                        TodayClassCard(
                            slot = slot,
                            onClick = { onOpenUnitDetail(slot.unitCode) },
                            onOpenVenueMap = { onOpenVenueMap(slot.venue) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveQuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmallEmphasized,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
