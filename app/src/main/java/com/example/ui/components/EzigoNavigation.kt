package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val permissionKey: String
)

val navItems = listOf(
    NavItem(Screen.DASHBOARD, "Dashboard", Icons.Default.Dashboard, "Dashboard"),
    NavItem(Screen.STUDENTS, "Students", Icons.Default.People, "Students"),
    NavItem(Screen.ATTENDANCE, "Attendance", Icons.Default.EventAvailable, "Attendance"),
    NavItem(Screen.FACULTY, "Faculty & Hours", Icons.Default.School, "Faculty"),
    NavItem(Screen.FEES, "Fees", Icons.Default.AccountBalanceWallet, "Fees"),
    NavItem(Screen.EXAMS, "Exams", Icons.Default.AssignmentTurnedIn, "Exams"),
    NavItem(Screen.SYLLABUS, "Syllabus", Icons.Default.MenuBook, "Syllabus"),
    NavItem(Screen.REPORTS, "Reports", Icons.Default.Description, "Reports"),
    NavItem(Screen.SETTINGS, "Settings", Icons.Default.Settings, "Settings")
)

@Composable
fun EzigoBottomNavBar(
    currentScreen: Screen,
    canAccess: (String) -> Unit,
    canAccessModule: (String) -> Boolean,
    onScreenSelected: (Screen) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            navItems.forEach { item ->
                val accessible = canAccessModule(item.permissionKey)
                if (accessible) {
                    val isSelected = currentScreen == item.screen
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        selected = isSelected,
                        onClick = { onScreenSelected(item.screen) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EzigoNavRail(
    currentScreen: Screen,
    canAccessModule: (String) -> Boolean,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        navItems.forEach { item ->
            if (canAccessModule(item.permissionKey)) {
                val isSelected = currentScreen == item.screen
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onScreenSelected(item.screen) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }
    }
}
