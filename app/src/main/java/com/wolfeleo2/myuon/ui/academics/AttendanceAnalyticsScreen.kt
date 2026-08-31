package com.wolfeleo2.myuon.ui.academics

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.data.model.AttendanceSummary
import com.wolfeleo2.myuon.data.model.AttendanceWeekRecord
import com.wolfeleo2.myuon.data.model.ClassSessionAttendance
import com.wolfeleo2.myuon.ui.components.ExpressiveShapeBadge
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.theme.ExpressivePolygons
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AttendanceAnalyticsScreen(
    unitCode: String = "CSC 311",
    attendanceSummary: AttendanceSummary? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = attendanceSummary ?: AttendanceSummary(
        unitCode = unitCode,
        unitTitle = "Course Unit Attendance",
        totalLecturesHeld = 0,
        lecturesAttended = 0,
        totalLabSessionsHeld = 0,
        labSessionsAttended = 0
    )
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val headlineModifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.sharedBounds(
                rememberSharedContentState(key = "attendance-$unitCode"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier

    val weeks = summary.weeklyBreakdown
    // weeklyBreakdown is ordered oldest-to-newest; default to the most recent week.
    var selectedWeekIndex by remember(weeks) { mutableIntStateOf((weeks.size - 1).coerceAtLeast(0)) }
    var isCumulative by remember { mutableStateOf(false) }
    var selectedSession by remember { mutableStateOf<ClassSessionAttendance?>(null) }

    val selectedWeek = weeks.getOrNull(selectedWeekIndex)

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Class Attendance Analytics",
                subtitle = "${summary.unitCode}: ${summary.unitTitle}",
                canNavigateBack = true,
                onNavigateBack = onBack,
                actions = {
                    IconButton(
                        onClick = { /* Calendar Picker */ },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.screenHorizontal,
                end = Spacing.screenHorizontal,
                top = Spacing.sm,
                bottom = innerPadding.calculateBottomPadding() + Spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Week Selector Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedWeek?.weekLabel ?: "This week",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = { selectedWeekIndex = (selectedWeekIndex - 1).coerceAtLeast(0) },
                                enabled = selectedWeekIndex > 0
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week")
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = { selectedWeekIndex = (selectedWeekIndex + 1).coerceAtMost(weeks.lastIndex.coerceAtLeast(0)) },
                                enabled = selectedWeekIndex < weeks.lastIndex
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next week")
                            }
                        }
                    }
                }
            }

            // Cumulative Toggle (two-option connected group, matching AcademicsScreen's pattern)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    ToggleButton(
                        checked = !isCumulative,
                        onCheckedChange = { isCumulative = false },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        modifier = Modifier.semantics { role = Role.RadioButton }
                    ) {
                        Text("Selected Week", style = MaterialTheme.typography.labelMediumEmphasized)
                    }
                    ToggleButton(
                        checked = isCumulative,
                        onCheckedChange = { isCumulative = true },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        modifier = Modifier.semantics { role = Role.RadioButton }
                    ) {
                        Text("Cumulative", style = MaterialTheme.typography.labelMediumEmphasized)
                    }
                }
            }

            // Big Stat Headline
            item {
                val displayedPercentage = if (isCumulative) summary.overallPercentage else (selectedWeek?.percentageAttended ?: 100.0)

                Column(modifier = headlineModifier) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Text(
                            text = String.format("%.0f%%", displayedPercentage),
                            style = MaterialTheme.typography.displayMediumEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isCumulative) "attended this semester" else "attended this week",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isCumulative) {
                            "Senate 75% Rule (semester-to-date): ${summary.lecturesAttended + summary.labSessionsAttended}/${summary.totalLecturesHeld + summary.totalLabSessionsHeld} sessions attended. " +
                                if (summary.isSenateThresholdMet) "Threshold met." else "Below threshold."
                        } else {
                            "Senate 75% Rule: this is ${selectedWeek?.weekLabel ?: "this week"} only, not the semester total."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Per-week Session Pills Chart (1-2 sessions/week, sessions-attended vs sessions-held)
            item {
                Surface(
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusLg),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExpressiveWeekSessionChart(
                        week = selectedWeek,
                        selectedSession = selectedSession,
                        onSelectSession = { selectedSession = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.lg)
                    )
                }
            }

            // Selected Session Inspection Card (if any)
            if (selectedSession != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            if (selectedSession!!.isAttended) {
                                ExpressiveShapeBadge(
                                    polygon = ExpressivePolygons.Burst,
                                    backgroundColor = Color(0xFF00875A),
                                    contentColor = Color.White,
                                    size = 40.dp,
                                    icon = Icons.Default.Check
                                )
                            } else {
                                ExpressiveShapeBadge(
                                    polygon = ExpressivePolygons.Cookie9Sided,
                                    backgroundColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                    size = 40.dp,
                                    icon = Icons.Default.Close
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${selectedSession!!.sessionType} • ${selectedSession!!.timeSlot}",
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${selectedSession!!.topicCovered} • ${selectedSession!!.venue}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Weekly Sessions List
            item {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "${selectedWeek?.weekLabel ?: "This week"}'s Sessions",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                val sessions = selectedWeek?.sessions.orEmpty()
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    sessions.forEachIndexed { index, session ->
                        val shape = when {
                            sessions.size == 1 -> RoundedCornerShape(16.dp)
                            index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                            index == sessions.lastIndex -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                            else -> RoundedCornerShape(4.dp)
                        }

                        Surface(
                            onClick = { selectedSession = session },
                            shape = shape,
                            color = if (session == selectedSession) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.md, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${session.sessionType} • ${session.timeSlot}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = session.venue,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (session.isAttended) {
                                    ExpressiveShapeBadge(
                                        polygon = ExpressivePolygons.Sunny,
                                        backgroundColor = Color(0xFF00875A),
                                        contentColor = Color.White,
                                        size = 24.dp,
                                        icon = Icons.Default.Check
                                    )
                                } else {
                                    ExpressiveShapeBadge(
                                        polygon = ExpressivePolygons.Cookie9Sided,
                                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                        size = 24.dp,
                                        icon = Icons.Default.Close
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Physics-Based Animated Session Chart.
 * Shows the 1-2 sessions scheduled for the selected week as discrete pills
 * (filled = attended, outlined = missed), against a 75% Senate reference line.
 */
@Composable
private fun ExpressiveWeekSessionChart(
    week: AttendanceWeekRecord?,
    selectedSession: ClassSessionAttendance?,
    onSelectSession: (ClassSessionAttendance) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions = week?.sessions.orEmpty()

    // Physics Animation: Animates from 0 to 1 with EaseOutBack overshoot curve
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(week) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 900,
                easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f) // Flutter Curves.easeOutBack
            )
        )
    }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier) {
        // Upper threshold legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Senate threshold: 75%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chart Container with Dashed 75% Reference Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .drawBehind {
                    // Draw 75% reference dashed line
                    val y75 = size.height * (1f - 0.75f)
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, y75),
                        end = Offset(size.width, y75),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw baseline
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                sessions.forEach { session ->
                    val isSelected = session == selectedSession
                    // Full pill for attended, a token "missed" sliver for absent - discrete,
                    // not a continuous height-per-hours bar.
                    val heightFraction = if (session.isAttended) 1f else 0.1f
                    val animatedHeightFraction = (heightFraction * animationProgress.value).coerceIn(0f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectSession(session) }
                    ) {
                        if (session.isAttended) {
                            ExpressiveShapeBadge(
                                polygon = ExpressivePolygons.Sunny,
                                backgroundColor = Color(0xFF00875A),
                                contentColor = Color.White,
                                size = 26.dp,
                                icon = Icons.Default.Check
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Pill-Shaped Vertical Bar
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .fillMaxHeight(animatedHeightFraction.coerceAtLeast(0.02f))
                                .clip(RoundedCornerShape(percent = 50)) // Pill Shape
                                .background(
                                    when {
                                        session.isAttended && isSelected -> Color(0xFF00875A)
                                        session.isAttended -> Color(0xFF00875A).copy(alpha = 0.85f)
                                        isSelected -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                    }
                                )
                        )
                    }
                }

                if (sessions.isEmpty()) {
                    Text(
                        text = "No sessions scheduled this week.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Session Type Labels below each pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            sessions.forEach { session ->
                val isSelected = session == selectedSession
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = session.sessionType,
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
