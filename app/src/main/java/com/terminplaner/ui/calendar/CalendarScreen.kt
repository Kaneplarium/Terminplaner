package com.terminplaner.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.terminplaner.domain.model.Appointment
import com.terminplaner.ui.components.AppointmentCard
import com.terminplaner.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    var currentMonth by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.GERMAN)
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalender") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.AppointmentEdit.createRoute(selectedDate = selectedDate)
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neuer Termin")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = monthFormat.format(Date(currentMonth)),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
            ) { date ->
                selectedDate = date
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Termine am ${dateFormat.format(Date(selectedDate))}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val appointmentsForDate = remember(selectedDate) {
                getAppointmentsForMonthYear(currentMonth).filter { appointment ->
                    val aptCal = Calendar.getInstance().apply { timeInMillis = appointment.dateTime }
                    val selCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                    (aptCal[Calendar.YEAR] == selCal[Calendar.YEAR] &&
                            aptCal[Calendar.MONTH] == selCal[Calendar.MONTH] &&
                            aptCal[Calendar.DAY_OF_MONTH] == selCal[Calendar.DAY_OF_MONTH])
                }
            }

            if (appointmentsForDate.isEmpty()) {
                Text(
                    text = "Keine Termine für diesen Tag",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(appointmentsForDate) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            categoryColor = null,
                            onClick = {
                                navController.navigate(
                                    Screen.AppointmentEdit.createRoute(appointmentId = appointment.id)
                                )
                            },
                            onDelete = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Long,
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = currentMonth }
    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = Calendar.getInstance().apply {
        timeInMillis = currentMonth
        set(Calendar.DAY_OF_MONTH, 1)
    }.get(Calendar.DAY_OF_WEEK)

    val dayNames = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val rows = (daysInMonth + firstDayOfWeek - 2) / 7 + 1

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayIndex = row * 7 + col - firstDayOfWeek + 2
                    if (dayIndex in 1..daysInMonth) {
                        val dateCal = Calendar.getInstance().apply {
                            timeInMillis = currentMonth
                            set(Calendar.DAY_OF_MONTH, dayIndex)
                        }
                        val isSelected = (selectedCal[Calendar.YEAR] == dateCal[Calendar.YEAR] &&
                                selectedCal[Calendar.MONTH] == dateCal[Calendar.MONTH] &&
                                selectedCal[Calendar.DAY_OF_MONTH] == dateCal[Calendar.DAY_OF_MONTH])

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { onDateSelected(dateCal.timeInMillis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayIndex.toString(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

fun getAppointmentsForMonthYear(timestamp: Long): List<Appointment> {
    return emptyList()
}