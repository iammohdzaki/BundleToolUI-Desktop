package command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetDeviceSpecCommandTest {

    @Test
    fun `test buildCommand with all parameters`() {
        val config = GetDeviceSpecCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            outputJsonPath = "/path/to/device_spec.json",
            adbPath = "/path/to/adb",
            deviceId = "emulator-5554"
        )
        val command = GetDeviceSpecCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isSuccess)
        
        val expectedCommand = "java -jar \"/path/to/bundletool.jar\" get-device-spec " +
                "--output=\"/path/to/device_spec.json\" " +
                "--adb=\"/path/to/adb\" " +
                "--device-id=\"emulator-5554\""
                
        assertEquals(expectedCommand, result.getOrNull())
    }

    @Test
    fun `test buildCommand without optional parameters`() {
        val config = GetDeviceSpecCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            outputJsonPath = "/path/to/device_spec.json"
        )
        val command = GetDeviceSpecCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isSuccess)
        
        val expectedCommand = "java -jar \"/path/to/bundletool.jar\" get-device-spec " +
                "--output=\"/path/to/device_spec.json\""
                
        assertEquals(expectedCommand, result.getOrNull())
    }

    @Test
    fun `test buildCommand fails if bundletool path is empty`() {
        val config = GetDeviceSpecCommand.Config(
            bundleToolPath = "",
            outputJsonPath = "/path/to/device_spec.json"
        )
        val command = GetDeviceSpecCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("BundleTool path missing", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test buildCommand fails if output path is empty`() {
        val config = GetDeviceSpecCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            outputJsonPath = ""
        )
        val command = GetDeviceSpecCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Output path missing", result.exceptionOrNull()?.message)
    }
}
