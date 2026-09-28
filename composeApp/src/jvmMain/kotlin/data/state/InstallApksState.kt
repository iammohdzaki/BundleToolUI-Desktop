package data.state

data class InstallApksState(
    val bundleToolPath: String = "",
    val adbPath: String = "",
    val apksPath: String = "",
    val deviceId: String = "",
    val isInstalling: Boolean = false,
    val isReady: Boolean = false,
    val log: String = ""
)

sealed interface InstallApksEvent {
    object Initialize : InstallApksEvent
    data class SelectApksFile(val path: String) : InstallApksEvent
    data class SetDeviceId(val id: String) : InstallApksEvent
    object Install : InstallApksEvent
}
