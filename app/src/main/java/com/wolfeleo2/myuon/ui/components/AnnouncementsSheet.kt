package com.wolfeleo2.myuon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

data class CampusNotice(
    val id: String,
    val title: String,
    val source: String,
    val date: String,
    val summary: String,
    val isUrgent: Boolean = false
)

val SampleCampusNotices = listOf(
    CampusNotice(
        id = "NOT-01",
        title = "Senate Notice: Semester 2 Official Examination Cards Released",
        source = "Academic Registrar (Examinations)",
        date = "29 Aug 2026",
        summary = "Students with 100% fee clearance and approved course unit registrations can now view and download their digital exam cards directly via the portal.",
        isUrgent = true
    ),
    CampusNotice(
        id = "NOT-02",
        title = "HAMIS Hostel Allocation Stage 2 Window Open",
        source = "Student Welfare Authority (SWA)",
        date = "28 Aug 2026",
        summary = "Online room selection for Chiromo Hall, Stella Awinja (Box), Hall 1, and Main Campus Prefabs is live until 5th September.",
        isUrgent = false
    ),
    CampusNotice(
        id = "NOT-03",
        title = "GoK HEF / HELB Band Capitation Reconciliation",
        source = "Finance & Bursar Directorate",
        date = "25 Aug 2026",
        summary = "Government student scholarship disbursements have been credited to fee ledgers. Check your fee statement to verify updated balances.",
        isUrgent = false
    ),
    CampusNotice(
        id = "NOT-04",
        title = "Eduroam Campus Wi-Fi Security Certificate Renewal",
        source = "ICT Centre (Main & Chiromo)",
        date = "20 Aug 2026",
        summary = "Students experiencing connection drops on eduroam Wi-Fi should re-authenticate using their Active Directory credentials.",
        isUrgent = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsSheet(
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
                .padding(bottom = Spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "University Announcements",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "4 NEW",
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SampleCampusNotices) { notice ->
                    Surface(
                        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = notice.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (notice.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = notice.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Text(
                                text = notice.title,
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = notice.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
