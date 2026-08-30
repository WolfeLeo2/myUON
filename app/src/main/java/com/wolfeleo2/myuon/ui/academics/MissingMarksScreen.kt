package com.wolfeleo2.myuon.ui.academics

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MissingMarksScreen(
    viewModel: AcademicsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val heroModifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = "missing-marks"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()
    var unitCode by remember { mutableStateOf("CSC 321") }
    var unitTitle by remember { mutableStateOf("Artificial Intelligence Principles") }
    var lecturer by remember { mutableStateOf("Dr. Peter M. Mwangi") }
    var component by remember { mutableStateOf("CAT 2 Score Missing") }
    var note by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var submitFailed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Missing Marks Dispute",
                subtitle = "Direct Departmental Query Escalation",
                canNavigateBack = true,
                onNavigateBack = onBack
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
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Surface(
                shape = RoundedCornerShape(Sizes.cardCornerRadius),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = heroModifier
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        text = "Departmental Verification Ticket",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "Dispute tickets are routed immediately to the Lecturer and the Chairman of Department for mark script retrieval.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = unitCode,
                onValueChange = { unitCode = it },
                label = { Text("Course Unit Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = unitTitle,
                onValueChange = { unitTitle = it },
                label = { Text("Course Unit Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lecturer,
                onValueChange = { lecturer = it },
                label = { Text("Course Lecturer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = component,
                onValueChange = { component = it },
                label = { Text("Missing Component (e.g. CAT 1, CAT 2, Final Exam)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Additional Information (e.g. Date script was sat/submitted)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val success = viewModel.submitMissingMarks(unitCode, unitTitle, lecturer, component, note)
                    isSubmitted = success
                    submitFailed = !success
                },
                enabled = note.isNotBlank() && !isSubmitted,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(if (isSubmitted) "Dispute Ticket Lodged" else "Dispatch Query to COD")
            }

            if (isSubmitted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Ticket lodged with status: UNDER_CHAIRPERSON_REVIEW. You will receive an alert once the grade sheet is revised.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            } else if (submitFailed && uiState.registrationMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✗ ${uiState.registrationMessage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            }
        }
    }
}
