package com.wolfeleo2.myuon.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.data.model.UonCampuses
import com.wolfeleo2.myuon.ui.components.ExpressiveShapeBadge
import com.wolfeleo2.myuon.ui.components.LoadingOverlay
import com.wolfeleo2.myuon.ui.theme.ExpressivePolygons
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginScreenContent(
        uiState = uiState,
        onRegNoChanged = viewModel::onRegNoChanged,
        onAdEmailChanged = viewModel::onAdEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onFullNameChanged = viewModel::onFullNameChanged,
        onCampusSelected = viewModel::onCampusSelected,
        onProgramChanged = viewModel::onProgramChanged,
        onMobileNumberChanged = viewModel::onMobileNumberChanged,
        onToggleLoginMode = viewModel::toggleLoginMode,
        onToggleSignUpMode = viewModel::toggleSignUpMode,
        onSubmitClick = { viewModel.submit(onLoginSuccess) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onRegNoChanged: (String) -> Unit,
    onAdEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onCampusSelected: (String) -> Unit,
    onProgramChanged: (String) -> Unit,
    onMobileNumberChanged: (String) -> Unit,
    onToggleLoginMode: () -> Unit,
    onToggleSignUpMode: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isCampusDropdownExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // University Crest / Symbol - Expressive Upgrade
            ExpressiveShapeBadge(
                polygon = ExpressivePolygons.Sunny,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                size = 100.dp,
                icon = Icons.Default.School
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "myUON",
                style = MaterialTheme.typography.displayMediumEmphasized,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "University of Nairobi Student Companion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Auth Card
            Surface(
                shape = RoundedCornerShape(Sizes.cardCornerRadius),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = if (uiState.isSignUpMode) "Create Student Account" else "Authentication Mode",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!uiState.isSignUpMode) {
                        // Connected Button Group for Login Mode Selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                        ) {
                            ToggleButton(
                                checked = !uiState.isAdLoginMode,
                                onCheckedChange = { if (uiState.isAdLoginMode) onToggleLoginMode() },
                                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { role = Role.RadioButton }
                            ) {
                                Text("SMIS Portal", style = MaterialTheme.typography.labelMediumEmphasized)
                            }
                            ToggleButton(
                                checked = uiState.isAdLoginMode,
                                onCheckedChange = { if (!uiState.isAdLoginMode) onToggleLoginMode() },
                                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { role = Role.RadioButton }
                            ) {
                                Text("AD Account", style = MaterialTheme.typography.labelMediumEmphasized)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    if (uiState.isSignUpMode) {
                        OutlinedTextField(
                            value = uiState.fullName,
                            onValueChange = onFullNameChanged,
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    if (uiState.isSignUpMode || !uiState.isAdLoginMode) {
                        OutlinedTextField(
                            value = uiState.regNo,
                            onValueChange = onRegNoChanged,
                            label = { Text("Registration Number (e.g. P15/12345/2022)") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    if (uiState.isSignUpMode || uiState.isAdLoginMode) {
                        OutlinedTextField(
                            value = uiState.adEmail,
                            onValueChange = onAdEmailChanged,
                            label = { Text("Student Email (@students.uonbi.ac.ke)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    // Campus Dropdown for Sign Up
                    if (uiState.isSignUpMode) {
                        ExposedDropdownMenuBox(
                            expanded = isCampusDropdownExpanded,
                            onExpandedChange = { isCampusDropdownExpanded = !isCampusDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.campus,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Campus") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCampusDropdownExpanded) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = isCampusDropdownExpanded,
                                onDismissRequest = { isCampusDropdownExpanded = false }
                            ) {
                                UonCampuses.ALL.forEach { campusOption ->
                                    DropdownMenuItem(
                                        text = { Text(campusOption) },
                                        onClick = {
                                            onCampusSelected(campusOption)
                                            isCampusDropdownExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        OutlinedTextField(
                            value = uiState.program,
                            onValueChange = onProgramChanged,
                            label = { Text("Degree Program") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        OutlinedTextField(
                            value = uiState.mobileNumber,
                            onValueChange = onMobileNumberChanged,
                            label = { Text("Mobile Number (e.g. 0712345678)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChanged,
                        label = { Text("Portal Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Button(
                        onClick = onSubmitClick,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (uiState.isSignUpMode) "Register Student Account" else "Access Student Portal",
                            style = MaterialTheme.typography.labelLargeEmphasized
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    TextButton(
                        onClick = onToggleSignUpMode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (uiState.isSignUpMode) "Already registered? Sign In instead" else "New student? Create an account",
                            style = MaterialTheme.typography.labelMediumEmphasized,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "Biometric Quick Unlock Available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (uiState.isLoading) {
            LoadingOverlay(
                message = if (uiState.isSignUpMode) "Registering Student Profile..." else "Verifying with UoN SMIS Directory..."
            )
        }
    }
}

@Preview(showBackground = true, name = "Login - Registration Number Mode")
@Composable
fun LoginScreenRegistrationPreview() {
    com.wolfeleo2.myuon.ui.theme.MyUONTheme {
        LoginScreenContent(
            uiState = LoginUiState(isAdLoginMode = false),
            onRegNoChanged = {},
            onAdEmailChanged = {},
            onPasswordChanged = {},
            onFullNameChanged = {},
            onCampusSelected = {},
            onProgramChanged = {},
            onMobileNumberChanged = {},
            onToggleLoginMode = {},
            onToggleSignUpMode = {},
            onSubmitClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Sign Up - Campus Dropdown Mode")
@Composable
fun LoginScreenSignUpPreview() {
    com.wolfeleo2.myuon.ui.theme.MyUONTheme {
        LoginScreenContent(
            uiState = LoginUiState(isSignUpMode = true),
            onRegNoChanged = {},
            onAdEmailChanged = {},
            onPasswordChanged = {},
            onFullNameChanged = {},
            onCampusSelected = {},
            onProgramChanged = {},
            onMobileNumberChanged = {},
            onToggleLoginMode = {},
            onToggleSignUpMode = {},
            onSubmitClick = {}
        )
    }
}
