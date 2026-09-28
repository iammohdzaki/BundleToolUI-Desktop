package command

import data.domain.CommandResult
import manager.FileOperationManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class GetDeviceSpecCommand(
    private val config: Config
) : BundleToolCommand {
    private val logger: Logger = LoggerFactory.getLogger("GetDeviceSpecCommand")

    data class Config(
        val bundleToolPath: String,
        val adbPath: String? = null,
        val outputJsonPath: String,
        val deviceId: String? = null
    )

    override fun buildCommand(): Result<String> {
        if (config.bundleToolPath.isBlank()) return Result.failure(IllegalArgumentException("BundleTool path missing"))
        if (config.outputJsonPath.isBlank()) return Result.failure(IllegalArgumentException("Output path missing"))

        val cmd = buildString {
            append("java -jar \"${config.bundleToolPath}\" get-device-spec ")
            append("--output=\"${config.outputJsonPath}\" ")
            config.adbPath?.takeIf { it.isNotBlank() }?.let {
                append("--adb=\"$it\" ")
            }
            config.deviceId?.takeIf { it.isNotBlank() }?.let {
                append("--device-id=\"$it\" ")
            }
        }
        logger.debug("Built command: {}", cmd.trim())
        return Result.success(cmd.trim())
    }
}
