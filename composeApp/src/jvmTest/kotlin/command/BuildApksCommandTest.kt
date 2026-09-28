package command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildApksCommandTest {

    @Test
    fun `should format command correctly for default APK set`() {
        val config = BuildApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            aabPath = "/path/to/app.aab",
            outputDir = "/path/to/output"
        )
        val command = BuildApksCommand(config)
        
        val result = command.buildCommand().getOrThrow()
        assertTrue(result.contains("java -jar \"/path/to/bundletool.jar\" build-apks"))
        assertTrue(result.contains("--bundle=\"/path/to/app.aab\""))
        assertTrue(result.contains("--output=\"/path/to/output\""))
        assertTrue(!result.contains("--mode=universal"))
    }

    @Test
    fun `should format command correctly for universal APK`() {
        val config = BuildApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            aabPath = "/path/to/app.aab",
            outputDir = "/path/to/output",
            isUniversal = true
        )
        val command = BuildApksCommand(config)
        
        val result = command.buildCommand().getOrThrow()
        
        assertTrue(result.contains("--mode=universal"))
    }

    @Test
    fun `should include device spec when provided`() {
        val config = BuildApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            aabPath = "/path/to/app.aab",
            outputDir = "/path/to/output",
            deviceSpecPath = "/path/to/spec.json"
        )
        val command = BuildApksCommand(config)
        
        val result = command.buildCommand().getOrThrow()
        
        assertTrue(result.contains("--device-spec=\"/path/to/spec.json\""))
    }

    @Test
    fun `should include connected device flags when provided`() {
        val config = BuildApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            aabPath = "/path/to/app.aab",
            outputDir = "/path/to/output",
            connectedDevice = true,
            deviceId = "emulator-5554"
        )
        val command = BuildApksCommand(config)
        
        val result = command.buildCommand().getOrThrow()
        
        assertTrue(result.contains("--connected-device"))
        assertTrue(result.contains("--device-id=\"emulator-5554\""))
    }

    @Test
    fun `should include local testing flag when provided`() {
        val config = BuildApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            aabPath = "/path/to/app.aab",
            outputDir = "/path/to/output",
            localTesting = true
        )
        val command = BuildApksCommand(config)
        
        val result = command.buildCommand().getOrThrow()
        
        assertTrue(result.contains("--local-testing"))
    }
    
    @Test
    fun `should include keystore config when provided`() {
        val config = BuildApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            aabPath = "/path/to/app.aab",
            outputDir = "/path/to/output",
            keystore = BuildApksCommand.KeystoreConfig(
                path = "/path/to/keystore.jks",
                password = "storepassword",
                alias = "keyalias",
                keyPassword = "keypassword"
            )
        )
        val command = BuildApksCommand(config)
        
        val result = command.buildCommand().getOrThrow()
        
        assertTrue(result.contains("--ks=\"/path/to/keystore.jks\""))
        assertTrue(result.contains("--ks-pass=pass:storepassword"))
        assertTrue(result.contains("--ks-key-alias=\"keyalias\""))
        assertTrue(result.contains("--key-pass=pass:keypassword"))
    }

    @Test
    fun `should fail if bundleToolPath is blank`() {
        val config = BuildApksCommand.Config(
            bundleToolPath = "",
            aabPath = "/path/to/app.aab"
        )
        val command = BuildApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail if aabPath is blank`() {
        val config = BuildApksCommand.Config(
            bundleToolPath = "/path/to/bundletool.jar",
            aabPath = ""
        )
        val command = BuildApksCommand(config)
        
        val result = command.buildCommand()
        
        assertTrue(result.isFailure)
    }
}
