package manager

import command.ICommandExecutor
import command.OSProcessExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.AppPreferences
import utils.logger
import java.io.File

data class AdbDevice(
    val id: String,
    val status: String,
    val model: String
)

class AdbManager(
    private val appPreferences: AppPreferences,
    private val commandExecutor: ICommandExecutor
) {
    private val log = logger()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _devices = MutableStateFlow<List<AdbDevice>>(emptyList())
    val devices: StateFlow<List<AdbDevice>> = _devices
    
    private val _isAdbConfigured = MutableStateFlow(false)
    val isAdbConfigured: StateFlow<Boolean> = _isAdbConfigured
    
    init {
        startPolling()
    }
    
    private fun startPolling() {
        scope.launch {
            while (true) {
                fetchDevices()
                delay(3000) // poll every 3 seconds
            }
        }
    }
    
    private suspend fun fetchDevices() {
        val config = appPreferences.getBundleToolConfig()
        val adbPath = config?.adbPath?.takeIf { it.isNotBlank() }
        
        if (adbPath == null || !File(adbPath).exists()) {
            _isAdbConfigured.update { false }
            _devices.update { emptyList() }
            return
        }
        
        _isAdbConfigured.update { true }
        
        try {
            val result = commandExecutor.execute("\"$adbPath\" devices -l")
            
            if (result is data.domain.CommandResult.Success) {
                val parsedDevices = parseDevices(result.output)
                _devices.update { parsedDevices }
            } else {
                _devices.update { emptyList() }
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch ADB devices: ${e.message}")
            _devices.update { emptyList() }
        }
    }
    
    private fun parseDevices(output: String): List<AdbDevice> {
        val lines = output.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty() || !lines[0].startsWith("List of devices")) {
            return emptyList()
        }
        
        val deviceList = mutableListOf<AdbDevice>()
        for (i in 1 until lines.size) {
            val line = lines[i]
            // Format: ID  STATUS  product:... model:... device:... transport_id:...
            val parts = line.split(Regex("\\s+"))
            if (parts.size >= 2) {
                val id = parts[0]
                val status = parts[1]
                
                // Extract model if present
                var model = "Unknown"
                for (part in parts) {
                    if (part.startsWith("model:")) {
                        model = part.removePrefix("model:")
                        break
                    }
                }
                
                deviceList.add(AdbDevice(id, status, model.replace("_", " ")))
            }
        }
        return deviceList
    }
}
