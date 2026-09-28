package ui.windows.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mcp.McpServer
import mcp.McpActionHandler
import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import usecase.CommandUseCase

import local.AppPreferences
import command.BuildApksCommand
import command.InstallApksCommand
import command.GetDeviceSpecCommand
import command.GetSizeCommand
import data.domain.CommandResult

data class McpState(
    val isRunning: Boolean = false,
    val logs: List<String> = emptyList(),
    val port: String = "8123",
    val configSnippet: String = ""
)

class McpViewModel(
    private val appPreferences: AppPreferences,
    private val commandUseCase: CommandUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(McpState())
    val uiState: StateFlow<McpState> = _uiState.asStateFlow()

    init {
        updateConfigSnippet("8123")
    }

    private val mcpHandler = object : McpActionHandler {
        private suspend fun getBundleToolPath(): String {
            val config = appPreferences.getBundleToolConfig()
            return config?.bundleToolPath ?: throw Exception("BundleTool path not configured in Settings.")
        }

        override suspend fun buildApks(aabPath: String, outputDir: String, mode: String, localTesting: Boolean): String {
            addLog("Executing internal buildApks... ($aabPath)")
            val config = BuildApksCommand.Config(
                bundleToolPath = getBundleToolPath(),
                aabPath = aabPath,
                outputDir = outputDir,
                isUniversal = (mode == "universal"),
                localTesting = localTesting
            )
            val result = commandUseCase.executeBundleTool(BuildApksCommand(config))
            return if (result.result is CommandResult.Success) {
                "APK built successfully.\nOutput: ${result.result.output}"
            } else {
                val failure = result.result as CommandResult.Failure
                throw Exception("Failed to build APKs: ${failure.error}")
            }
        }

        override suspend fun installApks(apksPath: String, deviceId: String?): String {
            addLog("Executing internal installApks... ($apksPath)")
            val config = InstallApksCommand.Config(
                bundleToolPath = getBundleToolPath(),
                apksPath = apksPath,
                deviceId = deviceId
            )
            val result = commandUseCase.executeBundleTool(InstallApksCommand(config))
            return if (result.result is CommandResult.Success) {
                "APK installed successfully.\nOutput: ${result.result.output}"
            } else {
                val failure = result.result as CommandResult.Failure
                throw Exception("Failed to install APKs: ${failure.error}")
            }
        }

        override suspend fun getSize(apksPath: String, dimensions: String?): String {
            addLog("Executing internal getSize... ($apksPath)")
            val config = GetSizeCommand.Config(
                bundleToolPath = getBundleToolPath(),
                apksPath = apksPath,
                dimensions = dimensions
            )
            val result = commandUseCase.executeBundleTool(GetSizeCommand(config))
            return if (result.result is CommandResult.Success) {
                "Size data:\n${result.result.output}"
            } else {
                val failure = result.result as CommandResult.Failure
                throw Exception("Failed to get size: ${failure.error}")
            }
        }

        override suspend fun getDeviceSpec(deviceId: String?, outputPath: String): String {
            addLog("Executing internal getDeviceSpec...")
            val config = GetDeviceSpecCommand.Config(
                bundleToolPath = getBundleToolPath(),
                outputJsonPath = outputPath,
                deviceId = deviceId
            )
            val result = commandUseCase.executeBundleTool(GetDeviceSpecCommand(config))
            return if (result.result is CommandResult.Success) {
                "Device spec generated successfully.\nOutput: ${result.result.output}"
            } else {
                val failure = result.result as CommandResult.Failure
                throw Exception("Failed to generate device spec: ${failure.error}")
            }
        }
    }

    fun toggleServer() {
        val currentState = _uiState.value
        if (currentState.isRunning) {
            McpServer.stop()
            _uiState.value = currentState.copy(isRunning = false)
            addLog("Server stopped.")
        } else {
            val portInt = currentState.port.toIntOrNull() ?: 8123
            McpServer.logListener = { log ->
                addLog(log)
            }
            McpServer.start(port = portInt, handler = mcpHandler)
            _uiState.value = currentState.copy(isRunning = true)
            updateConfigSnippet(portInt.toString())
        }
    }

    fun updatePort(newPort: String) {
        if (newPort.all { it.isDigit() } && newPort.length <= 5) {
            _uiState.value = _uiState.value.copy(port = newPort)
            updateConfigSnippet(newPort)
        }
    }

    fun clearLogs() {
        _uiState.value = _uiState.value.copy(logs = emptyList())
    }

    private fun addLog(message: String) {
        // Append log and keep last 100 entries
        val newLogs = (_uiState.value.logs + message).takeLast(100)
        _uiState.value = _uiState.value.copy(logs = newLogs)
    }

    private fun updateConfigSnippet(port: String) {
        val snippet = """
            "mcpServers": {
              "bundletool-mcp": {
                "command": "npx",
                "args": ["-y", "supergateway", "--sse", "http://localhost:$port/mcp/sse"]
              }
            }
        """.trimIndent()
        _uiState.value = _uiState.value.copy(configSnippet = snippet)
    }
}
