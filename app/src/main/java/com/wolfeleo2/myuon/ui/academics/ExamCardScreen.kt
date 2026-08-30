package com.wolfeleo2.myuon.ui.academics

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.components.QrCodeCard
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ExamCardScreen(
    viewModel: AcademicsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val examCard = uiState.examCard
    val student = uiState.student

    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val heroModifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = "exam-card"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Examination Card",
                subtitle = "Official Examination Authorization Pass",
                canNavigateBack = true,
                onNavigateBack = onBack,
                actions = {
                    IconButton(
                        onClick = { /* Print/Share Intent */ },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Official Exam Card Pass
            Surface(
                shape = RoundedCornerShape(Sizes.cardCornerRadiusLg),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = heroModifier
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with UoN Seal & Authorization
                    Text(
                        text = "UNIVERSITY OF NAIROBI",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "OFFICIAL EXAMINATION PASS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Academic Year ${examCard?.academicYear ?: "2025/2026"} (Semester ${examCard?.semester ?: 2})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // QR Code Card
                    QrCodeCard(
                        token = examCard?.qrVerificationToken ?: "UON:F16/28914/2022:SEM2:CLEARED:AUTH",
                        modifier = Modifier.padding(Spacing.sm)
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Student Details Matrix
                    Surface(
                        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            ExamCardRow("Student Name", student?.fullName ?: "LEO WOLFE")
                            ExamCardRow("Registration No", student?.regNo ?: "F16/28914/2022")
                            ExamCardRow("Faculty", student?.faculty ?: "Faculty of Science & Technology")
                            ExamCardRow("Campus", student?.campus ?: "Chiromo Campus")
                            ExamCardRow("Fee Status", if (examCard?.isFeeCleared == true) "100% Cleared (Verified)" else "Pending", isSuccess = examCard?.isFeeCleared == true)
                            ExamCardRow("Units Registered", "${examCard?.units?.size ?: 6} Units Approved")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Registered Units Schedule on Card
            Text(
                text = "Authorized Units & Exam Timetable",
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            examCard?.units?.forEach { unit ->
                Surface(
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${unit.unitCode}: ${unit.unitTitle}",
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = unit.examDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = unit.examTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = unit.venue,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Cleared",
                            tint = Color(0xFF00875A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = { /* Export PDF */ },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Print / Export Official Exam Card")
            }
        }
    }
}

@Composable
private fun ExamCardRow(
    label: String,
    value: String,
    isSuccess: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isSuccess) Color(0xFF00875A) else MaterialTheme.colorScheme.onSurface
        )
    }
}
