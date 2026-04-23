package com.terminplaner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDropdown(
    label: String,
    currentTime: Long,
    onTimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance().apply { timeInMillis = currentTime }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

    Box(modifier = modifier) {
        OutlinedTextField(
            value = timeString,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            Text("Zeit auswählen", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall)
            
            (0..23).forEach { h ->
                DropdownMenuItem(
                    text = { Text(String.format(Locale.getDefault(), "%02d:00", h)) },
                    onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = currentTime
                            set(Calendar.HOUR_OF_DAY, h)
                            set(Calendar.MINUTE, 0)
                        }
                        onTimeSelected(newCal.timeInMillis)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(String.format(Locale.getDefault(), "%02d:30", h)) },
                    onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = currentTime
                            set(Calendar.HOUR_OF_DAY, h)
                            set(Calendar.MINUTE, 30)
                        }
                        onTimeSelected(newCal.timeInMillis)
                        expanded = false
                    }
                )
            }
        }
    }
}
