package data.state

import kotlinx.serialization.Serializable

@Serializable
data class PersistedBundleToolConfig(
    val bundleToolPath: String = "",
    val adbPath: String = "",
    val aabPath: String = "",
    val outputDir: String = "",
    val mode: OutputMode = OutputMode.Universal,
    val signingState: SigningState = SigningState(),
    val overwrite: Boolean = false,
    val aapt2Path: String = "",
    val connectedDevice: Boolean = false,
    val deviceId: String = "",
    val deviceSpecPath: String = "",
    val localTesting: Boolean = false
)

data class BundleToolState(
    val aabPath: String = "",
    val bundleToolPath: String = "",
    val outputDir: String = "",
    val signingState: SigningState = SigningState(),
    val mode: OutputMode = OutputMode.Universal,
    val overwrite: Boolean = false,
    val aapt2Path: String = "",
    val connectedDevice: Boolean = false,
    val deviceId: String = "",
    val deviceSpecPath: String = "",
    val specSuggestions: List<String> = emptyList(),
    val localTesting: Boolean = false,
    val isConverting: Boolean = false,
    val successOutputPath: String? = null,
    val log: String = "Waiting to start conversion..."
) {
    val isReady get() = aabPath.isNotBlank()
}

@Serializable
data class SigningState(
    val signingMode: SigningMode = SigningMode.Debug,
    val keystorePath: String = "",
    val keystorePassword: String = "",
    val keyAlias: String = "",
    val keyPassword: String = ""
)

sealed class BundleToolEvent {
    data object Initialize : BundleToolEvent()
    data class SelectAabFile(val path: String) : BundleToolEvent()
    data class SelectBundleToolPath(val path: String) : BundleToolEvent()
    data class SelectOutputDir(val path: String) : BundleToolEvent()
    data class SelectSigning(val signingMode: SigningMode) : BundleToolEvent()
    data class SelectKeyStore(val path: String) : BundleToolEvent()
    data class SelectKeyStorePassword(val path: String) : BundleToolEvent()
    data class SelectAlias(val alias: String) : BundleToolEvent()
    data class SelectKeyPassword(val password: String) : BundleToolEvent()
    data class SelectMode(val mode: OutputMode) : BundleToolEvent()
    data class SetOverwrite(val overwrite: Boolean) : BundleToolEvent()
    data class SelectAapt2Path(val path: String) : BundleToolEvent()
    data class SetConnectedDevice(val connectedDevice: Boolean) : BundleToolEvent()
    data class SetDeviceId(val deviceId: String) : BundleToolEvent()
    data class SelectDeviceSpecPath(val path: String) : BundleToolEvent()
    data class SetLocalTesting(val localTesting: Boolean) : BundleToolEvent()
    data object Convert : BundleToolEvent()
}

enum class OutputMode { Universal, ApkSet, DeviceSpecific }

enum class SigningMode { Debug, Release }
