package data.domain

data class CommandExecutionResult(
    val command: String,
    val result: CommandResult
)