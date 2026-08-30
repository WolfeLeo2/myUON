package com.wolfeleo2.myuon.ui.hostels

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.data.model.GenderTarget
import com.wolfeleo2.myuon.data.model.HostelHall
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.components.PullToRefreshBox
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HostelsScreen(
    viewModel: HostelsViewModel,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeBooking = uiState.activeBooking
    var selectedHallForBooking by remember { mutableStateOf<HostelHall?>(null) }

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Hostels & Accommodation",
                subtitle = "HAMIS Halls of Residence Booking",
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
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Spacing.screenHorizontal,
                    end = Spacing.screenHorizontal,
                    top = Spacing.sm,
                    bottom = Spacing.xl
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxSize()
            ) {
                // Active Allocation Card
                item {
                    if (activeBooking != null) {
                        Surface(
                            shape = RoundedCornerShape(Sizes.cardCornerRadiusLg),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(Spacing.lg)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ACTIVE ROOM ALLOCATION",
                                        style = MaterialTheme.typography.labelSmallEmphasized,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF00875A),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (activeBooking.isPaid) "CLEARED & OCCUPIED" else "PENDING RENT",
                                            style = MaterialTheme.typography.labelSmallEmphasized,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(Spacing.xs))

                                Text(
                                    text = "${activeBooking.hallName} • Room ${activeBooking.roomNumber}",
                                    style = MaterialTheme.typography.titleLargeEmphasized,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Text(
                                    text = "KES ${activeBooking.rentAmount.toInt()} / Semester",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )

                                Spacer(modifier = Modifier.height(Spacing.sm))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!activeBooking.isPaid) {
                                        Button(
                                            onClick = viewModel::payRent,
                                            shapes = ButtonDefaults.shapes()
                                        ) {
                                            Text("Pay Rent (KES ${activeBooking.rentAmount.toInt()})")
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ) {
                                            Text(
                                                text = "✓ Key Issued at Hall Custodian Office",
                                                style = MaterialTheme.typography.labelSmallEmphasized,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Real-Time Vacancy Radar Header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "HAMIS Real-Time Vacancy Radar",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Filter Chips
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.selectedGenderFilter == null,
                            onClick = { viewModel.setGenderFilter(null) },
                            label = { Text("All Halls") }
                        )
                        FilterChip(
                            selected = uiState.selectedGenderFilter == GenderTarget.MALE,
                            onClick = { viewModel.setGenderFilter(GenderTarget.MALE) },
                            label = { Text("Male Halls") }
                        )
                        FilterChip(
                            selected = uiState.selectedGenderFilter == GenderTarget.FEMALE,
                            onClick = { viewModel.setGenderFilter(GenderTarget.FEMALE) },
                            label = { Text("Female Halls") }
                        )
                    }
                }

                // Available Halls List
                items(uiState.filteredHalls) { hall ->
                    HostelHallCard(
                        hall = hall,
                        onBookClick = { selectedHallForBooking = hall }
                    )
                }
            }
        }

        // Room Reservation Dialog
        if (selectedHallForBooking != null) {
            val hall = selectedHallForBooking!!
            var selectedRoom by remember { mutableStateOf("104") }
            var selectedBed by remember { mutableStateOf("Bed 2 (Window)") }

            AlertDialog(
                onDismissRequest = { selectedHallForBooking = null },
                title = {
                    Text("Reserve Room in ${hall.hallName}", style = MaterialTheme.typography.titleLargeEmphasized)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            text = "${hall.campus}  •  KES ${hall.rentPerSemester.toInt()} / Semester",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = selectedRoom,
                            onValueChange = { selectedRoom = it },
                            label = { Text("Room Number (e.g. 104)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.applyForRoom(hall, selectedRoom, "Allocated")
                            selectedHallForBooking = null
                        },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text("Confirm Booking")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedHallForBooking = null },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HostelHallCard(
    hall: HostelHall,
    onBookClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(Sizes.cardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hall.hallName,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${hall.campus}  •  ${hall.genderTarget.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hall.availableRooms > 0) Color(0xFF00875A).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                    contentColor = if (hall.availableRooms > 0) Color(0xFF00875A) else MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = if (hall.availableRooms > 0) "${hall.availableRooms} BEDS VACANT" else "FULL",
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "KES ${hall.rentPerSemester.toInt()} per semester",
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                hall.amenities.take(3).forEach { amenity ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = amenity,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Button(
                onClick = onBookClick,
                enabled = hall.availableRooms > 0 && hall.isBookingOpen,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (hall.availableRooms > 0) "Select & Reserve Room" else "Fully Booked")
            }
        }
    }
}
