package command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetSizeCommandTest {

    @Test
    fun `test buildCommand with all parameters`() {
        val config = GetSizeCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            apksPath = "/path/to/app.apks",
            deviceSpecPath = "/path/to/device_spec.json",
            modules = "base,feature1",
            dimensions = "SDK,ABI"
        )
        val command = GetSizeCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isSuccess)
        
        val expectedCommand = "java -jar \"/path/to/bundletool.jar\" get-size total " +
                "--apks=\"/path/to/app.apks\" " +
                "--device-spec=\"/path/to/device_spec.json\" " +
                "--modules=\"base,feature1\" " +
                "--dimensions=\"SDK,ABI\""
                
        assertEquals(expectedCommand, result.getOrNull())
    }

    @Test
    fun `test buildCommand without optional parameters`() {
        val config = GetSizeCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            apksPath = "/path/to/app.apks"
        )
        val command = GetSizeCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isSuccess)
        
        val expectedCommand = "java -jar \"/path/to/bundletool.jar\" get-size total " +
                "--apks=\"/path/to/app.apks\""
                
        assertEquals(expectedCommand, result.getOrNull())
    }

    @Test
    fun `test buildCommand fails if bundletool path is empty`() {
        val config = GetSizeCommand.Config(
            bundleToolPath = "",
            apksPath = "/path/to/app.apks"
        )
        val command = GetSizeCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("BundleTool path missing", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test buildCommand fails if apks path is empty`() {
        val config = GetSizeCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            apksPath = ""
        )
        val command = GetSizeCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("APKS file path missing", result.exceptionOrNull()?.message)
    }
}
