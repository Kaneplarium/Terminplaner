package com.terminplaner.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.ui.components.TimeDropdown
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    navController: NavController,
    viewModel: TaskEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Aufgabe bearbeiten" else "Neue Aufgabe")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Erinnerung", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = uiState.reminderTime?.let { fullDateFormat.format(Date(it)) } ?: "Keine Erinnerung",
                onValueChange = { },
                label = { Text("Erinnerungsdatum") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Datum wählen")
                    }
                }
            )

            TimeDropdown(
                label = "Uhrzeit",
                currentTime = uiState.reminderTime ?: System.currentTimeMillis(),
                onTimeSelected = { viewModel.updateReminderTime(it) },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.reminderTime != null) {
                TextButton(onClick = { viewModel.updateReminderTime(null) }) {
                    Text("Erinnerung entfernen")
                }
            }

            Divider()

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Titel") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.titleError,
                supportingText = if (uiState.titleError) {
                    { Text("Titel ist erforderlich") }
                } else null
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Beschreibung (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("Zu Termin zuordnen", style = MaterialTheme.typography.titleMedium)

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                val selectedAppointment = uiState.appointments.find { it.id == uiState.appointmentId }
                OutlinedTextField(
                    value = if (selectedAppointment != null) {
                        "${selectedAppointment.title} (${dateFormat.format(Date(selectedAppointment.dateTime))})"
                    } else {
                        "Kein Termin"
                    },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Termin wählen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Kein Termin") },
                        onClick = {
                            viewModel.updateAppointment(null)
                            expanded = false
                        }
                    )
                    uiState.appointments.forEach { appointment ->
                        DropdownMenuItem(
                            text = { 
                                Text("${appointment.title} (${dateFormat.format(Date(appointment.dateTime))})") 
                            },
                            onClick = {
                                viewModel.updateAppointment(appointment.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Speichern")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.reminderTime ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        val currentReminder = uiState.reminderTime ?: System.currentTimeMillis()
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = currentReminder
                        }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                        }
                        viewModel.updateReminderTime(newCal.timeInMillis)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
