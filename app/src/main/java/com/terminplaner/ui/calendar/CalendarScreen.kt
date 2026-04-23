package com.terminplaner.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.components.AppointmentCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.GERMAN)
    var showFilter by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Kalender",
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthFormat.format(Date(uiState.selectedDate)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showFilter = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (uiState.selectedCategoryId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ThreeWeekCalendar(
                selectedDate = uiState.selectedDate,
                allAppointments = uiState.allAppointments,
                onDateSelected = { viewModel.selectDate(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Termine",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.appointments.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.appointments) { appointment ->
                        val category = uiState.categories.find { it.id == appointment.categoryId }
                        AppointmentCard(
                            appointment = appointment,
                            categoryColor = category?.let { Color(it.color) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    if (showFilter) {
        CategoryFilterDialog(
            categories = uiState.categories,
            selectedCategoryId = uiState.selectedCategoryId,
            onCategorySelected = {
                viewModel.selectCategory(it)
                showFilter = false
            },
            onDismiss = { showFilter = false }
        )
    }
}

@Composable
fun ThreeWeekCalendar(
    selectedDate: Long,
    allAppointments: List<Appointment>,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = selectedDate
        // Start from Monday of the current week
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val days = remember(selectedDate) {
        List(21) {
            val d = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            d
        }
    }

    val dayNames = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Day names header
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        // Week 1
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 0..6) {
                DayItem(
                    millis = days[i],
                    isSelected = isSameDay(days[i], selectedDate),
                    isToday = isSameDay(days[i], today),
                    isPast = days[i] < today,
                    hasAppointments = allAppointments.any { isSameDay(it.dateTime, days[i]) },
                    onClick = { onDateSelected(days[i]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Week 2
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 7..13) {
                DayItem(
                    millis = days[i],
                    isSelected = isSameDay(days[i], selectedDate),
                    isToday = isSameDay(days[i], today),
                    isPast = days[i] < today,
                    hasAppointments = allAppointments.any { isSameDay(it.dateTime, days[i]) },
                    onClick = { onDateSelected(days[i]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Week 3
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 14..20) {
                DayItem(
                    millis = days[i],
                    isSelected = isSameDay(days[i], selectedDate),
                    isToday = isSameDay(days[i], today),
                    isPast = days[i] < today,
                    hasAppointments = allAppointments.any { isSameDay(it.dateTime, days[i]) },
                    onClick = { onDateSelected(days[i]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DayItem(
    millis: Long,
    isSelected: Boolean,
    isToday: Boolean,
    isPast: Boolean,
    hasAppointments: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else if (isToday) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else if (isPast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else MaterialTheme.colorScheme.onSurface
            )
        }
        if (hasAppointments) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = if (isPast) 0.38f else 1f)
                    )
            )
        } else {
            Spacer(modifier = Modifier.size(6.dp)) // Maintain alignment
        }
    }
}

fun isSameDay(m1: Long, m2: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = m1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = m2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun CategoryFilterDialog(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategorie filtern") },
        text = {
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = { Text("Alle anzeigen") },
                        leadingContent = { 
                            RadioButton(selected = selectedCategoryId == null, onClick = { onCategorySelected(null) }) 
                        },
                        modifier = Modifier.clickable { onCategorySelected(null) }
                    )
                }
                items(categories) { category ->
                    ListItem(
                        headlineContent = { Text(category.name) },
                        leadingContent = {
                            RadioButton(selected = selectedCategoryId == category.id, onClick = { onCategorySelected(category.id) })
                        },
                        trailingContent = {
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(category.color)))
                        },
                        modifier = Modifier.clickable { onCategorySelected(category.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Schließen") }
        }
    )
}
