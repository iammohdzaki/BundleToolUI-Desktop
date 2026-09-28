package command

import data.domain.CommandResult
import manager.FileOperationManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GetSizeCommand(
    private val config: Config
) : BundleToolCommand {
    private val logger: Logger = LoggerFactory.getLogger("GetSizeCommand")

    data class Config(
        val bundleToolPath: String,
        val apksPath: String,
        val deviceSpecPath: String? = null,
        val modules: String? = null,
        val dimensions: String? = null
    )

    override fun buildCommand(): Result<String> {
        if (config.bundleToolPath.isBlank()) return Result.failure(IllegalArgumentException("BundleTool path missing"))
        if (config.apksPath.isBlank()) return Result.failure(IllegalArgumentException("APKS file path missing"))

        val cmd = buildString {
            append("java -jar \"${config.bundleToolPath}\" get-size total ")
            append("--apks=\"${config.apksPath}\" ")
            config.deviceSpecPath?.takeIf { it.isNotBlank() }?.let {
                append("--device-spec=\"$it\" ")
            }
            config.modules?.takeIf { it.isNotBlank() }?.let {
                append("--modules=\"$it\" ")
            }
            config.dimensions?.takeIf { it.isNotBlank() }?.let {
                append("--dimensions=\"$it\" ")
            }
        }
        logger.debug("Built command: {}", cmd.trim())
        return Result.success(cmd.trim())
    }
}
