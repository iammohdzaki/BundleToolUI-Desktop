package command

import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class CommandExecutorTest {
    private lateinit var commandExecutor: CommandExecutor

    @Before
    fun setUp() {
        commandExecutor = CommandExecutor()
    }

    // These test cases may not work for MAC and Linux need to verify.
    @Test
    fun `execute valid command successfully`() {
        val expectedOutput = "openjdk 11.0.18 2023-01-17"
        val cmd = "java --version"

        runBlocking {
            commandExecutor.executeCommand(
                cmd,
                this,
                onSuccess = {
                    println("SUCCESS -> $it")
                    assertThat(expectedOutput, it.contains(expectedOutput))
                },
                onFailure = {
                    println("ERROR -> ${it.message}")
                    assertThat(expectedOutput, it.message?.contains(expectedOutput) ?: false)
                }
            )
        }
    }

    @Test
    fun `execute invalid command and handle failure`() {
        val cmd = "invalid_command"
        runBlocking {
            commandExecutor.executeCommand(
                cmd,
                this,
                onSuccess = {
                    assertTrue(it.isEmpty())
                },
                onFailure = {
                    assertTrue(it.message?.contains("The system cannot find the file specified") ?: false)
                }
            )
        }
    }
}