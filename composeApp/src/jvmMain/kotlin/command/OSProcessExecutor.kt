package command

import data.domain.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader

class OSProcessExecutor : ICommandExecutor {

    override suspend fun execute(command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val process = Runtime.getRuntime().exec(command)

            val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val errors = process.errorStream.bufferedReader().use(BufferedReader::readText)

            val exitCode = process.waitFor()
            val duration = System.currentTimeMillis() - start

            if (exitCode == 0) {
                CommandResult.Success(output.trim(), duration)
            } else {
                CommandResult.Failure(errors.ifEmpty { "Unknown error" }, exitCode)
            }
        } catch (e: Exception) {
            CommandResult.Failure(e.localizedMessage ?: "Execution failed")
        }
    }
}