package com.healthapp.intervaltimer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.healthapp.intervaltimer.api.ApiConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val apiConfig = remember { ApiConfig(context) }
    val scope = rememberCoroutineScope()

    var useStubApi by remember { mutableStateOf(true) }
    var syncEnabled by remember { mutableStateOf(false) }
    var apiBaseUrl by remember { mutableStateOf(ApiConfig.DEFAULT_BASE_URL) }

    // Load current settings
    LaunchedEffect(Unit) {
        useStubApi = apiConfig.isUsingStubApi()
        syncEnabled = apiConfig.isSyncEnabled()
        apiBaseUrl = apiConfig.getBaseUrl()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Mode Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "API Configuration",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Stub API Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use Stub API",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (useStubApi) "Fake data without backend" else "Connect to real backend",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useStubApi,
                            onCheckedChange = { enabled ->
                                useStubApi = enabled
                                scope.launch {
                                    apiConfig.setUseStubApi(enabled)
                                }
                            }
                        )
                    }

                    // API Base URL (only shown when not using stub)
                    if (!useStubApi) {
                        OutlinedTextField(
                            value = apiBaseUrl,
                            onValueChange = { url ->
                                apiBaseUrl = url
                                scope.launch {
                                    apiConfig.setBaseUrl(url)
                                }
                            },
                            label = { Text("API Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Sync Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sync Settings",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Sync Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Sync",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (syncEnabled) "Sessions sync to backend" else "Local storage only",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = syncEnabled,
                            onCheckedChange = { enabled ->
                                syncEnabled = enabled
                                scope.launch {
                                    apiConfig.setSyncEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Development Mode",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Stub API provides fake data for testing without a real backend. " +
                                "Enable sync when you have a backend server configured.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
