package data.state

import kotlinx.serialization.Serializable

@Serializable
data class PersistedBundleToolConfig(
    val bundleToolPath: String = "",
    val aabPath: String = "",
    val outputDir: String = "",
    val mode: OutputMode = OutputMode.Universal,
    val signingState: SigningState = SigningState()
)

data class BundleToolState(
    val aabPath: String = "",
    val bundleToolPath: String = "",
    val outputDir: String = "",
    val signingState: SigningState = SigningState(),
    val mode: OutputMode = OutputMode.Universal,
    val isConverting: Boolean = false,
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
    data object Convert : BundleToolEvent()
}

enum class OutputMode { Universal, ApkSet, DeviceSpecific }

enum class SigningMode { Debug, Release }
