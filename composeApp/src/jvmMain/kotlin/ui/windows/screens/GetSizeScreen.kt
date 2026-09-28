package ui.windows.screens

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import data.state.GetSizeEvent
import ui.components.ButtonWithLoader
import ui.components.FilePickerField
import ui.components.StyledOutlinedTextField
import ui.components.WindowHeader
import ui.windows.viewmodel.GetSizeViewModel

@Composable
fun GetSizeScreen(
    viewModel: GetSizeViewModel,
    onNavigateToDeviceSpec: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(GetSizeEvent.Initialize)
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
                title = "Get Size",
                subTitle = "Calculates the total size of the APKs that would be served to a specific device configuration."
            )

            // APKS File Picker
            FilePickerField(
                label = "Select APKS File (.apks)",
                value = state.apksPath,
                placeholder = "No .apks file selected",
                fileExtensionFilter = ".apks",
                onPick = { viewModel.onEvent(GetSizeEvent.SelectApksFile(it)) }
            )
            
            // Device Spec File
            FilePickerField(
                label = "Device Spec JSON (Optional, but recommended)",
                value = state.deviceSpecPath,
                placeholder = "Target a specific device configuration",
                fileExtensionFilter = ".json",
                suggestions = state.specSuggestions,
                onPick = { viewModel.onEvent(GetSizeEvent.SelectDeviceSpec(it)) },
                clickableText = data.model.ClickableText(
                    text = "Generate Device Spec",
                    onClick = onNavigateToDeviceSpec
                )
            )

            // Modules
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Text(
                    text = "Modules (Optional, leave unselected for default)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (state.apksPath.isBlank()) {
                    androidx.compose.material3.Text(
                        text = "Select an .apks file first to auto-detect modules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else if (state.availableModules.isEmpty()) {
                    StyledOutlinedTextField(
                        value = state.modules,
                        onValueChange = { viewModel.onEvent(GetSizeEvent.SetModules(it)) },
                        placeholder = "Modules (e.g. base,module1)",
                        supportingText = "Could not auto-detect modules. Enter a comma-separated list.",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // We have detected modules, render them as chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    ) {
                        val selectedOptions = state.modules.split(",").filter { it.isNotBlank() }.toSet()
    
                        state.availableModules.forEach { option ->
                            val isSelected = selectedOptions.contains(option)
                            ui.components.SelectableChip(
                                isSelected = isSelected,
                                onClick = {
                                    val newOptions = if (isSelected) {
                                        selectedOptions - option
                                    } else {
                                        selectedOptions + option
                                    }
                                    val newModulesString = newOptions.joinToString(",")
                                    viewModel.onEvent(GetSizeEvent.SetModules(newModulesString))
                                },
                                text = option
                            )
                        }
                    }
                }
            }

            // Dimensions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Text(
                    text = "Dimensions to Measure (Leave all unselected for 'ALL')",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    val allOptions = listOf("SDK", "ABI", "SCREEN_DENSITY", "LANGUAGE")
                    val selectedOptions = state.dimensions.split(",").filter { it.isNotBlank() && it != "ALL" }.toSet()

                    allOptions.forEach { option ->
                        val isSelected = selectedOptions.contains(option)
                        ui.components.SelectableChip(
                            isSelected = isSelected,
                            onClick = {
                                val newOptions = if (isSelected) {
                                    selectedOptions - option
                                } else {
                                    selectedOptions + option
                                }
                                val newDimensionsString = if (newOptions.isEmpty()) "ALL" else newOptions.joinToString(",")
                                viewModel.onEvent(GetSizeEvent.SetDimensions(newDimensionsString))
                            },
                            text = option
                        )
                    }
                }
            }

            // Generate Button
            ButtonWithLoader(
                text = "Calculate Size",
                enabled = state.isReady,
                isLoading = state.isExecuting,
                onClick = { viewModel.onEvent(GetSizeEvent.CalculateSize) }
            )
            
            if (state.parsedSizeResults != null) {
                SizeResultsVisualizer(results = state.parsedSizeResults!!)
            }

            ui.components.LogViewer(log = state.log)
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
        )
    }
}

@Composable
fun SizeResultsVisualizer(results: List<data.state.SizeResultRow>) {
    val maxBytesOverall = results.maxOfOrNull { it.maxBytes }?.coerceAtLeast(1L) ?: 1L

    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                text = "Estimated Download Sizes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        results.forEach { res ->
            androidx.compose.material3.ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(),
                elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Side: Tags
                        if (res.dimensions.isEmpty()) {
                            androidx.compose.material3.Text(
                                text = "Universal / Total App Size",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            // FlowRow for chips
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.weight(1f).horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                res.dimensions.forEach { (key, value) ->
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = "$key: $value",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))

                        // Right Side: Size
                        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.End) {
                            val sizeText = if (res.minBytes == res.maxBytes) {
                                formatSize(res.minBytes)
                            } else {
                                "${formatSize(res.minBytes)} – ${formatSize(res.maxBytes)}"
                            }
                            androidx.compose.material3.Text(
                                text = sizeText,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.material3.Text(
                                text = "(${res.maxBytes} B)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Progress bar
                    if (results.size > 1 || res.dimensions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val fraction = (res.maxBytes.toFloat() / maxBytesOverall.toFloat()).coerceIn(0f, 1f)
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(java.util.Locale.US, "%.2f MB", mb)
        kb >= 1.0 -> String.format(java.util.Locale.US, "%.2f KB", kb)
        else -> "$bytes B"
    }
}
