import utils.SigningMode
import utils.Utils

class CommandBuilder {
    private var bundletoolPath: String = ""
    private var aabFilePath: Pair<String, String> = Pair("", "")
    private var isOverwrite: Boolean = false
    private var isAapt2PathEnabled: Boolean = false
    private var aapt2Path: String = ""
    private var isUniversalMode: Boolean = true
    private var signingMode: Int = 1
    private var keyStorePath: String = ""
    private var keyStorePassword: String = ""
    private var keyAlias: String = ""
    private var keyPassword: String = ""

    fun bundletoolPath(path: String) = apply { this.bundletoolPath = path }
    fun aabFilePath(path: Pair<String, String>) = apply { this.aabFilePath = path }
    fun isOverwrite(overwrite: Boolean) = apply { this.isOverwrite = overwrite }
    fun isAapt2PathEnabled(enabled: Boolean) = apply { this.isAapt2PathEnabled = enabled }
    fun aapt2Path(path: String) = apply { this.aapt2Path = path }
    fun isUniversalMode(universalMode: Boolean) = apply { this.isUniversalMode = universalMode }
    fun signingMode(mode: Int) = apply { this.signingMode = mode }
    fun keyStorePath(path: String) = apply { this.keyStorePath = path }
    fun keyStorePassword(password: String) = apply { this.keyStorePassword = password }
    fun keyAlias(alias: String) = apply { this.keyAlias = alias }
    fun keyPassword(password: String) = apply { this.keyPassword = password }

    fun validateAndGetCommand(): Pair<String, Boolean> {
        if (bundletoolPath.isEmpty()) {
            return Pair("bundletoolPath", false)
        }
        if (aabFilePath.first.isEmpty() || aabFilePath.second.isEmpty()) {
            return Pair("aabFilePath", false)
        }
        if (isAapt2PathEnabled && aapt2Path.isEmpty()) {
            return Pair("aapt2Path", false)
        }
        if (signingMode == SigningMode.RELEASE && (keyStorePath.isEmpty() || keyStorePassword.isEmpty() || keyAlias.isEmpty() || keyPassword.isEmpty())) {
            return Pair("Check Keystore Info!", false)
        }
        return Pair(getCommand(), true)
    }

    private fun getCommand(): String {
        val commandBuilder = StringBuilder()
        if (Utils.isWindowsOS()) {
            commandBuilder.append("java -jar \"${bundletoolPath}\" build-apks ")
            if (isUniversalMode) {
                commandBuilder.append("--mode=universal ")
            }
            if (isOverwrite) {
                commandBuilder.append("--overwrite ")
            }
            if (isAapt2PathEnabled) {
                commandBuilder.append("--aapt2=\"$aapt2Path\" ")
            }
            commandBuilder.append("--bundle=\"${aabFilePath.first}${aabFilePath.second}\" --output=\"${aabFilePath.first}${aabFilePath.second.split(".")[0]}.apks\" ")

            if (signingMode == SigningMode.RELEASE){
                commandBuilder.append("--ks=$keyStorePath --ks-pass=pass:$keyStorePassword --ks-key-alias=$keyAlias --key-pass=pass:$keyPassword ")
            }

            return commandBuilder.toString()
        }
        return ""
    }
}