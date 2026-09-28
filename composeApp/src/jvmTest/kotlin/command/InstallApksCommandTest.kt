package command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallApksCommandTest {

    @Test
    fun `test buildCommand with all parameters`() {
        val config = InstallApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            apksPath = "/path/to/app.apks",
            adbPath = "/path/to/adb",
            deviceId = "emulator-5554"
        )
        val command = InstallApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isSuccess)
        
        val expectedCommand = "java -jar \"/path/to/bundletool.jar\" install-apks " +
                "--apks=\"/path/to/app.apks\" " +
                "--adb=\"/path/to/adb\" " +
                "--device-id=\"emulator-5554\""
                
        assertEquals(expectedCommand, result.getOrNull())
    }

    @Test
    fun `test buildCommand without adb path and device id`() {
        val config = InstallApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            apksPath = "/path/to/app.apks"
        )
        val command = InstallApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isSuccess)
        
        val expectedCommand = "java -jar \"/path/to/bundletool.jar\" install-apks " +
                "--apks=\"/path/to/app.apks\""
                
        assertEquals(expectedCommand, result.getOrNull())
    }
    
    @Test
    fun `test buildCommand fails if bundletool path is empty`() {
        val config = InstallApksCommand.Config(
            bundleToolPath = "",
            apksPath = "/path/to/app.apks"
        )
        val command = InstallApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("BundleTool path missing", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test buildCommand fails if apks path is empty`() {
        val config = InstallApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            apksPath = ""
        )
        val command = InstallApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("APKS file path missing", result.exceptionOrNull()?.message)
    }
}
