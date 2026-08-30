package com.wolfeleo2.myuon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@Composable
fun ExpressiveStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    badgeText: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(Sizes.cardCornerRadius),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.9f),
                            modifier = Modifier.size(Sizes.iconMd)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }

            if (subtitle != null || badgeText != null) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            contentColor = badgeColor
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmallEmphasized,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}
