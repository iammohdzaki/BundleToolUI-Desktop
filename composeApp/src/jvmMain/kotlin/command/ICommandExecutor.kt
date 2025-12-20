package command

import data.domain.CommandResult

interface ICommandExecutor {
    suspend fun execute(command: String): CommandResult
}