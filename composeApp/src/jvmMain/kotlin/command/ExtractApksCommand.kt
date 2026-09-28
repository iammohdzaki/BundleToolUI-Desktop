package command

import data.domain.CommandResult
import manager.FileOperationManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExtractApksCommand(
    private val config: Config
) : BundleToolCommand {
    private val logger: Logger = LoggerFactory.getLogger("ExtractApksCommand")

    data class Config(
        val bundleToolPath: String,
        val apksPath: String,
        val outputDir: String,
        val deviceSpecPath: String? = null
    )

    override fun buildCommand(): Result<String> {
        if (config.bundleToolPath.isBlank()) return Result.failure(IllegalArgumentException("BundleTool path missing"))
        if (config.apksPath.isBlank()) return Result.failure(IllegalArgumentException("APKS file path missing"))
        if (config.outputDir.isBlank()) return Result.failure(IllegalArgumentException("Output directory missing"))

        if (config.deviceSpecPath.isNullOrBlank()) {
            // No device spec provided. We can't use bundletool extract-apks.
            // But an .apks file is just a zip containing the APKs! We can just unzip it.
            // We will return a mock command to indicate we are bypassing bundletool,
            // and perform the extraction in postProcess.
            return Result.success("MOCK_UNZIP")
        }

        val cmd = buildString {
            append("java -jar \"${config.bundleToolPath}\" extract-apks ")
            append("--apks=\"${config.apksPath}\" ")
            append("--output-dir=\"${config.outputDir}\" ")
            append("--device-spec=\"${config.deviceSpecPath}\" ")
        }
        logger.debug("Built command: {}", cmd.trim())
        return Result.success(cmd.trim())
    }

    override suspend fun postProcess(
        fileManager: FileOperationManager,
        commandOutput: String,
        durationMs: Long
    ): CommandResult {
        if (config.deviceSpecPath.isNullOrBlank()) {
            return fileManager.extractApksArchive(config.apksPath, config.outputDir, durationMs)
        }
        return CommandResult.Success(commandOutput, durationMs)
    }
}
