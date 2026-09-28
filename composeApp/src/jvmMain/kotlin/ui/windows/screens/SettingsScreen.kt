package ui.windows.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import data.state.SettingsEvent
import ui.components.FilePickerField
import ui.components.WindowHeader
import ui.windows.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingsEvent.Initialize)
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            WindowHeader(
                title = "Settings",
                subTitle = "Global configurations for BundleTool UI"
            )

            // BundleTool Executable Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilePickerField(
                    label = "Global BundleTool Executable (.jar)",
                    value = state.bundleToolPath,
                    placeholder = "Select or download bundletool.jar",
                    fileExtensionFilter = ".jar",
                    onPick = { viewModel.onEvent(SettingsEvent.SelectBundleToolPath(it)) }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.onEvent(SettingsEvent.DownloadBundleTool) },
                        enabled = !state.isDownloading
                    ) {
                        Text(if (state.bundleToolPath.isBlank()) "Download BundleTool" else "Update BundleTool")
                    }
                    
                    if (state.isDownloading) {
                        Spacer(modifier = Modifier.width(16.dp))
                        CircularProgressIndicator(
                            progress = { state.downloadProgress },
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    
                    if (state.downloadMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = state.downloadMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.downloadMessage.startsWith("Error")) 
                                MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Text(
                    text = "If you don't have BundleTool installed, clicking 'Download' will automatically fetch the latest release from GitHub and set it up for you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ADB Executable Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilePickerField(
                    label = "Global ADB Executable (.exe or no extension)",
                    value = state.adbPath,
                    placeholder = "Select adb executable (optional, recommended)",
                    clickableText = data.model.ClickableText(
                        text = "Download Android Platform Tools",
                        url = "https://developer.android.com/tools/releases/platform-tools"
                    ),
                    onPick = { viewModel.onEvent(SettingsEvent.SelectAdbPath(it)) }
                )
                
                Text(
                    text = "ADB (Android Debug Bridge) is required for some features like 'Install APKs' or 'Get Device Spec'. BundleTool will try to find it in PATH automatically, but explicitly defining it here is recommended.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // About & System Info Section
            androidx.compose.material3.Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "About & System Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SystemInfoRow("App Version", "1.0.7")
                    SystemInfoRow("OS Environment", "${System.getProperty("os.name")} ${System.getProperty("os.version")} (${System.getProperty("os.arch")})")
                    SystemInfoRow("Java Runtime", "${System.getProperty("java.version")} - ${System.getProperty("java.vendor")}")
                    
                    val btFile = java.io.File(state.bundleToolPath)
                    val btVersion = if (btFile.exists() && btFile.name.contains("-all-")) {
                        btFile.name.substringAfter("-all-").substringBefore(".jar")
                    } else if (btFile.exists()) {
                        "Custom (${btFile.name})"
                    } else {
                        "Not configured"
                    }
                    SystemInfoRow("BundleTool Version", btVersion)
                }
            }
        }
    }
}

@Composable
private fun SystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

}
