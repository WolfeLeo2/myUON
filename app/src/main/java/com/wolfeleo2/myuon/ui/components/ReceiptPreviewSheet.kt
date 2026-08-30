package com.wolfeleo2.myuon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.data.model.FeeTransaction
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReceiptPreviewSheet(
    transaction: FeeTransaction,
    regNo: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal)
                .navigationBarsPadding()
                .padding(bottom = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF00875A).copy(alpha = 0.15f),
                contentColor = Color(0xFF00875A)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("eCitizen / SMIS Verified Receipt", style = MaterialTheme.typography.labelMediumEmphasized)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "UNIVERSITY OF NAIROBI",
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "OFFICIAL TRANSACTION RECEIPT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Surface(
                shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    ReceiptRow("Transaction Reference", transaction.referenceNumber, isBold = true)
                    ReceiptRow("Student Reg No", regNo, isBold = true)
                    ReceiptRow("Date of Payment", transaction.date)
                    ReceiptRow("Description", transaction.description)
                    ReceiptRow("Payment Channel", transaction.type.name)
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm), color = MaterialTheme.colorScheme.outlineVariant)
                    ReceiptRow("Amount Paid", "KES ${(-transaction.amount).toInt()}", isBold = true, highlightColor = Color(0xFF00875A))
                    ReceiptRow("New Outstanding Balance", "KES ${transaction.balanceAfter.toInt()}", isBold = true)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = onDismissRequest,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Share / Save Receipt PDF")
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isBold: Boolean = false, highlightColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleSmallEmphasized else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = highlightColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
