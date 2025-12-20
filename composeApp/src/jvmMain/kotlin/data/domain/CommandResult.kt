package data.domain

sealed class CommandResult {
    data class Success(val output: String, val durationMs: Long) : CommandResult()
    data class Failure(val error: String, val exitCode: Int? = null) : CommandResult()
}