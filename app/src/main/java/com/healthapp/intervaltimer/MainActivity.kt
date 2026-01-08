package com.healthapp.intervaltimer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.healthapp.intervaltimer.data.TimerSession
import com.healthapp.intervaltimer.ui.SettingsScreen
import com.healthapp.intervaltimer.ui.TimerViewModel
import com.healthapp.intervaltimer.ui.theme.IntervalTimerTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denial
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            IntervalTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: TimerViewModel) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(onBackPressed = { showSettings = false })
    } else {
        TimerScreen(
            viewModel = viewModel,
            onSettingsClick = { showSettings = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interval Timer") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Configuration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Timer Configuration",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Activity Time Slider
                    Text(
                        text = "Activity Time: ${uiState.config.activityMinutes} minutes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = uiState.config.activityMinutes.toFloat(),
                        onValueChange = { viewModel.updateActivityMinutes(it.toInt()) },
                        valueRange = 10f..120f,
                        steps = 109,
                        enabled = !uiState.isRunning,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Rest Time Slider
                    Text(
                        text = "Rest Time: ${uiState.config.restMinutes} minutes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = uiState.config.restMinutes.toFloat(),
                        onValueChange = { viewModel.updateRestMinutes(it.toInt()) },
                        valueRange = 5f..180f,
                        steps = 174,
                        enabled = !uiState.isRunning
                    )
                }
            }

            // Start/Stop Button
            Button(
                onClick = {
                    if (uiState.isRunning) {
                        viewModel.stopTimer()
                    } else {
                        viewModel.startTimer()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isRunning)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (uiState.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (uiState.isRunning) "Stop Timer" else "Start Timer",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Status Card
            if (uiState.isRunning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Timer Running",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Current Phase: ${uiState.currentPhase.name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Total Sessions: ${uiState.totalCompletedSessions}")
                    Text("Total Cycles: ${uiState.totalCompletedCycles}")
                }
            }

            // Recent Sessions
            if (uiState.recentSessions.isNotEmpty()) {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn {
                    items(uiState.recentSessions.take(5)) { session ->
                        SessionItem(session = session)
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: TimerSession) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Text(
                text = dateFormat.format(Date(session.startTime)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Activity: ${session.activityMinutes}m | Rest: ${session.restMinutes}m | Cycles: ${session.completedCycles}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
