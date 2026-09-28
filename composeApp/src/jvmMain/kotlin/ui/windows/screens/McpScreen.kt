package ui.windows.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ui.components.WindowHeader
import ui.windows.viewmodel.McpViewModel

@Composable
fun McpScreen(viewModel: McpViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WindowHeader(
            title = "Model Context Protocol (MCP)",
            subTitle = "Allow AI assistants to automate BundleTool locally"
        )

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            indicator = { /* Hide default indicator */ },
            divider = { }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Setup & Config") },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Live Server Logs") },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (selectedTab == 0) {
            McpSetupTab(state = state, viewModel = viewModel)
        } else {
            McpLogsTab(state = state, onClear = { viewModel.clearLogs() })
        }
    }
}

@Composable
private fun McpSetupTab(state: ui.windows.viewmodel.McpState, viewModel: McpViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("MCP Server Status", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (state.isRunning) "Running on port ${state.port}" else "Offline",
                            color = if (state.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = { viewModel.toggleServer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (state.isRunning) "Stop Server" else "Start Server")
                    }
                }
                
                OutlinedTextField(
                    value = state.port,
                    onValueChange = { viewModel.updatePort(it) },
                    label = { Text("Server Port") },
                    enabled = !state.isRunning,
                    modifier = Modifier.width(150.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AI Configuration Snippet", style = MaterialTheme.typography.titleMedium)
            Text("Copy this JSON into your Claude Desktop, Antigravity, or other MCP client configuration file to give it access to BundleTool.", style = MaterialTheme.typography.bodyMedium)
            
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFF1E1E1E))
            ) {
                Text(
                    text = state.configSnippet,
                    color = androidx.compose.ui.graphics.Color(0xFFD4D4D4),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp).padding(end = 40.dp)
                )
                
                IconButton(
                    onClick = { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(state.configSnippet)) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                        contentDescription = "Copy to clipboard",
                        tint = androidx.compose.ui.graphics.Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun McpLogsTab(state: ui.windows.viewmodel.McpState, onClear: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Server Activity", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onClear) { Text("Clear Logs") }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(androidx.compose.ui.graphics.Color(0xFF1E1E1E))
                .padding(16.dp)
        ) {
            if (state.logs.isEmpty()) {
                Text(
                    text = "No logs yet. Start the server and connect an MCP client.",
                    color = androidx.compose.ui.graphics.Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.logs.forEach { log ->
                        Text(
                            text = log,
                            color = androidx.compose.ui.graphics.Color(0xFFA8C7FA),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
