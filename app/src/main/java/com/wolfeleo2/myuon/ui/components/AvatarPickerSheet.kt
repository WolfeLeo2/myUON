package com.wolfeleo2.myuon.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.data.model.AvatarCatalog
import com.wolfeleo2.myuon.data.model.AvatarConfig
import com.wolfeleo2.myuon.data.model.AvatarProvider
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AvatarPickerSheet(
    currentConfig: AvatarConfig,
    seed: String,
    onSelectConfig: (AvatarConfig) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProvider by remember(currentConfig) { mutableStateOf(currentConfig.provider) }
    var selectedStyle by remember(currentConfig) { mutableStateOf(currentConfig.style) }

    val filteredOptions = remember(selectedProvider) {
        AvatarCatalog.options.filter { it.provider == selectedProvider }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = Spacing.md)
        ) {
            // Sheet Header (Title + Live Preview Avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Choose Avatar Character",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Active Live Preview Circle
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    UserAvatar(
                        seed = seed,
                        config = AvatarConfig(selectedProvider, selectedStyle),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Edge-to-Edge Category / Provider Buttons Row (Full bleed scroll without parent page horizontal padding)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                AvatarProvider.entries.forEach { provider ->
                    val isSelected = selectedProvider == provider
                    val containerColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                    val contentColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        onClick = {
                            selectedProvider = provider
                            val defaultForProvider = AvatarCatalog.options.firstOrNull { it.provider == provider }?.styleKey ?: ""
                            if (filteredOptions.none { it.styleKey == selectedStyle }) {
                                selectedStyle = defaultForProvider
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        contentColor = contentColor,
                        tonalElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Text(
                            text = provider.displayName,
                            style = MaterialTheme.typography.labelMediumEmphasized,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = selectedProvider.tagLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)
            )

            Spacer(modifier = Modifier.height(Spacing.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // List of Style Options for Selected Provider
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = Spacing.screenHorizontal,
                    vertical = Spacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(filteredOptions) { option ->
                    val isOptionSelected = selectedProvider == option.provider && selectedStyle == option.styleKey

                    Surface(
                        onClick = {
                            selectedStyle = option.styleKey
                            val newConfig = AvatarConfig(option.provider, option.styleKey)
                            onSelectConfig(newConfig)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isOptionSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isOptionSelected) 2.dp else 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            // Live Avatar Thumbnail Preview
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(48.dp)
                            ) {
                                UserAvatar(
                                    seed = seed,
                                    config = AvatarConfig(option.provider, option.styleKey),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = option.displayName,
                                        style = MaterialTheme.typography.titleSmallEmphasized,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOptionSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isOptionSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOptionSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            RadioButton(
                                selected = isOptionSelected,
                                onClick = {
                                    selectedStyle = option.styleKey
                                    onSelectConfig(AvatarConfig(option.provider, option.styleKey))
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Done / Close Button
            Button(
                onClick = onDismissRequest,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Confirm Avatar Style")
            }
        }
    }
}
