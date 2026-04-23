package com.terminplaner.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.navigation.Screen
import com.terminplaner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val darkThemeMode by viewModel.darkThemeMode.collectAsState()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                viewModel.importData(reader.readText())
            }
        }
    }

    val storageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            viewModel.updateStoragePath(it.toString())
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Einstellungen",
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Kategorie: Allgemein
            Text(
                text = "Allgemein",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            ListItem(
                headlineContent = { Text("Papierkorb") },
                supportingContent = { Text("Gelöschte Termine anzeigen") },
                leadingContent = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Trash.route)
                }
            )

            ListItem(
                headlineContent = { Text("Kategorien verwalten") },
                supportingContent = { Text("Kategorien erstellen und bearbeiten") },
                leadingContent = {
                    Icon(Icons.Default.Palette, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    navController.navigate(Screen.CategoriesList.route)
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Kategorie: Datenverwaltung
            Text(
                text = "Datenverwaltung",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            ListItem(
                headlineContent = { Text("Daten exportieren") },
                supportingContent = { Text("Termine und Kategorien als JSON speichern") },
                leadingContent = {
                    Icon(Icons.Default.Upload, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    viewModel.exportData(context)
                }
            )

            ListItem(
                headlineContent = { Text("Daten importieren") },
                supportingContent = { Text("Termine und Kategorien aus JSON laden") },
                leadingContent = {
                    Icon(Icons.Default.Download, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    importLauncher.launch("application/json")
                }
            )

            ListItem(
                headlineContent = { Text("Speicherort wählen") },
                supportingContent = { Text(uiState.storagePath ?: "Standard-Speicherpfad für Exporte festlegen") },
                leadingContent = {
                    Icon(Icons.Default.Storage, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    storageLauncher.launch(null)
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Kategorie: Erscheinungsbild
            Text(
                text = "Erscheinungsbild",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            ListItem(
                headlineContent = { Text("Dunkles Design") },
                supportingContent = { 
                    Text(when(darkThemeMode) {
                        ThemePreferences.MODE_LIGHT -> "Hell"
                        ThemePreferences.MODE_DARK -> "Dunkel"
                        else -> "System-Standard"
                    })
                },
                leadingContent = {
                    Icon(Icons.Default.DarkMode, contentDescription = null)
                },
                trailingContent = {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text("Wählen")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("System-Standard") },
                                onClick = { 
                                    viewModel.setDarkMode(ThemePreferences.MODE_SYSTEM)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hell") },
                                onClick = { 
                                    viewModel.setDarkMode(ThemePreferences.MODE_LIGHT)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Dunkel") },
                                onClick = { 
                                    viewModel.setDarkMode(ThemePreferences.MODE_DARK)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            )

            Text(
                text = "Designfarbe",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val colors = listOf(Red, Orange, Green, Blue, Pink, Brown, Grey, Black)
                colors.forEach { color ->
                    val isSelected = themeColor == color.toArgb().toLong()
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { viewModel.setThemeColor(color.toArgb().toLong()) },
                        shape = CircleShape,
                        color = color,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
                        } else null
                    ) {}
                }
            }

            if (uiState.exportSuccess) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Export erfolgreich")
                }
            }

            if (uiState.importSuccess) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Import erfolgreich")
                }
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(uiState.error!!)
                }
            }
        }
    }
}