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
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.onDownload
import kotlin.time.Duration.Companion.milliseconds

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
            kotlinx.coroutines.delay(500.milliseconds)
            
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
                
                val client = io.ktor.client.HttpClient(io.ktor.client.engine.cio.CIO) {
                    install(io.ktor.client.plugins.HttpTimeout) {
                        requestTimeoutMillis = 60000
                        connectTimeoutMillis = 15000
                        socketTimeoutMillis = 60000
                    }
                }
                
                try {
                    val response = client.get("https://api.github.com/repos/google/bundletool/releases/latest") {
                        header("Accept", "application/vnd.github.v3+json")
                    }.bodyAsText()
                    
                    val urlRegex = "\"browser_download_url\":\\s*\"([^\"]+\\.jar)\"".toRegex()
                    val matchResult = urlRegex.find(response)
                    
                    val downloadUrl = matchResult?.groups?.get(1)?.value ?: throw Exception("Could not find .jar download URL.")
                    val fileName = downloadUrl.substringAfterLast("/")
                    
                    val appDir = File(System.getProperty("user.home"), ".bundletool_ui")
                    if (!appDir.exists()) appDir.mkdirs()
                    
                    val outputFile = File(appDir, fileName)
                    
                    _uiState.update { it.copy(message = "Downloading $fileName...", downloadProgress = 0.1f) }
                    
                    val bytes = client.get(downloadUrl) {
                        onDownload { bytesSentTotal, contentLength ->
                            if (contentLength != null && contentLength > 0) {
                                val progress = (bytesSentTotal.toFloat() / contentLength.toFloat()) * 0.9f + 0.1f
                                _uiState.update { it.copy(downloadProgress = progress) }
                            }
                        }
                    }.readRawBytes()
                    
                    outputFile.writeBytes(bytes)
                    
                    val absolutePath = outputFile.absolutePath
                    val config = appPreferences.getBundleToolConfig() ?: data.state.PersistedBundleToolConfig()
                    appPreferences.saveBundleToolConfig(config.copy(bundleToolPath = absolutePath))
                    
                    _uiState.update { it.copy(state = SplashState.DONE) }
                } finally {
                    client.close()
                }
                
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
