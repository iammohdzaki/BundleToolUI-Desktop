package ui.windows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import command.InstallApksCommand
import data.domain.CommandResult
import data.state.InstallApksEvent
import data.state.InstallApksState
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

class InstallApksViewModel(
    private val commandUseCase: CommandUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val log: Logger = LoggerFactory.getLogger(InstallApksViewModel::class.java)

    private val _uiState = MutableStateFlow(InstallApksState())
    val uiState: StateFlow<InstallApksState> = _uiState

    fun onEvent(event: InstallApksEvent) {
        when (event) {
            is InstallApksEvent.Initialize -> loadPersistedConfig()
            is InstallApksEvent.SelectApksFile -> {
                _uiState.update { it.copy(apksPath = event.path) }
                checkReady()
            }
            is InstallApksEvent.SetDeviceId -> {
                _uiState.update { it.copy(deviceId = event.id) }
            }
            is InstallApksEvent.Install -> installApks()
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
            val ready = state.bundleToolPath.isNotBlank() &&
                    File(state.bundleToolPath).exists() &&
                    state.apksPath.isNotBlank() &&
                    File(state.apksPath).exists()
            state.copy(isReady = ready)
        }
    }

    private fun installApks() {
        if (!_uiState.value.isReady) return

        _uiState.update { it.copy(isInstalling = true, log = "Starting APK installation to device...\n") }
        val state = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val config = InstallApksCommand.Config(
                    bundleToolPath = state.bundleToolPath,
                    adbPath = state.adbPath,
                    apksPath = state.apksPath,
                    deviceId = state.deviceId
                )
                
                val command = InstallApksCommand(config)
                val (commandExecuted, result) = commandUseCase.executeBundleTool(command)

                log.info("Executing command: {}", commandExecuted)
                _uiState.update { it.copy(log = it.log + "\n> $commandExecuted\n") }

                when (result) {
                    is CommandResult.Success -> {
                        _uiState.update {
                            it.copy(
                                log = it.log + "\nSuccess (took ${result.durationMs}ms):\n${result.output}\n",
                                isInstalling = false
                            )
                        }
                    }
                    is CommandResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                log = it.log + "\nError:\n${result.error}\n",
                                isInstalling = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to execute", e)
                _uiState.update {
                    it.copy(
                        log = it.log + "\nUnexpected Error:\n${e.message}\n",
                        isInstalling = false
                    )
                }
            }
        }
    }
}
