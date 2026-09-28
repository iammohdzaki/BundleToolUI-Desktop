package ui.windows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import command.GetDeviceSpecCommand
import data.domain.CommandResult
import data.state.GetDeviceSpecEvent
import data.state.GetDeviceSpecState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.AppPreferences
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import usecase.CommandUseCase

class GetDeviceSpecViewModel(
    private val commandUseCase: CommandUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val log: Logger = LoggerFactory.getLogger(GetDeviceSpecViewModel::class.java)

    private val _uiState = MutableStateFlow(GetDeviceSpecState())
    val uiState: StateFlow<GetDeviceSpecState> = _uiState

    fun onEvent(event: GetDeviceSpecEvent) {
        when (event) {
            is GetDeviceSpecEvent.Initialize -> loadPersistedConfig()
            is GetDeviceSpecEvent.SelectOutputFile -> {
                _uiState.update { it.copy(outputJsonPath = event.path) }
                checkReady()
            }
            is GetDeviceSpecEvent.SetDeviceId -> {
                val sanitizedModel = event.model.replace(Regex("[^a-zA-Z0-9_-]"), "_").trim('_')
                val defaultSpecsDir = java.io.File(System.getProperty("user.home"), ".bundletool_ui/specs").apply { mkdirs() }
                val suggestedFile = java.io.File(defaultSpecsDir, "${sanitizedModel}_spec.json").absolutePath
                
                _uiState.update { 
                    it.copy(
                        deviceId = event.id,
                        outputJsonPath = suggestedFile
                    )
                }
                checkReady()
            }
            is GetDeviceSpecEvent.GenerateSpec -> generateSpec()
        }
    }

    private fun loadPersistedConfig() {
        viewModelScope.launch {
            val config = appPreferences.getBundleToolConfig()
            if (config != null) {
                _uiState.update { 
                    it.copy(
                        bundleToolPath = config.bundleToolPath,
                        adbPath = config.adbPath
                    )
                }
                checkReady()
            }
        }
    }

    private fun checkReady() {
        _uiState.update { state ->
            val ready = state.bundleToolPath.isNotBlank() && state.outputJsonPath.isNotBlank()
            state.copy(isReady = ready)
        }
    }

    private fun generateSpec() {
        if (!_uiState.value.isReady) return

        _uiState.update { it.copy(isExecuting = true, successOutputPath = null, log = "Generating device spec...\n") }
        val state = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val config = GetDeviceSpecCommand.Config(
                    bundleToolPath = state.bundleToolPath,
                    adbPath = state.adbPath,
                    outputJsonPath = state.outputJsonPath,
                    deviceId = state.deviceId
                )
                
                val outputFile = java.io.File(config.outputJsonPath)
                if (outputFile.exists()) {
                    _uiState.update {
                        it.copy(
                            log = it.log + "\nDevice spec already exists at:\n${config.outputJsonPath}\nSkipping generation.\n",
                            successOutputPath = outputFile.parent,
                            isExecuting = false
                        )
                    }
                    return@launch
                }
                
                val command = GetDeviceSpecCommand(config)
                val (commandExecuted, result) = commandUseCase.executeBundleTool(command)

                log.info("Executing command: {}", commandExecuted)
                _uiState.update { it.copy(log = it.log + "\n> $commandExecuted\n") }

                when (result) {
                    is CommandResult.Success -> {
                        _uiState.update {
                            it.copy(
                                log = it.log + "\nSuccess (took ${result.durationMs}ms):\n${result.output}\n",
                                successOutputPath = java.io.File(config.outputJsonPath).parent,
                                isExecuting = false
                            )
                        }
                    }
                    is CommandResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                log = it.log + "\nError:\n${result.error}\n",
                                isExecuting = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to execute", e)
                _uiState.update {
                    it.copy(
                        log = it.log + "\nUnexpected Error:\n${e.message}\n",
                        isExecuting = false
                    )
                }
            }
        }
    }
}
