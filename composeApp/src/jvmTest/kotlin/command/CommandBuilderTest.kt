package command

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CommandBuilderTest {

    private fun tempAabFile(name: String = "app.aab"): File {
        val tempDir = File(System.getProperty("java.io.tmpdir"))
        val aab = File(tempDir, name)
        if (!aab.exists()) aab.writeText("dummy")
        return aab
    }

    @Test
    fun `resolveOutputPath returns default next to aab when outputDir is null`() {
        val aab = tempAabFile("sample.aab")
        val config = CommandBuilder.Config(bundleToolPath = "bt.jar", aabPath = aab.absolutePath)
        val cb = CommandBuilder(config)

        val resolved = cb.resolveOutputPath(config)
        val expected = File(aab.parent, "${aab.nameWithoutExtension}.apks").absolutePath
        assertEquals(expected, resolved)
    }

    @Test
    fun `resolveOutputPath uses directory when directory provided`() {
        val aab = tempAabFile("sample2.aab")
        val outDir = File(System.getProperty("java.io.tmpdir"), "outTestDir")
        outDir.mkdirs()
        val config = CommandBuilder.Config(bundleToolPath = "bt.jar", aabPath = aab.absolutePath, outputDir = outDir.absolutePath)
        val cb = CommandBuilder(config)

        val resolved = cb.resolveOutputPath(config)
        val expected = File(outDir, "${aab.nameWithoutExtension}.apks").absolutePath
        assertEquals(expected, resolved)
    }

    @Test
    fun `resolveOutputPath treats trailing slash as directory path`() {
        val aab = tempAabFile("sample3.aab")
        val tmp = System.getProperty("java.io.tmpdir")
        val config = CommandBuilder.Config(bundleToolPath = "bt.jar", aabPath = aab.absolutePath, outputDir = tmp + File.separator)
        val cb = CommandBuilder(config)

        val resolved = cb.resolveOutputPath(config)
        val expected = File(tmp, "${aab.nameWithoutExtension}.apks").absolutePath
        assertEquals(expected, resolved)
    }

    @Test
    fun `resolveOutputPath uses provided file when not a directory and no trailing slash`() {
        val aab = tempAabFile("sample4.aab")
        val provided = File(System.getProperty("java.io.tmpdir"), "customName.apks")
        val config = CommandBuilder.Config(bundleToolPath = "bt.jar", aabPath = aab.absolutePath, outputDir = provided.absolutePath)
        val cb = CommandBuilder(config)

        val resolved = cb.resolveOutputPath(config)
        assertEquals(provided.absolutePath, resolved)
    }

    @Test
    fun `build returns failure when bundleToolPath or aabPath missing`() {
        val cfg1 = CommandBuilder.Config(bundleToolPath = "", aabPath = "some.aab")
        val cb1 = CommandBuilder(cfg1)
        val r1 = cb1.build()
        assertTrue(r1.isFailure)

        val cfg2 = CommandBuilder.Config(bundleToolPath = "bt.jar", aabPath = "")
        val cb2 = CommandBuilder(cfg2)
        val r2 = cb2.build()
        assertTrue(r2.isFailure)
    }

    @Test
    fun `build constructs command with universal mode and default overwrite present`() {
        val aab = tempAabFile("sample5.aab")
        val cfg = CommandBuilder.Config(bundleToolPath = "bt.jar", aabPath = aab.absolutePath, isUniversal = true, overwrite = false)
        val cb = CommandBuilder(cfg)
        val res = cb.build()
        assertTrue(res.isSuccess)
        val cmd = res.getOrNull()!!

        assertTrue(cmd.contains("java -jar \"bt.jar\" build-apks"))
        assertTrue(cmd.contains("--bundle=\"${aab.absolutePath}\""))
        // universal mode should be present
        assertTrue(cmd.contains("--mode=universal"))
        // Note: current implementation always appends --overwrite at the end, so at least one occurrence expected
        assertTrue(cmd.contains("--overwrite"))
    }

    @Test
    fun `build duplicates overwrite when overwrite flag true (current behavior)`() {
        val aab = tempAabFile("sample6.aab")
        val cfg = CommandBuilder.Config(bundleToolPath = "bt.jar", aabPath = aab.absolutePath, isUniversal = false, overwrite = true)
        val cb = CommandBuilder(cfg)
        val res = cb.build()
        assertTrue(res.isSuccess)
        val cmd = res.getOrNull()!!

        // Because the implementation appends overwrite conditionally and then unconditionally, expect two occurrences
        val occurrences = "--overwrite".toRegex().findAll(cmd).count()
        assertEquals(2, occurrences)
    }

    @Test
    fun `build includes keystore parameters when provided`() {
        val aab = tempAabFile("sample7.aab")
        val ks = CommandBuilder.KeystoreConfig(path = "keystore.jks", password = "pw", alias = "alias", keyPassword = "kpw")
        val cfg = CommandBuilder.Config(bundleToolPath = "bt.jar", aabPath = aab.absolutePath, keystore = ks)
        val cb = CommandBuilder(cfg)
        val res = cb.build()
        assertTrue(res.isSuccess)
        val cmd = res.getOrNull()!!

        assertTrue(cmd.contains("--ks=${ks.path}"))
        assertTrue(cmd.contains("--ks-pass=pass:${ks.password}"))
        assertTrue(cmd.contains("--ks-key-alias=${ks.alias}"))
        assertTrue(cmd.contains("--key-pass=pass:${ks.keyPassword}"))
    }
}

