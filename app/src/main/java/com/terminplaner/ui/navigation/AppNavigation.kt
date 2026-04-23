package com.terminplaner.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.terminplaner.ui.appointments.AppointmentEditScreen
import com.terminplaner.ui.appointments.AppointmentsListScreen
import com.terminplaner.ui.calendar.CalendarScreen
import com.terminplaner.ui.category.CategoryEditScreen
import com.terminplaner.ui.category.CategoriesListScreen
import com.terminplaner.ui.settings.SettingsScreen
import com.terminplaner.ui.tasks.TaskEditScreen
import com.terminplaner.ui.tasks.TasksListScreen
import com.terminplaner.ui.trash.TrashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "calendar"
    ) {
        composable("calendar") {
            CalendarScreen(navController = navController)
        }
        composable("appointments_list") {
            AppointmentsListScreen(navController = navController)
        }
        composable("tasks_list") {
            TasksListScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
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
        composable(
            route = "task_edit?taskId={taskId}&appointmentId={appointmentId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("appointmentId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            TaskEditScreen(navController = navController)
        }
    }
}
