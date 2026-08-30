package com.wolfeleo2.myuon.ui.venues

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.ui.components.ExpressiveShapeBadge
import com.wolfeleo2.myuon.ui.components.ExpressiveTopAppBar
import com.wolfeleo2.myuon.ui.theme.ExpressivePolygons
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

data class CampusVenue(
    val code: String,
    val name: String,
    val campus: String,
    val description: String,
    val walkingTimeFromGate: String
)

val SampleCampusVenues = listOf(
    CampusVenue(
        code = "MLT 01",
        name = "Main Lecture Theatre 01",
        campus = "Main Campus",
        description = "Large capacity theatre adjacent to Gandhi Wing and Engineering Quad.",
        walkingTimeFromGate = "3 mins from Main Gate"
    ),
    CampusVenue(
        code = "MLT 02",
        name = "Main Lecture Theatre 02",
        campus = "Main Campus",
        description = "Upper tier amphitheatre dedicated to faculty examinations and inter-school lectures.",
        walkingTimeFromGate = "4 mins from Main Gate"
    ),
    CampusVenue(
        code = "Chiromo Millennium Hall 01",
        name = "Millennium Hall 01",
        campus = "Chiromo Science Campus",
        description = "State of the art science auditorium next to CBPS Physical Sciences building.",
        walkingTimeFromGate = "2 mins from Chiromo Gate"
    ),
    CampusVenue(
        code = "Chiromo Lab 02",
        name = "Department of Computer Science Lab 02",
        campus = "Chiromo Science Campus",
        description = "High performance computing and database development laboratory.",
        walkingTimeFromGate = "3 mins from Chiromo Library"
    ),
    CampusVenue(
        code = "8-4-4 Room 04",
        name = "8-4-4 Building Lecture Room 04",
        campus = "Main Campus",
        description = "Multi-story lecture complex opposite Jomo Kenyatta Memorial Library (JKML).",
        walkingTimeFromGate = "5 mins from Central Catering Unit"
    ),
    CampusVenue(
        code = "ED 02",
        name = "Education Building Room 02",
        campus = "Kikuyu Campus",
        description = "Education faculty lecture hall near Kenya National Examinations Council resource centre.",
        walkingTimeFromGate = "2 mins from Kikuyu Admin Block"
    ),
    CampusVenue(
        code = "Parklands LT 01",
        name = "School of Law Lecture Theatre 01",
        campus = "Parklands Campus",
        description = "Moot court adjacent lecture room for legal jurisprudence and constitutional studies.",
        walkingTimeFromGate = "3 mins from Parklands Gate"
    ),
    CampusVenue(
        code = "Lower Kabete LT 03",
        name = "Faculty of Business LT 03",
        campus = "Lower Kabete Campus",
        description = "Modern business theatre adjacent to the MBA library complex.",
        walkingTimeFromGate = "4 mins from Lower Kabete Main Gate"
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VenueMapScreen(
    initialVenueCode: String = "",
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(initialVenueCode) }
    val filteredVenues = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            SampleCampusVenues
        } else {
            SampleCampusVenues.filter {
                it.code.contains(searchQuery, ignoreCase = true) ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.campus.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            ExpressiveTopAppBar(
                title = "Campus Venues & Maps",
                subtitle = "Lecture Theatres, Computer Labs & Exam Halls",
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
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search venue, building or campus...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm)
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start = Spacing.screenHorizontal,
                    end = Spacing.screenHorizontal,
                    bottom = Spacing.xl
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxSize()
            ) {
                // Campus Hub Information Card
                item {
                    Surface(
                        shape = RoundedCornerShape(Sizes.cardCornerRadius),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            ExpressiveShapeBadge(
                                polygon = ExpressivePolygons.Arch,
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                size = 44.dp,
                                icon = Icons.Default.Map
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "University Campus Transit",
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Walking directions calibrated from main campus entry gates with real-time transit landmarks.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                items(filteredVenues) { venue ->
                    Surface(
                        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    ExpressiveShapeBadge(
                                        polygon = ExpressivePolygons.Cookie9Sided,
                                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.primary,
                                        size = 36.dp,
                                        icon = Icons.Default.LocationOn
                                    )

                                    Column {
                                        Text(
                                            text = venue.code,
                                            style = MaterialTheme.typography.titleMediumEmphasized,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = venue.campus,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CAMPUS MAP",
                                        style = MaterialTheme.typography.labelSmallEmphasized,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Text(
                                text = venue.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = venue.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                    contentDescription = null,
                                    tint = Color(0xFF00875A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = venue.walkingTimeFromGate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF00875A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
