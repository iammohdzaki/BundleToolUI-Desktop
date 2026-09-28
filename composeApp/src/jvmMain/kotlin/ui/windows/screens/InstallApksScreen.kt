package ui.windows.screens

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import data.state.InstallApksEvent
import ui.components.ButtonWithLoader
import ui.components.FilePickerField
import ui.components.StyledOutlinedTextField
import ui.components.WindowHeader
import ui.windows.viewmodel.InstallApksViewModel

@Composable
fun InstallApksScreen(viewModel: InstallApksViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(InstallApksEvent.Initialize)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.TopCenter
    ){
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 24.dp)
                .fillMaxSize().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Header
            WindowHeader(
                title = "Install APKs",
                subTitle = "Installs APKs extracted from an APK Set to a connected device."
            )
            
            if (state.adbPath.isBlank()) {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium).padding(16.dp)) {
                    Text(
                        "Warning: ADB path is not set in Settings. BundleTool will attempt to locate ADB in your system PATH, but it is highly recommended to set it explicitly in Settings.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // APKS File Picker
            FilePickerField(
                label = "Select APKS File (.apks)",
                value = state.apksPath,
                placeholder = "No .apks file selected",
                fileExtensionFilter = ".apks",
                onPick = { viewModel.onEvent(InstallApksEvent.SelectApksFile(it)) }
            )

            // Device ID Dropdown
            val adbManager: manager.AdbManager = org.koin.compose.koinInject()
            val allDevices by adbManager.devices.collectAsState()
            val onlineDevices = androidx.compose.runtime.remember(allDevices) { allDevices.filter { it.status == "device" } }
            var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            // Auto-pick first device if none selected and devices available
            androidx.compose.runtime.LaunchedEffect(onlineDevices) {
                if (state.deviceId.isBlank() && onlineDevices.isNotEmpty()) {
                    val first = onlineDevices.first()
                    viewModel.onEvent(InstallApksEvent.SetDeviceId(first.id))
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                StyledOutlinedTextField(
                    value = state.deviceId,
                    onValueChange = {},
                    placeholder = "Device ID (Optional, e.g. emulator-5554)",
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Device",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
                
                // Overlay box to intercept all clicks on the TextField and open the dropdown
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(androidx.compose.ui.graphics.Color.Transparent)
                        .clickable(
                            interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { expanded = true }
                )

                androidx.compose.material3.DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (onlineDevices.isEmpty()) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("No online devices found") },
                            onClick = { expanded = false }
                        )
                    } else {
                        onlineDevices.forEach { device ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { 
                                    androidx.compose.foundation.layout.Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.PhoneAndroid,
                                            contentDescription = "Device",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        androidx.compose.foundation.layout.Column {
                                            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = device.model.ifBlank { "Unknown Device" },
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 8.dp))
                                                val isOnline = device.status == "device"
                                                Box(
                                                    modifier = Modifier
                                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                                        .background(if (isOnline) androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color(0xFFE57373).copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = device.status.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        color = if (isOnline) androidx.compose.ui.graphics.Color(0xFF2E7D32) else androidx.compose.ui.graphics.Color(0xFFC62828)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = device.id, 
                                                style = MaterialTheme.typography.bodySmall, 
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.onEvent(InstallApksEvent.SetDeviceId(device.id))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }



            // Install Button
            ButtonWithLoader(
                text = "Install to Device",
                enabled = state.isReady,
                isLoading = state.isInstalling,
                onClick = { viewModel.onEvent(InstallApksEvent.Install) }
            )
            
            ui.components.LogViewer(log = state.log)
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
        )
    }
}
