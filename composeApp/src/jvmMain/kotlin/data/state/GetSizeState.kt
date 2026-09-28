package data.state

data class SizeResultRow(
    val dimensions: Map<String, String>,
    val minBytes: Long,
    val maxBytes: Long
)

data class GetSizeState(
    val bundleToolPath: String = "",
    val apksPath: String = "",
    val deviceSpecPath: String = "",
    val modules: String = "",
    val dimensions: String = "ALL",
    val availableModules: List<String> = emptyList(),
    val isReady: Boolean = false,
    val isExecuting: Boolean = false,
    val log: String = "",
    val specSuggestions: List<String> = emptyList(),
    val parsedSizeResults: List<SizeResultRow>? = null
)

sealed interface GetSizeEvent {
    object Initialize : GetSizeEvent
    data class SelectApksFile(val path: String) : GetSizeEvent
    data class SelectDeviceSpec(val path: String) : GetSizeEvent
    data class SetModules(val modules: String) : GetSizeEvent
    data class SetDimensions(val dimensions: String) : GetSizeEvent
    object CalculateSize : GetSizeEvent
}
