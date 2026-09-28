package command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExtractApksCommandTest {

    @Test
    fun `test buildCommand without device spec falls back to MOCK_UNZIP`() {
        val config = ExtractApksCommand.Config(
            bundleToolPath = "bundletool.jar",
            apksPath = "app.apks",
            outputDir = "output"
        )
        val command = ExtractApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isSuccess)
        assertEquals("MOCK_UNZIP", result.getOrNull())
    }

    @Test
    fun `test buildCommand with device spec generates correct bundletool command`() {
        val config = ExtractApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            apksPath = "/path/to/app.apks",
            outputDir = "/path/to/output_dir",
            deviceSpecPath = "/path/to/device_spec.json"
        )
        val command = ExtractApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isSuccess)
        
        val expectedCommand = "java -jar \"/path/to/bundletool.jar\" extract-apks " +
                "--apks=\"/path/to/app.apks\" " +
                "--output-dir=\"/path/to/output_dir\" " +
                "--device-spec=\"/path/to/device_spec.json\""
                
        assertEquals(expectedCommand, result.getOrNull())
    }
    
    @Test
    fun `test buildCommand fails if bundletool path is empty`() {
        val config = ExtractApksCommand.Config(
            bundleToolPath = "",
            apksPath = "/path/to/app.apks",
            outputDir = "/path/to/output_dir"
        )
        val command = ExtractApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("BundleTool path missing", result.exceptionOrNull()?.message)
    }
}
