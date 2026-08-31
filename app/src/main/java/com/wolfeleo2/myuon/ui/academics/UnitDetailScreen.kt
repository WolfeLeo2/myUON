package com.wolfeleo2.myuon.ui.academics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.data.model.CourseUnit
import com.wolfeleo2.myuon.data.model.SyllabusTopic
import com.wolfeleo2.myuon.data.model.UnitStatus
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.ui.components.ExpressiveShapeBadge
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.theme.ExpressivePolygons
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun UnitDetailScreen(
    unitCode: String,
    viewModel: AcademicsViewModel,
    onOpenVenueMap: (String) -> Unit,
    onOpenSpecialExam: (String) -> Unit,
    onOpenAttendance: (String) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(unitCode) {
        viewModel.loadUnitDetail(unitCode)
    }
    val unit = uiState.courseUnits.find { it.unitCode.equals(unitCode, ignoreCase = true) }
        ?: CourseUnit(
            unitCode = unitCode,
            unitTitle = "Course Unit Outline",
            credits = 3,
            lecturerName = "Department Faculty",
            lecturerEmail = "cs@uonbi.ac.ke",
            venueName = "Chiromo Campus",
            campus = "Chiromo Science Campus"
        )

    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val heroModifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = "unit-$unitCode"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = unit.unitCode,
                subtitle = unit.unitTitle,
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.screenHorizontal,
                end = Spacing.screenHorizontal,
                top = Spacing.sm,
                bottom = Spacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Unit Hero Banner
            item {
                Surface(
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusLg),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = heroModifier
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(
                                    text = if (unit.isCore) "CORE UNIT" else "ELECTIVE",
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "45 Contact Hours",
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        Text(
                            text = unit.unitTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        Text(
                            text = "Academic Year ${unit.academicYear} • Semester ${unit.semester}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // 1. SYLLABUS OVERVIEW & COURSE TOPICS (COMES FIRST)
            item {
                Text(
                    text = "Syllabus & Course Modules",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Course Description (with Empty State fallback)
            item {
                Surface(
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = "Course Description",
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (unit.description.isNotBlank()) unit.description else "No content published yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (unit.description.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Weekly Syllabus Topics Breakdown (with Empty State)
            if (unit.syllabusTopics.isNotEmpty()) {
                item {
                    Text(
                        text = "Weekly Lecture & Practical Modules",
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(unit.syllabusTopics.size) { index ->
                    val topic = unit.syllabusTopics[index]
                    SyllabusTopicCard(topic = topic)
                }
            } else {
                item {
                    Surface(
                        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Weekly lecture modules and detailed syllabus topics have not been published yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Learning Outcomes
            if (unit.learningOutcomes.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                text = "Intended Learning Outcomes",
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            unit.learningOutcomes.forEach { outcome ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF00875A),
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Text(
                                        text = outcome,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recommended Textbooks
            if (unit.recommendedTextbooks.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Recommended Academic Texts",
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            unit.recommendedTextbooks.forEach { book ->
                                Text(
                                    text = "•  $book",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. LECTURE HALL & SCHEDULE
            item {
                Text(
                    text = "Venue & Lecture Schedule",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(Sizes.cardCornerRadius),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        // Venue line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExpressiveShapeBadge(
                                polygon = ExpressivePolygons.Sunny,
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                                size = 36.dp,
                                icon = Icons.Default.LocationOn
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = unit.venueName,
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = unit.campus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // Time schedule line
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Weekly Lecture: ${unit.scheduleTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // Dedicated directions button
                        Button(
                            onClick = { onOpenVenueMap(unit.venueName) },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Find Venue Directions on Campus Map")
                        }
                    }
                }
            }

            // 3. ATTENDANCE ANALYTICS ACTION CARD
            item {
                Surface(
                    onClick = { onOpenAttendance(unit.unitCode) },
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        ExpressiveShapeBadge(
                            polygon = ExpressivePolygons.Burst,
                            backgroundColor = Color(0xFF00875A),
                            contentColor = Color.White,
                            size = 40.dp,
                            icon = Icons.Default.BarChart
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Class Attendance Analytics",
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "92.8% Attended • Senate 75% Examination Threshold Met",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00875A),
                                fontWeight = FontWeight.SemiBold
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

            // 4. INSTRUCTOR & CONSULTATION HOURS
            item {
                Surface(
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
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
                            size = 44.dp,
                            icon = Icons.Default.Person
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = unit.lecturerName,
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = unit.lecturerEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${unit.lecturerOffice} • Consultation: Mon & Wed 14:00 - 16:00",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 5. ASSESSMENT WEIGHTING & PASS CRITERIA
            item {
                Surface(
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = "Assessment Weighting & Grading Scheme",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            AssessmentComponentCard(
                                title = "CATs & Practicals",
                                weight = "30%",
                                description = "2 Continuous Assessment Tests & Lab Reports",
                                modifier = Modifier.weight(1f)
                            )
                            AssessmentComponentCard(
                                title = "Final Examination",
                                weight = "70%",
                                description = "End of Semester 3-Hour Written Examination",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "University Minimum Pass Mark: 40% (Supplementary grade cap: 40%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6. QUICK ACTIONS FOOTER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { onOpenSpecialExam(unit.unitCode) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Special Exam")
                    }

                    FilledTonalButton(
                        onClick = { /* Launch eClass Moodle */ },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("eClass Portal")
                    }
                }
            }
        }
    }
}

@Composable
private fun SyllabusTopicCard(topic: SyllabusTopic) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { isExpanded = !isExpanded },
        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
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
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            text = "WK ${topic.weekNumber}",
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = Spacing.sm, start = 36.dp)) {
                    topic.subtopics.forEach { sub ->
                        Text(
                            text = "•  $sub",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentComponentCard(
    title: String,
    weight: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = weight,
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
