package data.state

data class SettingsState(
    val bundleToolPath: String = "",
    val adbPath: String = "",
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadMessage: String = ""
)

sealed interface SettingsEvent {
    object Initialize : SettingsEvent
    data class SelectBundleToolPath(val path: String) : SettingsEvent
    data class SelectAdbPath(val path: String) : SettingsEvent
    object DownloadBundleTool : SettingsEvent
}
