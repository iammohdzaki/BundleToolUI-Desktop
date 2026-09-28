package usecase

import command.BundleToolCommand
import command.ICommandExecutor
import data.domain.CommandExecutionResult
import data.domain.CommandResult
import manager.FileOperationManager

class CommandUseCase(
    private val executor: ICommandExecutor,
    private val fileManager: FileOperationManager
) {
    suspend fun executeCommand(rawCommand: String): CommandResult {
        return executor.execute(rawCommand)
    }

    suspend fun executeBundleTool(command: BundleToolCommand): CommandExecutionResult {
        val result = command.buildCommand()

        return result.fold(
            onSuccess = { cmdString ->
                val execResult = if (cmdString == "MOCK_UNZIP") {
                    command.postProcess(fileManager, "Extracting all APKs from archive directly...", 0)
                } else {
                    when (val res = executor.execute(cmdString)) {
                        is CommandResult.Success -> {
                            command.postProcess(fileManager, res.output, res.durationMs)
                        }
                        is CommandResult.Failure -> res
                    }
                }
                CommandExecutionResult(
                    if (cmdString == "MOCK_UNZIP") "Bypassed bundletool: Extracting APKS archive directly" else cmdString,
                    execResult
                )
            },
            onFailure = {
                CommandExecutionResult(
                    "Invalid command configuration",
                    CommandResult.Failure(it.message ?: "Invalid config")
                )
            }
        )
    }
}