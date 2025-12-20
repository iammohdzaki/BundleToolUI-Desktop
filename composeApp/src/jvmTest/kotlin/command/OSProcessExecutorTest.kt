package command

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OSProcessExecutorTest {

    @Test
    fun `execute returns success for simple echo command`() = runBlocking {
        val executor = OSProcessExecutor()
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val cmd = if (isWindows) "cmd /c echo hello" else "echo hello"

        val result = executor.execute(cmd)
        when (result) {
            is data.domain.CommandResult.Success -> {
                assertTrue(result.output.contains("hello"))
                assertTrue(result.durationMs >= 0)
            }
            is data.domain.CommandResult.Failure -> fail("Expected success but got failure: ${result.error}")
        }
    }

    @Test
    fun `execute returns failure for invalid command`() = runBlocking {
        val executor = OSProcessExecutor()
        val cmd = "nonexistent_command_foobar_12345"
        val result = executor.execute(cmd)
        when (result) {
            is data.domain.CommandResult.Success -> fail("Expected failure but got success: ${result.output}")
            is data.domain.CommandResult.Failure -> {
                // On some platforms, trying to exec a non-existent command may throw and be caught
                assertTrue(result.error.isNotEmpty())
            }
        }
    }
}

