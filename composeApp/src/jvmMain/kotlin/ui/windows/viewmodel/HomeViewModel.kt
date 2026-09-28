package ui.windows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.domain.CommandResult
import data.state.BundleToolEvent
import data.state.BundleToolState
import data.state.OutputMode
import data.state.PersistedBundleToolConfig
import data.state.SigningMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.AppPreferences
import usecase.CommandUseCase
import utils.logger

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val appPreferences: AppPreferences,
    private val commandUseCase: CommandUseCase
) : ViewModel() {
    private val log = logger()

    private val _uiState = MutableStateFlow(BundleToolState())
    val uiState: StateFlow<BundleToolState> = _uiState

    init {
        log.info("Initializing HomeViewModel...")
        onEvent(BundleToolEvent.Initialize)
    }

    fun onEvent(event: BundleToolEvent) {
        log.debug("Received event: {}", event)

        when (event) {
            is BundleToolEvent.Initialize -> loadPersistedState()
            is BundleToolEvent.SelectAabFile -> {
                _uiState.update { it.copy(aabPath = event.path) }
                persistCurrentState()
            }

            is BundleToolEvent.SelectBundleToolPath -> {
                _uiState.update { it.copy(bundleToolPath = event.path) }
                persistCurrentState()
            }

            is BundleToolEvent.SelectOutputDir -> {
                _uiState.update { it.copy(outputDir = event.path) }
                persistCurrentState()
            }

            is BundleToolEvent.SelectSigning -> {
                _uiState.update {
                    it.copy(signingState = it.signingState.copy(signingMode = event.signingMode))
                }
                persistCurrentState()
            }

            is BundleToolEvent.SelectKeyStore -> {
                _uiState.update {
                    it.copy(signingState = it.signingState.copy(keystorePath = event.path))
                }
                persistCurrentState()
            }

            is BundleToolEvent.SelectKeyStorePassword -> _uiState.update {
                it.copy(signingState = it.signingState.copy(keystorePassword = event.path))
            }

            is BundleToolEvent.SelectAlias -> _uiState.update {
                it.copy(signingState = it.signingState.copy(keyAlias = event.alias))
            }

            is BundleToolEvent.SelectKeyPassword -> _uiState.update {
                it.copy(signingState = it.signingState.copy(keyPassword = event.password))
            }

            is BundleToolEvent.SelectMode -> _uiState.update { 
                var newConnectedDevice = it.connectedDevice
                var newDeviceSpecPath = it.deviceSpecPath
                var newDeviceId = it.deviceId
                if (event.mode == OutputMode.Universal) {
                    newConnectedDevice = false
                    newDeviceSpecPath = ""
                    newDeviceId = ""
                }
                it.copy(mode = event.mode, connectedDevice = newConnectedDevice, deviceSpecPath = newDeviceSpecPath, deviceId = newDeviceId) 
            }
            is BundleToolEvent.SetOverwrite -> _uiState.update { it.copy(overwrite = event.overwrite) }
            is BundleToolEvent.SelectAapt2Path -> _uiState.update { it.copy(aapt2Path = event.path) }
            is BundleToolEvent.SetConnectedDevice -> _uiState.update { 
                var newMode = it.mode
                if (event.connectedDevice && it.mode == OutputMode.Universal) {
                    newMode = OutputMode.ApkSet
                }
                it.copy(connectedDevice = event.connectedDevice, mode = newMode) 
            }
            is BundleToolEvent.SetDeviceId -> _uiState.update { it.copy(deviceId = event.deviceId) }
            is BundleToolEvent.SelectDeviceSpecPath -> _uiState.update { 
                var newMode = it.mode
                if (event.path.isNotBlank() && it.mode == OutputMode.Universal) {
                    newMode = OutputMode.ApkSet
                }
                it.copy(deviceSpecPath = event.path, mode = newMode) 
            }
            is BundleToolEvent.SetLocalTesting -> _uiState.update { it.copy(localTesting = event.localTesting) }
            is BundleToolEvent.Convert -> convert()
        }
    }

    private fun convert() {
        val state = uiState.value
        log.info("Starting conversion for AAB: {}", state.aabPath)

        _uiState.update {
            it.copy(isConverting = true, successOutputPath = null, log = "🚀 Starting conversion...\n")
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                persistCurrentState()
                val config = command.BuildApksCommand.Config(
                    bundleToolPath = state.bundleToolPath,
                    aabPath = state.aabPath,
                    outputDir = state.outputDir,
                    isUniversal = state.mode == OutputMode.Universal,
                    keystore = if (state.signingState.signingMode == SigningMode.Release)
                        command.BuildApksCommand.KeystoreConfig(
                            path = state.signingState.keystorePath,
                            password = state.signingState.keystorePassword,
                            alias = state.signingState.keyAlias,
                            keyPassword = state.signingState.keyPassword
                        ) else null,
                    overwrite = state.overwrite,
                    aapt2Path = state.aapt2Path,
                    connectedDevice = state.connectedDevice,
                    deviceId = state.deviceId,
                    deviceSpecPath = state.deviceSpecPath,
                    localTesting = state.localTesting
                )

                log.debug("Generated Command config: {}", config)

                val (commandExecuted, result) = commandUseCase.executeBundleTool(command.BuildApksCommand(config))

                log.info("Executing command: {}", commandExecuted)
                _uiState.update { it.copy(log = it.log + "\n> $commandExecuted\n") }

                when (result) {
                    is CommandResult.Success -> {
                        log.info("Command executed successfully in {} ms", result.durationMs)
                        _uiState.update {
                            it.copy(
                                isConverting = false,
                                successOutputPath = config.outputDir ?: java.io.File(config.aabPath).parent,
                                log = it.log + "\n✅ ${result.output}\nCompleted in ${result.durationMs}ms"
                            )
                        }
                    }

                    is CommandResult.Failure -> {
                        log.error("Command execution failed: {}", result.error)
                        _uiState.update {
                            it.copy(
                                isConverting = false,
                                log = it.log + "\n❌ ${result.error}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Unexpected error during conversion", e)
                _uiState.update {
                    it.copy(
                        isConverting = false,
                        log = it.log + "\n❌ Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun runCustomCommand(command: String) {
        log.info("Running custom command: {}", command)

        _uiState.update {
            it.copy(log = it.log + "\n> $command\n")
        }

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = commandUseCase.executeCommand(command)) {
                is CommandResult.Success -> {
                    log.info("Custom command succeeded in {} ms", result.durationMs)
                    _uiState.update {
                        it.copy(
                            log = it.log + "${result.output}\n✅ Done in ${result.durationMs}ms\n"
                        )
                    }
                }

                is CommandResult.Failure -> {
                    log.error("Custom command failed: {}", result.error)
                    _uiState.update {
                        it.copy(log = it.log + "❌ ${result.error}\n")
                    }
                }
            }
        }
    }

    fun clearLogs() {
        log.debug("Clearing UI logs")
        _uiState.update { it.copy(log = "") }
    }

    private fun loadPersistedState() {
        viewModelScope.launch(Dispatchers.IO) {
            log.info("Loading persisted bundle tool state from preferences...")
            val savedConfig = appPreferences.getBundleToolConfig()
            val suggestions = getSpecSuggestions()
            
            if (savedConfig != null) {
                log.debug("Loaded persisted config: {}", savedConfig)
                _uiState.update {
                    it.copy(
                        bundleToolPath = savedConfig.bundleToolPath,
                        aabPath = savedConfig.aabPath,
                        outputDir = savedConfig.outputDir,
                        mode = savedConfig.mode,
                        signingState = savedConfig.signingState,
                        overwrite = savedConfig.overwrite,
                        aapt2Path = savedConfig.aapt2Path,
                        connectedDevice = savedConfig.connectedDevice,
                        deviceId = savedConfig.deviceId,
                        deviceSpecPath = savedConfig.deviceSpecPath,
                        specSuggestions = suggestions,
                        localTesting = savedConfig.localTesting
                    )
                }
            } else {
                log.warn("No saved bundle tool configuration found.")
                _uiState.update { it.copy(specSuggestions = suggestions) }
            }
        }
    }

    private fun getSpecSuggestions(): List<String> {
        val defaultSpecsDir = java.io.File(System.getProperty("user.home"), ".bundletool_ui/specs")
        return if (defaultSpecsDir.exists()) {
            defaultSpecsDir.listFiles()?.filter { it.extension == "json" }?.map { it.absolutePath } ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun persistCurrentState() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = uiState.value
            val existingConfig = appPreferences.getBundleToolConfig()
            
            val config = PersistedBundleToolConfig(
                bundleToolPath = state.bundleToolPath,
                adbPath = existingConfig?.adbPath ?: "",
                aabPath = state.aabPath,
                outputDir = state.outputDir,
                mode = state.mode,
                signingState = state.signingState,
                overwrite = state.overwrite,
                aapt2Path = state.aapt2Path,
                connectedDevice = state.connectedDevice,
                deviceId = state.deviceId,
                deviceSpecPath = state.deviceSpecPath,
                localTesting = state.localTesting
            )

            log.debug("Persisting current config: {}", config)
            appPreferences.saveBundleToolConfig(config)
        }
    }
}