package com.terminplaner.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.terminplaner.ui.appointments.AppointmentEditScreen
import com.terminplaner.ui.appointments.AppointmentsListScreen
import com.terminplaner.ui.calendar.CalendarScreen
import com.terminplaner.ui.category.CategoryEditScreen
import com.terminplaner.ui.category.CategoriesListScreen
import com.terminplaner.ui.settings.SettingsScreen
import com.terminplaner.ui.trash.TrashScreen
import kotlinx.coroutines.launch

val navItems = listOf(
    BottomNavItem.Calendar,
    BottomNavItem.Appointments,
    BottomNavItem.Settings,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val showDrawer = currentDestination?.route in listOf("calendar", "appointments_list", "settings")

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "TerminPlaner",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Divider()
                Spacer(Modifier.height(12.dp))
                navItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "calendar",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("calendar") {
                    CalendarScreen(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
                composable("appointments_list") {
                    AppointmentsListScreen(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
                composable(
                    route = "appointment_edit?appointmentId={appointmentId}&selectedDate={selectedDate}",
                    arguments = listOf(
                        navArgument("appointmentId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        },
                        navArgument("selectedDate") {
                            type = NavType.LongType
                            defaultValue = 0L
                        }
                    )
                ) {
                    AppointmentEditScreen(navController = navController)
                }
                composable("trash") {
                    TrashScreen(navController = navController)
                }
                composable("categories_list") {
                    CategoriesListScreen(navController = navController)
                }
                composable(
                    route = "category_edit?categoryId={categoryId}",
                    arguments = listOf(
                        navArgument("categoryId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        }
                    )
                ) {
                    CategoryEditScreen(navController = navController)
                }
            }
        }
    }
}
