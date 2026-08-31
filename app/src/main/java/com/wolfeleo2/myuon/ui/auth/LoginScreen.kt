package com.wolfeleo2.myuon.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    val focusManager = LocalFocusManager.current

    // Shared IME config for every field that isn't the last one.
    val nextFieldKeyboard = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Crest reacts to the form's mode — the header is part of the state, not decoration.
            AnimatedContent(
                targetState = uiState.isSignUpMode,
                transitionSpec = {
                    (fadeIn(tween(260)) + scaleIn(tween(300), initialScale = 0.85f)) togetherWith
                            (fadeOut(tween(180)) + scaleOut(tween(220), targetScale = 0.85f))
                },
                label = "crest"
            ) { signUp ->
                ExpressiveShapeBadge(
                    polygon = if (signUp) ExpressivePolygons.Clover4Leaf else ExpressivePolygons.Sunny,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    size = 100.dp,
                    icon = if (signUp) Icons.Default.PersonAdd else Icons.Default.School,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
            }

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
                    AnimatedContent(
                        targetState = uiState.isSignUpMode,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(140))
                        },
                        label = "cardTitle"
                    ) { signUp ->
                        Text(
                            text = if (signUp) "Create Student Account" else "Authentication Mode",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Login-mode switch — only relevant when signing in.
                    AnimatedVisibility(
                        visible = !uiState.isSignUpMode,
                        enter = fadeIn(tween(220)) + expandVertically(tween(280)),
                        exit = fadeOut(tween(160)) + shrinkVertically(tween(240))
                    ) {
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
                    }

                    AnimatedVisibility(
                        visible = uiState.isSignUpMode,
                        enter = fadeIn(tween(220)) + expandVertically(tween(280)),
                        exit = fadeOut(tween(160)) + shrinkVertically(tween(240))
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    // Form Content
                    Column(
                        modifier = Modifier.padding(top = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        // Registration number — always visible in sign-up, or in SMIS sign-in.
                        AnimatedVisibility(
                            visible = uiState.isSignUpMode || !uiState.isAdLoginMode,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            OutlinedTextField(
                                value = uiState.regNo,
                                onValueChange = onRegNoChanged,
                                label = { Text("Registration Number") },
                                placeholder = { Text("e.g. C01/12345/2026") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "ID Badge") },
                                isError = uiState.regNoError != null,
                                supportingText = uiState.regNoError?.let { { Text(it) } },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = nextFieldKeyboard,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Student email — always visible in sign-up, or in AD sign-in.
                        AnimatedVisibility(
                            visible = uiState.isSignUpMode || uiState.isAdLoginMode,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            OutlinedTextField(
                                value = uiState.adEmail,
                                onValueChange = onAdEmailChanged,
                                label = { Text("Student Email") },
                                placeholder = { Text("username@students.uonbi.ac.ke") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                                isError = uiState.adEmailError != null,
                                supportingText = uiState.adEmailError?.let { { Text(it) } },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = nextFieldKeyboard,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Sign-Up Specific Fields
                        AnimatedVisibility(
                            visible = uiState.isSignUpMode,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                OutlinedTextField(
                                    value = uiState.fullName,
                                    onValueChange = onFullNameChanged,
                                    label = { Text("Full Name") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                                    isError = uiState.fullNameError != null,
                                    supportingText = uiState.fullNameError?.let { { Text(it) } },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = nextFieldKeyboard,
                                    modifier = Modifier.fillMaxWidth()
                                )

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
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location") },
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

                                OutlinedTextField(
                                    value = uiState.program,
                                    onValueChange = onProgramChanged,
                                    label = { Text("Degree Program") },
                                    leadingIcon = { Icon(Icons.Default.School, contentDescription = "Education") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = nextFieldKeyboard,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = uiState.mobileNumber,
                                    onValueChange = onMobileNumberChanged,
                                    label = { Text("Mobile Number") },
                                    placeholder = { Text("e.g. 0712345678") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                                    isError = uiState.mobileNumberError != null,
                                    supportingText = uiState.mobileNumberError?.let { { Text(it) } },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = nextFieldKeyboard,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = onPasswordChanged,
                            label = { Text("Portal Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible },
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            isError = uiState.passwordError != null,
                            supportingText = uiState.passwordError?.let { { Text(it) } },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    onSubmitClick()
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Form-level failures (bad credentials, network) — field-specific
                    // problems now surface inline on the field itself.
                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = fadeIn(tween(200)) + expandVertically(tween(240)),
                        exit = fadeOut(tween(140)) + shrinkVertically(tween(200))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Button(
                        onClick = onSubmitClick,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        AnimatedContent(
                            targetState = uiState.isSignUpMode,
                            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(140)) },
                            label = "submitLabel"
                        ) { signUp ->
                            Text(
                                text = if (signUp) "Register Student Account" else "Access Student Portal",
                                style = MaterialTheme.typography.labelLargeEmphasized
                            )
                        }
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