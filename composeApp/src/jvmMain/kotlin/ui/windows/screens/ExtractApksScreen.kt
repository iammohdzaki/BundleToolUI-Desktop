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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import data.model.ClickableText
import data.state.ExtractApksEvent
import ui.components.ButtonWithLoader
import ui.components.FilePickerField
import ui.components.WindowHeader
import ui.windows.viewmodel.ExtractApksViewModel

@Composable
fun ExtractApksScreen(
    viewModel: ExtractApksViewModel,
    onNavigateToDeviceSpec: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ExtractApksEvent.Initialize)
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
                title = "Extract APKs",
                subTitle = "Extract APKs from an APK Set (.apks) archive."
            )

            // 1. APKS File Picker
            FilePickerField(
                label = "Select APKS File (.apks)",
                value = state.apksPath,
                placeholder = "No .apks file selected",
                fileExtensionFilter = ".apks",
                onPick = { viewModel.onEvent(ExtractApksEvent.SelectApksFile(it)) }
            )

            // 3. Output Directory
            FilePickerField(
                label = "Output Directory",
                value = state.outputDir,
                placeholder = "Where should the APKs be extracted?",
                onPick = { viewModel.onEvent(ExtractApksEvent.SelectOutputDir(it)) },
                selectFolder = true
            )

            // Device Spec File
            FilePickerField(
                label = "Device Spec JSON (Optional)",
                value = state.deviceSpecPath,
                placeholder = "Select Device Spec JSON",
                fileExtensionFilter = ".json",
                suggestions = state.specSuggestions,
                onPick = { viewModel.onEvent(ExtractApksEvent.SelectDeviceSpec(it)) },
                clickableText = ClickableText(
                    text = "Generate Device Spec",
                    onClick = onNavigateToDeviceSpec
                )
            )

            // Success Link
            state.successOutputPath?.let { path ->
                ui.components.SuccessFolderLink(path = path)
            }

            // Extract Button
            ButtonWithLoader(
                text = "Extract APKs",
                enabled = state.isReady,
                isLoading = state.isExtracting,
                onClick = { viewModel.onEvent(ExtractApksEvent.Extract) }
            )
            
            ui.components.LogViewer(log = state.log)
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
        )
    }
}
