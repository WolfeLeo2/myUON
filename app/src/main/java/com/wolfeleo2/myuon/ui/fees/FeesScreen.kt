package com.wolfeleo2.myuon.ui.fees

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.data.model.FeeTransaction
import com.wolfeleo2.myuon.data.model.TransactionType
import com.wolfeleo2.myuon.ui.components.ExpressiveShapeBadge
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.FeesSkeleton
import com.wolfeleo2.myuon.ui.components.PullToRefreshBox
import com.wolfeleo2.myuon.ui.components.ReceiptPreviewSheet
import com.wolfeleo2.myuon.ui.theme.ExpressivePolygons
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    viewModel: FeesViewModel,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val statement = uiState.feeStatement
    val student = uiState.student

    var showPaymentSheet by remember { mutableStateOf(false) }
    var showMpesaDialog by remember { mutableStateOf(false) }
    var showEcitizenDialog by remember { mutableStateOf(false) }
    var showBankDialog by remember { mutableStateOf(false) }
    var selectedTransactionForReceipt by remember { mutableStateOf<FeeTransaction?>(null) }

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Fees & Financials",
                subtitle = "Invoices, Payments & HELB/HEF Statements",
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPaymentSheet = true },
                icon = { Icon(Icons.Default.Payment, contentDescription = null) },
                text = { Text("Pay Fees", style = MaterialTheme.typography.labelLargeEmphasized) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshData,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (statement == null) {
                FeesSkeleton(
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
                    // Payment result banner (success or failure of last M-Pesa/eCitizen/bank submission)
                    if (uiState.paymentSuccessMessage != null) {
                        item {
                            val failed = uiState.paymentSuccessMessage!!.contains("exceeds") ||
                                uiState.paymentSuccessMessage!!.contains("could not")
                            Surface(
                                onClick = viewModel::clearMessage,
                                shape = RoundedCornerShape(12.dp),
                                color = if (failed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${if (failed) "✗" else "✓"} ${uiState.paymentSuccessMessage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (failed) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(Spacing.md)
                                )
                            }
                        }
                    }

                    // Official Material 3 Expressive Connected Button Group (Semester vs Academic Year)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                        ) {
                            val modes = listOf(
                                FeeScopeMode.SEMESTER to "Semester Scope",
                                FeeScopeMode.ACADEMIC_YEAR to "Full Academic Year"
                            )
                            modes.forEachIndexed { index, (mode, label) ->
                                ToggleButton(
                                    checked = uiState.scopeMode == mode,
                                    onCheckedChange = { viewModel.setScopeMode(mode) },
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { role = Role.RadioButton }
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelMediumEmphasized)
                                }
                            }
                        }
                    }

                    // Hero Fee Summary Card
                    item {
                        val balance = statement.outstandingBalance
                        Surface(
                            shape = RoundedCornerShape(Sizes.cardCornerRadiusLg),
                            color = if (balance <= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(Spacing.lg)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "OUTSTANDING BALANCE",
                                        style = MaterialTheme.typography.labelMediumEmphasized,
                                        color = if (balance <= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (balance <= 0) Color(0xFF00875A) else Color(0xFFDE350B),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (balance <= 0) "100% CLEARED" else "PAYMENT DUE",
                                            style = MaterialTheme.typography.labelSmallEmphasized,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(Spacing.xs))

                                Text(
                                    text = "KES ${balance.toInt()}",
                                    style = MaterialTheme.typography.displaySmallEmphasized,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balance <= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )

                                Spacer(modifier = Modifier.height(Spacing.sm))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Invoiced",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (balance <= 0) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "KES ${statement.totalInvoiced.toInt()}",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Total Paid / Disbursed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (balance <= 0) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "KES ${statement.totalPaid.toInt()}",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF00875A)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Payment Channels Guide Banner (Absa Bank, M-Pesa, eCitizen)
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
                                    polygon = ExpressivePolygons.Sunny,
                                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    size = 42.dp,
                                    icon = Icons.Default.AccountBalance
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Official Payment Channels",
                                        style = MaterialTheme.typography.titleSmallEmphasized,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "M-Pesa 300059  •  eCitizen  •  Absa A/C 2032770838",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Semester Invoice Breakdown Section (Segmented List)
                    item {
                        Text(
                            text = "Semester Invoice Breakdown",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    item {
                        val invoiceList = statement.invoiceBreakdown.toList()
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            invoiceList.forEachIndexed { index, (item, amount) ->
                                val shape = when {
                                    invoiceList.size == 1 -> RoundedCornerShape(16.dp)
                                    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                    index == invoiceList.lastIndex -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                                    else -> RoundedCornerShape(4.dp)
                                }
                                Surface(
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = Spacing.md, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "KES ${amount.toInt()}",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Payment History & Ledger Section
                    item {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Payment Transactions & Receipts (Tap to View)",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(statement.transactions) { tx ->
                        TransactionItemRow(
                            tx = tx,
                            onClick = { selectedTransactionForReceipt = tx }
                        )
                    }
                }
            }
        }

        // Modal Payment Options Sheet (Opened by Extended FAB)
        if (showPaymentSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPaymentSheet = false },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .navigationBarsPadding()
                        .padding(bottom = Spacing.lg)
                ) {
                    Text(
                        text = "Pay University Fees",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose your preferred verified payment channel:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Channel 1: M-Pesa Express
                    Surface(
                        onClick = {
                            showPaymentSheet = false
                            showMpesaDialog = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00875A).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = Color(0xFF00875A))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("M-Pesa eCitizen Paybill", style = MaterialTheme.typography.titleSmallEmphasized, fontWeight = FontWeight.Bold)
                                Text("Paybill 300059 • Instant SMIS reconciliation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    // Channel 2: eCitizen Portal
                    Surface(
                        onClick = {
                            showPaymentSheet = false
                            showEcitizenDialog = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("eCitizen National Gateway", style = MaterialTheme.typography.titleSmallEmphasized, fontWeight = FontWeight.Bold)
                                Text("Official government portal invoice receipt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    // Channel 3: Bank Direct (Absa Bank Kenya / KCB)
                    Surface(
                        onClick = {
                            showPaymentSheet = false
                            showBankDialog = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bank Deposit (Absa Bank / KCB)", style = MaterialTheme.typography.titleSmallEmphasized, fontWeight = FontWeight.Bold)
                                Text("Absa A/C 2032770838 • KCB A/C 1104698661", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Receipt Modal Bottom Sheet
        if (selectedTransactionForReceipt != null) {
            ReceiptPreviewSheet(
                transaction = selectedTransactionForReceipt!!,
                regNo = student?.regNo ?: "F16/28914/2022",
                onDismissRequest = { selectedTransactionForReceipt = null }
            )
        }

        // M-Pesa Payment Dialog
        if (showMpesaDialog) {
            var amountText by remember { mutableStateOf("18500") }
            var mpesaCode by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showMpesaDialog = false },
                title = {
                    Text("M-Pesa Express / Paybill 300059", style = MaterialTheme.typography.titleLargeEmphasized)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            text = "Paybill: 300059  •  Account: ${student?.regNo ?: "F16/28914/2022"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Amount (KES)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = mpesaCode,
                            onValueChange = { mpesaCode = it },
                            label = { Text("M-Pesa Transaction Code (e.g. UBK7294817)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                viewModel.makeMpesaPayment(amount, mpesaCode)
                                showMpesaDialog = false
                            }
                        },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text("Record M-Pesa Payment")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showMpesaDialog = false },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // eCitizen Payment Dialog
        if (showEcitizenDialog) {
            var amountText by remember { mutableStateOf("18500") }
            var invoiceRef by remember { mutableStateOf("ECIT-UON-92841") }

            AlertDialog(
                onDismissRequest = { showEcitizenDialog = false },
                title = {
                    Text("eCitizen Government Gateway", style = MaterialTheme.typography.titleLargeEmphasized)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            text = "National Government payment gateway for university tuition and exam fees.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Invoice Amount (KES)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = invoiceRef,
                            onValueChange = { invoiceRef = it },
                            label = { Text("eCitizen Invoice / Reference Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                viewModel.makeMpesaPayment(amount, invoiceRef)
                                showEcitizenDialog = false
                            }
                        },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text("Reconcile eCitizen Invoice")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEcitizenDialog = false },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Direct Bank Deposit (Absa Bank Kenya / KCB) Dialog
        if (showBankDialog) {
            var selectedBank by remember { mutableStateOf("Absa Bank Kenya") }
            var depositSlipNo by remember { mutableStateOf("") }
            var amountText by remember { mutableStateOf("18500") }

            AlertDialog(
                onDismissRequest = { showBankDialog = false },
                title = {
                    Text("Direct Bank Deposit", style = MaterialTheme.typography.titleLargeEmphasized)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(Spacing.sm)) {
                                Text(
                                    text = "• Absa Bank: A/C 2032770838 (Absa Plaza Branch)",
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "• KCB Bank: A/C 1104698661 (University Plaza Branch)",
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        OutlinedTextField(
                            value = selectedBank,
                            onValueChange = { selectedBank = it },
                            label = { Text("Selected Bank") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = depositSlipNo,
                            onValueChange = { depositSlipNo = it },
                            label = { Text("Bank Deposit Slip / Voucher Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Amount Deposited (KES)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && depositSlipNo.isNotBlank()) {
                                viewModel.makeMpesaPayment(amount, "$selectedBank: $depositSlipNo")
                                showBankDialog = false
                            }
                        },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text("Submit Bank Slip")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBankDialog = false },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun TransactionItemRow(
    tx: FeeTransaction,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (tx.type) {
                            TransactionType.INVOICE -> MaterialTheme.colorScheme.errorContainer
                            TransactionType.HELB_DISBURSEMENT, TransactionType.HEF_SCHOLARSHIP -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (tx.type) {
                        TransactionType.INVOICE -> Icons.AutoMirrored.Filled.ReceiptLong
                        TransactionType.HELB_DISBURSEMENT -> Icons.Default.School
                        TransactionType.HEF_SCHOLARSHIP -> Icons.Default.AccountBalance
                        else -> Icons.Default.Check
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ref: ${tx.referenceNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tx.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = if (tx.amount < 0) "- KES ${(-tx.amount).toInt()}" else "+ KES ${tx.amount.toInt()}",
                style = MaterialTheme.typography.titleSmallEmphasized,
                fontWeight = FontWeight.Bold,
                color = if (tx.amount < 0) Color(0xFF00875A) else MaterialTheme.colorScheme.error
            )
        }
    }
}
