package command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class CommandExecutor {

    fun executeCommand(
        cmd: String,
        coroutineScope: CoroutineScope,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val runtime = Runtime.getRuntime()
                val startTime = System.currentTimeMillis()

                val process = runtime.exec(cmd)
                val outputReader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                var output = ""
                var errorOutput = ""

                // Read command output
                var line: String?
                while (outputReader.readLine().also { line = it } != null) {
                    output += line + "\n"
                }

                // Read error output
                while (errorReader.readLine().also { line = it } != null) {
                    errorOutput += line + "\n"
                }

                process.waitFor()
                val endTime = System.currentTimeMillis()
                if (process.exitValue() == 0) {
                    val executionTime = ((endTime - startTime) / 1000).toString() + "s"
                    onSuccess("$cmd\n$output\nCommand Executed in $executionTime")
                } else {
                    val exception = Exception("Command execution failed: $cmd$errorOutput")
                    onFailure(exception)
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}