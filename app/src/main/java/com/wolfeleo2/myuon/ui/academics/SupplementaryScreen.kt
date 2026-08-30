package com.wolfeleo2.myuon.ui.academics

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SupplementaryScreen(
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
                rememberSharedContentState(key = "supplementary"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()
    var unitCode by remember { mutableStateOf("CSC 224") }
    var unitTitle by remember { mutableStateOf("Data Communication & Networks") }
    var previousScore by remember { mutableStateOf("34.0") }
    var paymentRef by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var submitFailed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Supplementary Exams",
                subtitle = "Resit Registration & Fee Assessment",
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
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = heroModifier
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        text = "Supplementary Exam Policy",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "1. Fee is KES 1,000 per unit paper.\n2. In accordance with UoN Examination Regulations, the maximum final score recorded on the academic transcript for any supplementary pass is capped at 40% (Pass).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            OutlinedTextField(
                value = unitCode,
                onValueChange = { unitCode = it },
                label = { Text("Failed Unit Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = unitTitle,
                onValueChange = { unitTitle = it },
                label = { Text("Unit Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = previousScore,
                onValueChange = { previousScore = it },
                label = { Text("Previous Score (%)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = paymentRef,
                onValueChange = { paymentRef = it },
                label = { Text("eCitizen / M-Pesa Ref (KES 1,000)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val score = previousScore.toDoubleOrNull() ?: 0.0
                    val success = viewModel.submitSupplementary(unitCode, unitTitle, score, paymentRef)
                    isSubmitted = success
                    submitFailed = !success
                },
                enabled = paymentRef.isNotBlank() && !isSubmitted,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(if (isSubmitted) "Supplementary Registered" else "Register Resit (KES 1,000)")
            }

            if (isSubmitted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ ${uiState.registrationMessage ?: "Supplementary exam registered."}",
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
