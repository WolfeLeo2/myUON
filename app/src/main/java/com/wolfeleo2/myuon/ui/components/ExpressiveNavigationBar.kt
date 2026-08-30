package com.wolfeleo2.myuon.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.wolfeleo2.myuon.ui.navigation.*

data class NavTabItem(
    val route: TopLevelDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val NavTabItems = listOf(
    NavTabItem(Dashboard, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavTabItem(Academics, "Academics", Icons.Filled.School, Icons.Outlined.School),
    NavTabItem(Fees, "Fees", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    NavTabItem(Timetable, "Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    NavTabItem(Hostels, "Hostels", Icons.Filled.Hotel, Icons.Outlined.Hotel)
)

@Composable
fun ExpressiveNavigationBar(
    currentDestination: NavKey,
    onTabSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        NavTabItems.forEach { item ->
            val isSelected = currentDestination == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = if (isSelected) MaterialTheme.typography.labelMediumEmphasized else MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
