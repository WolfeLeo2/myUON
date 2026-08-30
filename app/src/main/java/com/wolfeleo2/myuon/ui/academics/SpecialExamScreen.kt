package com.wolfeleo2.myuon.ui.academics

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
fun SpecialExamScreen(
    viewModel: AcademicsViewModel,
    prefillUnitCode: String = "",
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val heroModifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = "special-exam"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()
    var selectedUnitCode by remember { mutableStateOf(if (prefillUnitCode.isNotBlank()) prefillUnitCode else "CSC 315") }
    var unitTitle by remember { mutableStateOf("Operating Systems Principles") }
    var reasonCategory by remember { mutableStateOf("Medical Grounds (Certified by CMO)") }
    var explanation by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var submitFailed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Special Examination",
                subtitle = "Medical & Emergency Senate Deferral",
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
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = heroModifier
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        text = "UoN Special Exam Regulations",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "Special examinations are granted solely on grounds of severe illness certified by the University Chief Medical Officer (CMO) or verifiable compassionate grounds. Passes are NOT capped.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            OutlinedTextField(
                value = selectedUnitCode,
                onValueChange = { selectedUnitCode = it },
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
                value = reasonCategory,
                onValueChange = { reasonCategory = it },
                label = { Text("Reason Category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                label = { Text("Detailed Explanation & Hospital Reference") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val success = viewModel.submitSpecialExam(selectedUnitCode, unitTitle, reasonCategory, explanation)
                    isSubmitted = success
                    submitFailed = !success
                },
                enabled = explanation.isNotBlank() && !isSubmitted,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(if (isSubmitted) "Request Lodged with COD" else "Submit Petition to Dean")
            }

            if (isSubmitted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Your special exam petition has been registered and forwarded to the Dean and CMO for review.",
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
