package ui.windows.screens

import aabtoapk.composeapp.generated.resources.Res
import aabtoapk.composeapp.generated.resources.bundletool_not_selected
import aabtoapk.composeapp.generated.resources.download_bundletool
import aabtoapk.composeapp.generated.resources.no_aab_selected
import aabtoapk.composeapp.generated.resources.step_four_subtitle
import aabtoapk.composeapp.generated.resources.step_four_title
import aabtoapk.composeapp.generated.resources.step_one_title
import aabtoapk.composeapp.generated.resources.step_three_title
import aabtoapk.composeapp.generated.resources.step_two_title
import aabtoapk.composeapp.generated.resources.subtitle
import aabtoapk.composeapp.generated.resources.title
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import data.state.BundleToolEvent
import data.state.OutputMode
import org.jetbrains.compose.resources.stringResource
import ui.components.ButtonWithLoader
import ui.components.FilePickerField
import ui.components.LogBox
import ui.components.OptionSelector
import ui.components.SigningSection
import ui.components.WindowHeader
import ui.windows.viewmodel.HomeViewModel

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BuildApksScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(BundleToolEvent.Initialize)
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
                title = "Build APKs",
                subTitle = "Convert Android App Bundles to various APK formats with ease."
            )
            // 1. AAB File Picker
            FilePickerField(
                label = stringResource(Res.string.step_one_title),
                value = state.aabPath,
                placeholder = stringResource(Res.string.no_aab_selected),
                fileExtensionFilter = ".aab",
                onPick = { viewModel.onEvent(BundleToolEvent.SelectAabFile(it)) }
            )

            // 2. Output Mode Selector
            OptionSelector(
                title = stringResource(Res.string.step_three_title),
                options = listOf(OutputMode.Universal, OutputMode.ApkSet),
                selected = state.mode,
                onSelect = { viewModel.onEvent(BundleToolEvent.SelectMode(it)) },
                optionLabel = {
                    when (it) {
                        OutputMode.Universal -> "Universal APK"
                        OutputMode.ApkSet -> "APK Set (.apks)"
                        OutputMode.DeviceSpecific -> "Device-specific"
                    }
                }
            )

            // 4. Output Directory
            FilePickerField(
                label = stringResource(Res.string.step_four_title),
                value = state.outputDir,
                placeholder = stringResource(Res.string.step_four_subtitle),
                onPick = { viewModel.onEvent(BundleToolEvent.SelectOutputDir(it)) },
                selectFolder = true
            )

            // 5. Signing Keystore
            SigningSection(
                signingMode = state.signingState.signingMode,
                keystorePath = state.signingState.keystorePath,
                keystorePassword = state.signingState.keystorePassword,
                keyAlias = state.signingState.keyAlias,
                keyPassword = state.signingState.keyPassword,
                onModeChange = { viewModel.onEvent(BundleToolEvent.SelectSigning(it)) },
                onPickKeystore = { viewModel.onEvent(BundleToolEvent.SelectKeyStore(it)) },
                onKeystorePasswordChange = { viewModel.onEvent(BundleToolEvent.SelectKeyStorePassword(it)) },
                onAliasChange = { viewModel.onEvent(BundleToolEvent.SelectAlias(it)) },
                onKeyPasswordChange = { viewModel.onEvent(BundleToolEvent.SelectKeyPassword(it)) }
            )

            // 6. Advanced Options
            androidx.compose.material3.ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors()
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.Text(
                        "Advanced Options (Optional)", 
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    
                    // Device Targeting Sub-Section
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.Text(
                            "Device Targeting",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        
                        val adbManager: manager.AdbManager = org.koin.compose.koinInject()
                        val allDevices by adbManager.devices.collectAsState()
                        val onlineDevices = remember(allDevices) { allDevices.filter { it.status == "device" } }
                        var expanded by remember { mutableStateOf(false) }
            
                        // Auto-pick first device if none selected and devices available
                        LaunchedEffect(onlineDevices) {
                            if (state.deviceId.isBlank() && onlineDevices.isNotEmpty()) {
                                val first = onlineDevices.first()
                                viewModel.onEvent(BundleToolEvent.SetDeviceId(first.id))
                                viewModel.onEvent(BundleToolEvent.SetConnectedDevice(true))
                            }
                        }

                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth()) {
                                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Text(
                                        text = "Target Connected Device",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    ui.components.HelpTooltip(
                                        text = "Instructs bundletool to build APKs that target a specific connected device.",
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                                    ui.components.StyledOutlinedTextField(
                                        value = state.deviceId,
                                        onValueChange = {},
                                        placeholder = "Select connected device",
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
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(androidx.compose.ui.graphics.Color.Transparent)
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                expanded = true
                                            }
                                    )
                                    
                                    androidx.compose.material3.DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        if (onlineDevices.isEmpty()) {
                                            androidx.compose.material3.DropdownMenuItem(
                                                text = { androidx.compose.material3.Text("No devices found") },
                                                onClick = { expanded = false }
                                            )
                                        } else {
                                            onlineDevices.forEach { device ->
                                                androidx.compose.material3.DropdownMenuItem(
                                                    text = { androidx.compose.material3.Text("${device.model} (${device.id})") },
                                                    onClick = {
                                                        viewModel.onEvent(BundleToolEvent.SetDeviceId(device.id))
                                                        viewModel.onEvent(BundleToolEvent.SetConnectedDevice(true))
                                                        // Deselect spec path if device is chosen
                                                        viewModel.onEvent(BundleToolEvent.SelectDeviceSpecPath(""))
                                                        expanded = false
                                                    }
                                                )
                                            }
                                            androidx.compose.material3.DropdownMenuItem(
                                                text = { androidx.compose.material3.Text("None / Clear") },
                                                onClick = {
                                                    viewModel.onEvent(BundleToolEvent.SetDeviceId(""))
                                                    viewModel.onEvent(BundleToolEvent.SetConnectedDevice(false))
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            androidx.compose.material3.Text(
                                "OR", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                                FilePickerField(
                                    label = "Device Spec JSON",
                                    value = state.deviceSpecPath,
                                    placeholder = "E.g. pixel4.json",
                                    fileExtensionFilter = ".json",
                                    suggestions = state.specSuggestions,
                                    tooltipText = "Provides a path to a .json file that specifies the device configuration you want to target.",
                                    onPick = { 
                                        viewModel.onEvent(BundleToolEvent.SelectDeviceSpecPath(it))
                                        // Deselect connected device if a spec path is chosen
                                        if (it.isNotBlank()) {
                                            viewModel.onEvent(BundleToolEvent.SetDeviceId(""))
                                            viewModel.onEvent(BundleToolEvent.SetConnectedDevice(false))
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    // General Options Sub-Section
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Checkbox(
                                    checked = state.overwrite,
                                    onCheckedChange = { viewModel.onEvent(BundleToolEvent.SetOverwrite(it)) }
                                )
                                androidx.compose.material3.Text("Overwrite output", style = MaterialTheme.typography.bodyMedium)
                                ui.components.HelpTooltip("Overwrites any existing .apks file with the same output path.")
                            }
                            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Checkbox(
                                    checked = state.localTesting,
                                    onCheckedChange = { viewModel.onEvent(BundleToolEvent.SetLocalTesting(it)) }
                                )
                                androidx.compose.material3.Text("Local testing flag", style = MaterialTheme.typography.bodyMedium)
                                ui.components.HelpTooltip("Enables your app bundle for local testing, allowing for quick iterative cycles without uploading to Play Servers.")
                            }
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = state.localTesting,
                            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(12.dp)
                            ) {
                                androidx.compose.material3.Text(
                                    text = "Note: If you're using the --local-testing flag with the build-apks command, you MUST use the 'Install APKs' screen (bundletool install-apks) to install your APKs to ensure that local testing works correctly. Standard adb install will not work.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Success Link
            state.successOutputPath?.let { path ->
                Spacer(modifier = Modifier.height(16.dp))
                ui.components.SuccessFolderLink(path = path)
            }

            // Build Button
            ButtonWithLoader(
                text = "Build .apks",
                enabled = state.isReady,
                isLoading = state.isConverting,
                onClick = { viewModel.onEvent(BundleToolEvent.Convert) }
            )
            
            ui.components.LogViewer(log = state.log)
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
        )
    }
}
