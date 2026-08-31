package com.wolfeleo2.myuon.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.data.model.TimetableItem
import androidx.compose.foundation.layout.Arrangement
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

/**
 * Compact "at a glance" row for a single scheduled class - used by the Dashboard's
 * "Today's Schedule" list. Deliberately lighter-weight than `TimetableScreen`'s own
 * `TimetableCard` (which is a fuller, browse-a-whole-day design); the two aren't merged
 * since they serve different contexts.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TodayClassCard(
    slot: TimetableItem,
    onClick: () -> Unit,
    onOpenVenueMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current
    val cardModifier = if (sharedScope != null) {
        with(sharedScope) {
            modifier.fillMaxWidth().sharedBounds(
                rememberSharedContentState(key = "unit-${slot.unitCode}"),
                animatedVisibilityScope = animatedScope
            )
        }
    } else modifier.fillMaxWidth()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = cardModifier
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = slot.startTime.formatTime(),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = slot.endTime.formatTime(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                // Main info in Bold
                Text(
                    text = "${slot.unitCode}: ${slot.unitTitle}",
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                // First Subtitle: Lecturer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = slot.lecturer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Second Subtitle: Venue on its own line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = slot.venue,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(
                onClick = onOpenVenueMap,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = "Locate",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun String.formatTime(): String {
    val trimmed = this.trim()
    val parts = trimmed.split(":")
    return if (parts.size >= 2) {
        "${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}"
    } else trimmed
}
