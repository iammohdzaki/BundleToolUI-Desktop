package data.state

data class GetDeviceSpecState(
    val bundleToolPath: String = "",
    val adbPath: String = "",
    val outputJsonPath: String = "",
    val deviceId: String = "",
    val isReady: Boolean = false,
    val isExecuting: Boolean = false,
    val successOutputPath: String? = null,
    val log: String = ""
)

sealed interface GetDeviceSpecEvent {
    object Initialize : GetDeviceSpecEvent
    data class SelectOutputFile(val path: String) : GetDeviceSpecEvent
    data class SetDeviceId(val id: String, val model: String = "Unknown") : GetDeviceSpecEvent
    object GenerateSpec : GetDeviceSpecEvent
}
