package ui.windows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.state.SettingsEvent
import data.state.SettingsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.AppPreferences
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class SettingsViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val log = LoggerFactory.getLogger(SettingsViewModel::class.java)
    
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.Initialize -> loadPersistedConfig()
            is SettingsEvent.SelectBundleToolPath -> {
                _uiState.update { it.copy(bundleToolPath = event.path) }
                persistBundleToolPath(event.path)
            }
            is SettingsEvent.SelectAdbPath -> {
                _uiState.update { it.copy(adbPath = event.path) }
                persistAdbPath(event.path)
            }
            is SettingsEvent.DownloadBundleTool -> downloadBundleTool()
        }
    }

    private fun loadPersistedConfig() {
        viewModelScope.launch {
            val config = appPreferences.getBundleToolConfig()
            if (config != null) {
                _uiState.update { it.copy(bundleToolPath = config.bundleToolPath, adbPath = config.adbPath) }
            }
        }
    }

    private fun persistBundleToolPath(path: String) {
        viewModelScope.launch {
            val config = appPreferences.getBundleToolConfig() ?: data.state.PersistedBundleToolConfig()
            appPreferences.saveBundleToolConfig(config.copy(bundleToolPath = path))
        }
    }

    private fun persistAdbPath(path: String) {
        viewModelScope.launch {
            val config = appPreferences.getBundleToolConfig() ?: data.state.PersistedBundleToolConfig()
            appPreferences.saveBundleToolConfig(config.copy(adbPath = path))
        }
    }

    private fun downloadBundleTool() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isDownloading = true, downloadMessage = "Fetching latest release info...", downloadProgress = 0f) }
                
                val apiUrl = URL("https://api.github.com/repos/google/bundletool/releases/latest")
                val connection = apiUrl.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Failed to fetch release info: HTTP ${connection.responseCode}")
                }
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                
                // Extract browser_download_url
                val urlRegex = "\"browser_download_url\":\\s*\"([^\"]+\\.jar)\"".toRegex()
                val matchResult = urlRegex.find(response)
                
                val downloadUrl = matchResult?.groups?.get(1)?.value ?: throw Exception("Could not find .jar download URL.")
                val fileName = downloadUrl.substringAfterLast("/")
                
                val appDir = File(System.getProperty("user.home"), ".bundletool_ui")
                if (!appDir.exists()) appDir.mkdirs()
                
                val outputFile = File(appDir, fileName)
                
                _uiState.update { it.copy(downloadMessage = "Downloading $fileName...", downloadProgress = 0.1f) }
                
                val fileUrl = URL(downloadUrl)
                val fileConn = fileUrl.openConnection() as HttpURLConnection
                fileConn.connectTimeout = 5000
                fileConn.readTimeout = 5000
                val fileLength = fileConn.contentLength
                
                fileConn.inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buffer = ByteArray(4096)
                        var bytesCopied = 0L
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesCopied += bytesRead
                            if (fileLength > 0) {
                                val progress = (bytesCopied.toFloat() / fileLength.toFloat()) * 0.9f + 0.1f
                                _uiState.update { it.copy(downloadProgress = progress) }
                            }
                        }
                    }
                }
                
                val absolutePath = outputFile.absolutePath
                _uiState.update { 
                    it.copy(
                        isDownloading = false, 
                        downloadMessage = "Downloaded successfully!", 
                        bundleToolPath = absolutePath,
                        downloadProgress = 1f
                    )
                }
                persistBundleToolPath(absolutePath)
                
            } catch (e: Exception) {
                log.error("Failed to download BundleTool", e)
                val msg = if (e is java.net.ConnectException) {
                    "Connection timed out. Please check your internet or firewall, or download manually from https://github.com/google/bundletool/releases"
                } else {
                    "Error: ${e.message}"
                }
                
                _uiState.update { 
                    it.copy(
                        isDownloading = false, 
                        downloadMessage = msg,
                        downloadProgress = 0f
                    )
                }
            }
        }
    }
}
