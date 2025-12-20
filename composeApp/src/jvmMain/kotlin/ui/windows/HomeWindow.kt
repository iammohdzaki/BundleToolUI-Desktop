package ui.windows

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeWindow(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(BundleToolEvent.Initialize)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ){
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            WindowHeader(
                stringResource(Res.string.title),
                stringResource(Res.string.subtitle)
            )
            // 1. AAB File Picker
            FilePickerField(
                label = stringResource(Res.string.step_one_title),
                value = state.aabPath,
                placeholder = stringResource(Res.string.no_aab_selected),
                fileExtensionFilter = ".aab",
                onPick = { viewModel.onEvent(BundleToolEvent.SelectAabFile(it)) }
            )

            // 2. Choose BundleTool
            FilePickerField(
                label = stringResource(Res.string.step_two_title),
                value = state.bundleToolPath,
                placeholder = stringResource(Res.string.bundletool_not_selected),
                fileExtensionFilter = ".jar",
                clickableText = data.model.ClickableText(
                    text = stringResource(Res.string.download_bundletool),
                    url = "https://github.com/google/bundletool/releases"
                ),
                onPick = { viewModel.onEvent(BundleToolEvent.SelectBundleToolPath(it)) }
            )

            // 3. Output Mode Selector
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

            // Convert Button
            ButtonWithLoader(
                enabled = state.isReady,
                isLoading = state.isConverting,
                onClick = { viewModel.onEvent(BundleToolEvent.Convert) }
            )

            // Log Box
            LogBox(log = state.log, onRunCommand = {
                viewModel.runCustomCommand(it)
            }, onClearLogs = {
                viewModel.clearLogs()
            }
            )
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
        )
    }
}
