package ui.windows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

enum class SplashState {
    CHECKING,
    NEEDS_SETUP,
    DOWNLOADING,
    DONE
}

data class SplashUiState(
    val state: SplashState = SplashState.CHECKING,
    val downloadProgress: Float = 0f,
    val message: String = ""
)

class SplashViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val log = LoggerFactory.getLogger(SplashViewModel::class.java)

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        checkBundleTool()
    }

    private fun checkBundleTool() {
        viewModelScope.launch {
            // Enforce minimum splash screen duration so it's always visible
            kotlinx.coroutines.delay(1500)
            
            val config = appPreferences.getBundleToolConfig()
            val path = config?.bundleToolPath
            if (!path.isNullOrBlank() && File(path).exists()) {
                _uiState.update { it.copy(state = SplashState.DONE) }
            } else {
                _uiState.update { it.copy(state = SplashState.NEEDS_SETUP) }
            }
        }
    }

    fun downloadBundleTool() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { 
                    it.copy(
                        state = SplashState.DOWNLOADING,
                        message = "Connecting to GitHub...",
                        downloadProgress = 0f
                    )
                }
                
                val apiUrl = URL("https://api.github.com/repos/google/bundletool/releases/latest")
                val connection = apiUrl.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 5000 // 5 seconds timeout
                connection.readTimeout = 5000
                
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Failed to fetch release info: HTTP ${connection.responseCode}")
                }
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                
                val urlRegex = "\"browser_download_url\":\\s*\"([^\"]+\\.jar)\"".toRegex()
                val matchResult = urlRegex.find(response)
                
                val downloadUrl = matchResult?.groups?.get(1)?.value ?: throw Exception("Could not find .jar download URL.")
                val fileName = downloadUrl.substringAfterLast("/")
                
                val appDir = File(System.getProperty("user.home"), ".bundletool_ui")
                if (!appDir.exists()) appDir.mkdirs()
                
                val outputFile = File(appDir, fileName)
                
                _uiState.update { it.copy(message = "Downloading $fileName...", downloadProgress = 0.1f) }
                
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
                val config = appPreferences.getBundleToolConfig() ?: data.state.PersistedBundleToolConfig()
                appPreferences.saveBundleToolConfig(config.copy(bundleToolPath = absolutePath))
                
                _uiState.update { it.copy(state = SplashState.DONE) }
                
            } catch (e: Exception) {
                log.error("Failed to download BundleTool", e)
                val msg = if (e is java.net.ConnectException) {
                    "Connection timed out. Please check your internet or firewall, or download it manually."
                } else {
                    "Error: ${e.message}. Please try downloading manually."
                }
                
                _uiState.update { 
                    it.copy(
                        state = SplashState.NEEDS_SETUP, 
                        message = msg
                    )
                }
            }
        }
    }

    fun selectLocalFile(path: String) {
        viewModelScope.launch {
            val config = appPreferences.getBundleToolConfig() ?: data.state.PersistedBundleToolConfig()
            appPreferences.saveBundleToolConfig(config.copy(bundleToolPath = path))
            _uiState.update { it.copy(state = SplashState.DONE) }
        }
    }
}
