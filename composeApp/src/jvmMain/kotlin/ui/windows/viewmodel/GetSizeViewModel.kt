package ui.windows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import command.GetSizeCommand
import data.domain.CommandResult
import data.state.GetSizeEvent
import data.state.GetSizeState
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

class GetSizeViewModel(
    private val commandUseCase: CommandUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val log: Logger = LoggerFactory.getLogger(GetSizeViewModel::class.java)

    private val _uiState = MutableStateFlow(GetSizeState())
    val uiState: StateFlow<GetSizeState> = _uiState

    fun onEvent(event: GetSizeEvent) {
        when (event) {
            is GetSizeEvent.Initialize -> loadPersistedConfig()
            is GetSizeEvent.SelectApksFile -> {
                val modules = extractModulesFromApks(event.path)
                _uiState.update { 
                    it.copy(
                        apksPath = event.path,
                        availableModules = modules,
                        // If there's only base, maybe pre-select it or leave empty. 
                        // Empty modules string means default bundletool behavior (all install time modules).
                        modules = "" 
                    )
                }
                checkReady()
            }
            is GetSizeEvent.SelectDeviceSpec -> {
                _uiState.update { it.copy(deviceSpecPath = event.path) }
            }
            is GetSizeEvent.SetModules -> {
                _uiState.update { it.copy(modules = event.modules) }
            }
            is GetSizeEvent.SetDimensions -> {
                _uiState.update { it.copy(dimensions = event.dimensions) }
            }
            is GetSizeEvent.CalculateSize -> calculateSize()
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

    private fun extractModulesFromApks(path: String): List<String> {
        try {
            val file = File(path)
            if (!file.exists() || !file.name.endsWith(".apks")) return emptyList()

            val modules = mutableSetOf<String>()
            java.util.zip.ZipFile(file).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    // APKs are typically in splits/ or standalones/
                    if ((name.startsWith("splits/") || name.startsWith("standalones/")) && name.endsWith(".apk")) {
                        val fileName = name.substringAfterLast("/")
                        // filename format is usually moduleName-qualifier.apk (e.g. base-master.apk, feature1-en.apk)
                        val moduleName = fileName.substringBefore("-")
                        if (moduleName.isNotBlank() && moduleName != fileName) { // ensuring there was a "-"
                            modules.add(moduleName)
                        } else if (moduleName.isNotBlank() && moduleName.endsWith(".apk")) {
                             // Fallback if it's just 'base.apk'
                            modules.add(moduleName.removeSuffix(".apk"))
                        }
                    }
                }
            }
            return modules.toList().sorted()
        } catch (e: Exception) {
            log.error("Failed to parse APKS for modules", e)
            return emptyList()
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

    private fun calculateSize() {
        if (!_uiState.value.isReady) return

        _uiState.update { it.copy(isExecuting = true, log = "Calculating total size...\n", parsedSizeResults = null) }
        val state = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val config = GetSizeCommand.Config(
                    bundleToolPath = state.bundleToolPath,
                    apksPath = state.apksPath,
                    deviceSpecPath = state.deviceSpecPath.takeIf { it.isNotBlank() },
                    modules = state.modules.takeIf { it.isNotBlank() },
                    dimensions = state.dimensions.takeIf { it.isNotBlank() && it != "ALL" }
                )
                
                val command = GetSizeCommand(config)
                val (commandExecuted, result) = commandUseCase.executeBundleTool(command)

                log.info("Executing command: {}", commandExecuted)
                _uiState.update { it.copy(log = it.log + "\n> $commandExecuted\n") }

                when (result) {
                    is CommandResult.Success -> {
                        val parsed = parseSizeOutput(result.output)
                        _uiState.update {
                            it.copy(
                                log = it.log + "\nResult (took ${result.durationMs}ms):\n${result.output}\n",
                                isExecuting = false,
                                parsedSizeResults = parsed
                            )
                        }
                    }
                    is CommandResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                log = it.log + "\nError:\n${result.error}\n",
                                isExecuting = false,
                                parsedSizeResults = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to execute", e)
                _uiState.update {
                    it.copy(
                        log = it.log + "\nUnexpected Error:\n${e.message}\n",
                        isExecuting = false,
                        parsedSizeResults = null
                    )
                }
            }
        }
    }

    private fun parseSizeOutput(output: String): List<data.state.SizeResultRow>? {
        try {
            val allLines = output.lines().map { it.trim() }.filter { it.isNotBlank() }
            
            // Find the line that actually contains the CSV headers
            val headerIndex = allLines.indexOfFirst { it.contains("MIN") && it.contains("MAX") }
            if (headerIndex == -1) return null
            
            val lines = allLines.drop(headerIndex)
            if (lines.size < 2) return null
            
            val headers = splitCsvRow(lines.first())
            val minIndex = headers.indexOf("MIN")
            val maxIndex = headers.indexOf("MAX")
            
            if (minIndex == -1 || maxIndex == -1) return null
            
            val results = mutableListOf<data.state.SizeResultRow>()
            
            for (i in 1 until lines.size) {
                val values = splitCsvRow(lines[i])
                if (values.size != headers.size) continue
                
                val minBytes = values[minIndex].toLongOrNull() ?: continue
                val maxBytes = values[maxIndex].toLongOrNull() ?: continue
                
                val dimensionsMap = mutableMapOf<String, String>()
                for (j in headers.indices) {
                    if (j != minIndex && j != maxIndex) {
                        dimensionsMap[headers[j]] = values[j]
                    }
                }
                
                results.add(data.state.SizeResultRow(dimensionsMap, minBytes, maxBytes))
            }
            return results.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            log.error("Failed to parse size output", e)
            return null
        }
    }

    private fun splitCsvRow(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = java.lang.StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            if (char == '\"') {
                inQuotes = !inQuotes
            } else if (char == ',' && !inQuotes) {
                result.add(current.toString().trim())
                current = java.lang.StringBuilder()
            } else {
                current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}
