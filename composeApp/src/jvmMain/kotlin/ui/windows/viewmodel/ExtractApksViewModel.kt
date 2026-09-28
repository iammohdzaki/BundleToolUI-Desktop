package ui.windows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import command.ExtractApksCommand
import data.domain.CommandResult
import data.state.ExtractApksEvent
import data.state.ExtractApksState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.AppPreferences
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import usecase.CommandUseCase
import java.io.File

class ExtractApksViewModel(
    private val commandUseCase: CommandUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val log: Logger = LoggerFactory.getLogger(ExtractApksViewModel::class.java)

    private val _uiState = MutableStateFlow(ExtractApksState())
    val uiState: StateFlow<ExtractApksState> = _uiState

    fun onEvent(event: ExtractApksEvent) {
        when (event) {
            is ExtractApksEvent.Initialize -> loadPersistedConfig()
            is ExtractApksEvent.SelectBundleToolPath -> {
                _uiState.update { it.copy(bundleToolPath = event.path) }
                persistBundleToolPath(event.path)
                checkReady()
            }
            is ExtractApksEvent.SelectApksFile -> {
                _uiState.update { it.copy(apksPath = event.path) }
                checkReady()
            }
            is ExtractApksEvent.SelectOutputDir -> {
                _uiState.update { it.copy(outputDir = event.path) }
                checkReady()
            }
            is ExtractApksEvent.SelectDeviceSpec -> {
                _uiState.update { it.copy(deviceSpecPath = event.path) }
                checkReady()
            }
            is ExtractApksEvent.Extract -> extractApks()
        }
    }

    private fun loadPersistedConfig() {
        viewModelScope.launch {
            val config = appPreferences.getBundleToolConfig()
            val suggestions = getSpecSuggestions()
            if (config != null) {
                _uiState.update { 
                    it.copy(
                        bundleToolPath = config.bundleToolPath,
                        specSuggestions = suggestions
                    )
                }
                checkReady()
            } else {
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

    private fun persistBundleToolPath(path: String) {
        viewModelScope.launch {
            val config = appPreferences.getBundleToolConfig() ?: data.state.PersistedBundleToolConfig()
            appPreferences.saveBundleToolConfig(config.copy(bundleToolPath = path))
        }
    }

    private fun checkReady() {
        _uiState.update { state ->
            val ready = state.bundleToolPath.isNotBlank() &&
                    File(state.bundleToolPath).exists() &&
                    state.apksPath.isNotBlank() &&
                    File(state.apksPath).exists() &&
                    state.outputDir.isNotBlank()
            state.copy(isReady = ready)
        }
    }

    private fun extractApks() {
        if (!_uiState.value.isReady) return

        _uiState.update { it.copy(isExtracting = true, successOutputPath = null, log = "Starting APKs extraction...\n") }
        val state = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val config = ExtractApksCommand.Config(
                    bundleToolPath = state.bundleToolPath,
                    apksPath = state.apksPath,
                    outputDir = state.outputDir,
                    deviceSpecPath = state.deviceSpecPath.takeIf { it.isNotBlank() }
                )
                
                val command = ExtractApksCommand(config)
                val (commandExecuted, result) = commandUseCase.executeBundleTool(command)

                log.info("Executing command: {}", commandExecuted)
                _uiState.update { it.copy(log = it.log + "\n> $commandExecuted\n") }

                when (result) {
                    is CommandResult.Success -> {
                        _uiState.update {
                            it.copy(
                                log = it.log + "\nSuccess (took ${result.durationMs}ms):\n${result.output}\n",
                                successOutputPath = config.outputDir,
                                isExtracting = false
                            )
                        }
                    }
                    is CommandResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                log = it.log + "\nError:\n${result.error}\n",
                                isExtracting = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to execute", e)
                _uiState.update {
                    it.copy(
                        log = it.log + "\nUnexpected Error:\n${e.message}\n",
                        isExtracting = false
                    )
                }
            }
        }
    }
}
