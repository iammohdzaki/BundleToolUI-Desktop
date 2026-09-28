package command

import data.domain.CommandResult
import manager.FileOperationManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.files.FileActionResult
import java.io.File

class BuildApksCommand(
    private val config: Config
) : BundleToolCommand {

    private val logger: Logger = LoggerFactory.getLogger("BuildApksCommand")

    data class Config(
        val bundleToolPath: String,
        val aabPath: String,
        val outputDir: String? = null,
        val isUniversal: Boolean = false,
        val keystore: KeystoreConfig? = null,
        val deviceId: String? = null,
        val overwrite: Boolean = false,
        val aapt2Path: String? = null,
        val connectedDevice: Boolean = false,
        val deviceSpecPath: String? = null,
        val localTesting: Boolean = false
    )

    data class KeystoreConfig(
        val path: String,
        val password: String,
        val alias: String,
        val keyPassword: String
    )

    override fun buildCommand(): Result<String> {
        if (config.bundleToolPath.isBlank()) return Result.failure(IllegalArgumentException("BundleTool path missing"))
        if (config.aabPath.isBlank()) return Result.failure(IllegalArgumentException("AAB file path missing"))
        
        val outputPath = resolveOutputPath()
        val cmd = buildString {
            append("java -jar \"${config.bundleToolPath}\" build-apks ")
            append("--bundle=\"${config.aabPath}\" ")
            append("--output=\"$outputPath\" ")
            if (config.isUniversal) append("--mode=universal ")
            if (config.overwrite) append("--overwrite ")
            if (config.connectedDevice) append("--connected-device ")
            if (config.localTesting) append("--local-testing ")
            config.aapt2Path?.takeIf { it.isNotBlank() }?.let { append("--aapt2=\"$it\" ") }
            config.deviceSpecPath?.takeIf { it.isNotBlank() }?.let { append("--device-spec=\"$it\" ") }
            config.keystore?.let {
                append("--ks=\"${it.path}\" ")
                append("--ks-pass=pass:${it.password} ")
                append("--ks-key-alias=\"${it.alias}\" ")
                append("--key-pass=pass:${it.keyPassword} ")
            }
            config.deviceId?.takeIf { it.isNotBlank() }?.let { append("--device-id=\"$it\" ") }
        }
        logger.debug("Built command: {}", cmd.trim())
        return Result.success(cmd.trim())
    }

    override suspend fun postProcess(
        fileManager: FileOperationManager,
        commandOutput: String,
        durationMs: Long
    ): CommandResult {
        val outputFile = File(resolveOutputPath())
        
        val fileResult = fileManager.handleBundletoolOutput(
            directory = outputFile.parent ?: ".",
            fileName = outputFile.name,
            isUniversal = config.isUniversal
        )

        return when (fileResult) {
            is FileActionResult.Success -> CommandResult.Success(
                "$commandOutput\n${fileResult.message}",
                durationMs
            )
            is FileActionResult.Failure -> CommandResult.Failure(fileResult.error)
        }
    }

    private fun resolveOutputPath(): String {
        val output = config.outputDir
        val aabFile = File(config.aabPath)
        val defaultOutput = File(aabFile.parent, "${aabFile.nameWithoutExtension}.apks").absolutePath

        if (output.isNullOrBlank()) return defaultOutput

        val outputFile = File(output)
        return when {
            outputFile.exists() && outputFile.isDirectory ->
                File(outputFile, "${aabFile.nameWithoutExtension}.apks").absolutePath

            output.endsWith("/") || output.endsWith("\\") ->
                File(output, "${aabFile.nameWithoutExtension}.apks").absolutePath

            else -> output
        }
    }
}