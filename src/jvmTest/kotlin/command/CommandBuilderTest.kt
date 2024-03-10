package command

import org.junit.Assert.assertEquals
import org.junit.Test
import utils.SigningMode

class CommandBuilderTest {

    @Test
    fun `validateAndGetCommand should return error if bundletoolPath is empty`() {
        val result = CommandBuilder()
            .validateAndGetCommand()
        assertEquals("bundletoolPath", result.first)
        assertEquals(false, result.second)
    }

    @Test
    fun `validateAndGetCommand should return error if aabFilePath is empty`() {
        val result = CommandBuilder()
            .bundletoolPath("bundletool.jar")
            .validateAndGetCommand()
        assertEquals("aabFilePath", result.first)
        assertEquals(false, result.second)
    }

    @Test
    fun `validateAndGetCommand should return error if aapt2Path is empty but isAapt2PathEnabled is true`() {
        val result = CommandBuilder()
            .bundletoolPath("bundletool.jar")
            .aabFilePath(Pair("/path", "gg.gg"))
            .isAapt2PathEnabled(true)
            .validateAndGetCommand()
        assertEquals("aapt2Path", result.first)
        assertEquals(false, result.second)
    }

    @Test
    fun `validateAndGetCommand should return error if signingMode is RELEASE but keystore information is missing`() {
        val result = CommandBuilder()
            .bundletoolPath("bundletool.jar")
            .aabFilePath(Pair("/path/to/", "file.aab"))
            .signingMode(SigningMode.RELEASE)
            .validateAndGetCommand()
        assertEquals("Check Keystore Info!", result.first)
        assertEquals(false, result.second)
    }

    @Test
    fun `validateAndGetCommand should return valid command for RELEASE signing mode with all required parameters`() {
        val result = CommandBuilder()
            .bundletoolPath("bundletool.jar")
            .aabFilePath(Pair("/path/to/", "file.aab"))
            .signingMode(SigningMode.RELEASE)
            .keyStorePath("/path/to/keystore.jks")
            .keyStorePassword("keystorePassword")
            .keyAlias("keyAlias")
            .keyPassword("keyPassword")
            .isUniversalMode(false).validateAndGetCommand()
        assertEquals(
            "java -jar \"bundletool.jar\" build-apks --bundle=\"/path/to/file.aab\" --output=\"/path/to/file.apks\" --ks=/path/to/keystore.jks --ks-pass=pass:keystorePassword --ks-key-alias=keyAlias --key-pass=pass:keyPassword ",
            result.first
        )
        assertEquals(true, result.second)
    }

    @Test
    fun `validateAndGetCommand should return valid command for DEBUG signing mode without keystore information`() {
        val result = CommandBuilder()
            .bundletoolPath("bundletool.jar")
            .aabFilePath(Pair("/path/to/", "file.aab"))
            .signingMode(SigningMode.DEBUG)
            .isUniversalMode(false)
            .validateAndGetCommand()
        assertEquals(
            "java -jar \"bundletool.jar\" build-apks --bundle=\"/path/to/file.aab\" --output=\"/path/to/file.apks\" ",
            result.first
        )
        assertEquals(true, result.second)
    }

    @Test
    fun `validateAndGetCommand should return valid command for verifying adb path`() {
        val result = CommandBuilder()
            .verifyAdbPath(true, "/path/to/adb")
            .getAdbVerifyCommand()
        assertEquals("\"/path/to/adb\" version", result)
    }

    @Test
    fun `validateAndGetCommand should return valid command for universal mode enabled`() {
        val result = CommandBuilder()
            .bundletoolPath("bundletool.jar")
            .aabFilePath(Pair("/path/to/", "file.aab"))
            .isUniversalMode(true)
            .validateAndGetCommand()
        assertEquals(
            "java -jar \"bundletool.jar\" build-apks --mode=universal --bundle=\"/path/to/file.aab\" --output=\"/path/to/file.apks\" ",
            result.first
        )
        assertEquals(true, result.second)
    }

    @Test
    fun `validateAndGetCommand should return valid command with overwrite enabled`() {
        val result = CommandBuilder()
            .bundletoolPath("bundletool.jar")
            .aabFilePath(Pair("/path/to/", "file.aab"))
            .isOverwrite(true)
            .isUniversalMode(false).validateAndGetCommand()
        assertEquals(
            "java -jar \"bundletool.jar\" build-apks --overwrite --bundle=\"/path/to/file.aab\" --output=\"/path/to/file.apks\" ",
            result.first
        )
        assertEquals(true, result.second)
    }
}
