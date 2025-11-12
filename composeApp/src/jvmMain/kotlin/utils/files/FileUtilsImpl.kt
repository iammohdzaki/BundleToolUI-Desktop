package utils.files

import java.io.File
import java.io.FileOutputStream

object FileUtilsImpl : IFileUtils {
    override fun renameFile(source: File, destination: File): Boolean {
        if (!source.exists()) return false

        // If destination exists, try to delete it first
        if (destination.exists()) {
            if (!destination.delete()) {
                println("⚠️ Destination file already exists and could not be deleted: ${destination.absolutePath}")
                return false
            }
        }

        // Try normal rename first
        if (source.renameTo(destination)) return true

        // Fallback: manual copy + delete (works on Windows too)
        return try {
            source.copyTo(destination, overwrite = true)
            if (source.delete()) {
                println("✅ Copied + deleted old file successfully.")
                true
            } else {
                println("⚠️ Copied but failed to delete original file.")
                true // still successful for user
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun unzip(zipFile: File, outputDir: String) {
        val buffer = ByteArray(1024)
        val zipInputStream = java.util.zip.ZipInputStream(zipFile.inputStream())
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val newFile = File(outputDir, entry.name)
            if (entry.isDirectory) {
                newFile.mkdirs()
            } else {
                newFile.parentFile?.mkdirs()
                FileOutputStream(newFile).use { output ->
                    var len: Int
                    while (zipInputStream.read(buffer).also { len = it } > 0) {
                        output.write(buffer, 0, len)
                    }
                }
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }

    override fun deleteFile(file: File): Boolean {
        return try {
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}