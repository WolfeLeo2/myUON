package com.wolfeleo2.myuon.ui.timetable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.data.model.ClassType
import com.wolfeleo2.myuon.data.model.TimetableItem
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.components.PullToRefreshBox
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel,
    onOpenUnitDetail: (String) -> Unit,
    onOpenVenueMap: (String) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Class Timetable",
                subtitle = "Lectures, Labs, Venues & Instructors",
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Day Selector Strip
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.days) { day ->
                        val isSelected = day.equals(uiState.selectedDay, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectDay(day) },
                            label = {
                                Text(
                                    text = day.take(3),
                                    style = MaterialTheme.typography.labelMediumEmphasized,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shapes = FilterChipDefaults.shapes()
                        )
                    }
                }

                // Schedule List for Selected Day
                if (uiState.filteredSlots.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                text = "No Scheduled Classes for ${uiState.selectedDay}",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enjoy your study break or library review time!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = Spacing.screenHorizontal,
                            end = Spacing.screenHorizontal,
                            bottom = Spacing.xxl
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredSlots) { slot ->
                            TimetableCard(
                                slot = slot,
                                onClick = { onOpenUnitDetail(slot.unitCode) },
                                onOpenVenueMap = { onOpenVenueMap(slot.venue) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun TimetableCard(
    slot: TimetableItem,
    onClick: () -> Unit,
    onOpenVenueMap: () -> Unit
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val cardModifier = if (sharedScope != null) {
        with(sharedScope) {
            Modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = "unit-${slot.unitCode}"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else Modifier.fillMaxWidth()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Sizes.cardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = cardModifier
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${slot.startTime} — ${slot.endTime}",
                            style = MaterialTheme.typography.labelMediumEmphasized,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (slot.classType) {
                        ClassType.LABORATORY -> Color(0xFF00875A).copy(alpha = 0.2f)
                        ClassType.TUTORIAL -> Color(0xFFFF8B00).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = when (slot.classType) {
                        ClassType.LABORATORY -> Color(0xFF00875A)
                        ClassType.TUTORIAL -> Color(0xFFFF8B00)
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                ) {
                    Text(
                        text = slot.classType.name,
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Main Title in Bold
            Text(
                text = "${slot.unitCode}: ${slot.unitTitle}",
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // First Subtitle: Lecturer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = slot.lecturer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•  ${slot.lecturerEmail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Second Subtitle: Venue Location Row with Navigation Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF00875A),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${slot.venue} (${slot.campus})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                FilledTonalButton(
                    onClick = onOpenVenueMap,
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", style = MaterialTheme.typography.labelSmallEmphasized)
                }
            }
        }
    }
}
