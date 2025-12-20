package usecase

import command.CommandBuilder
import command.ICommandExecutor
import data.domain.CommandExecutionResult
import data.domain.CommandResult
import manager.FileOperationManager
import utils.files.FileActionResult
import java.io.File

class CommandUseCase(
    private val executor: ICommandExecutor,
    private val fileManager: FileOperationManager
) {

    suspend fun executeCommand(rawCommand: String): CommandResult {
        return executor.execute(rawCommand)
    }

    suspend fun executeBundleTool(config: CommandBuilder.Config): CommandExecutionResult {
        val commandBuilder = CommandBuilder(config)
        val result = commandBuilder.build()

        return result.fold(
            onSuccess = { command ->
                val execResult = when (val res = executor.execute(command)) {
                    is CommandResult.Success -> {
                        val outputFile = File(commandBuilder.resolveOutputPath(config))
                        val fileResult = fileManager.handleBundletoolOutput(
                            directory = outputFile.parent ?: ".",
                            fileName = outputFile.name,
                            isUniversal = config.isUniversal
                        )

                        when (fileResult) {
                            is FileActionResult.Success -> CommandResult.Success(
                                "${res.output}\n${fileResult.message}",
                                res.durationMs
                            )

                            is FileActionResult.Failure -> CommandResult.Failure(fileResult.error)
                        }
                    }

                    is CommandResult.Failure -> res
                }
                CommandExecutionResult(command, execResult)
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