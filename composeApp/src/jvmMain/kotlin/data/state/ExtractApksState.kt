package data.state

data class ExtractApksState(
    val bundleToolPath: String = "",
    val apksPath: String = "",
    val outputDir: String = "",
    val deviceSpecPath: String = "",
    val isReady: Boolean = false,
    val isExtracting: Boolean = false,
    val successOutputPath: String? = null,
    val log: String = "",
    val specSuggestions: List<String> = emptyList()
)

sealed interface ExtractApksEvent {
    object Initialize : ExtractApksEvent
    data class SelectBundleToolPath(val path: String) : ExtractApksEvent
    data class SelectApksFile(val path: String) : ExtractApksEvent
    data class SelectOutputDir(val path: String) : ExtractApksEvent
    data class SelectDeviceSpec(val path: String) : ExtractApksEvent
    object Extract : ExtractApksEvent
}
