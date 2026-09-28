package command

import data.domain.CommandResult
import manager.FileOperationManager

/**
 * Represents a generic BundleTool command (e.g. build-apks, extract-apks)
 */
interface BundleToolCommand {
    /**
     * Builds the command string to be executed by the OS.
     */
    fun buildCommand(): Result<String>

    /**
     * Optional post-processing step after the command executes successfully.
     * For example, unzipping an output file.
     */
    suspend fun postProcess(
        fileManager: FileOperationManager, 
        commandOutput: String, 
        durationMs: Long
    ): CommandResult {
        // Default implementation does no post-processing
        return CommandResult.Success(commandOutput, durationMs)
    }
}
