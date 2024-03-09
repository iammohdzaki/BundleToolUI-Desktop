package utils

import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.FileOutputStream
import java.util.zip.ZipFile

object FileUtils {

    /**
     * Size of the buffer to read/write data
     */
    private const val BUFFER_SIZE = 4096

    fun deleteFile(file: File) =
        file.delete()

    /**
     * @param oldFile
     * @param newFile
     */
    fun renameFile(oldFile: File, newFile: File) =
        oldFile.renameTo(newFile)

    /**
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unzip(zipFilePath: File, destDirectory: String, skipFile: String = "toc.pb") {

        File(destDirectory).run {
            if (!exists()) {
                mkdirs()
            }
        }

        ZipFile(zipFilePath).use { zip ->

            zip.entries().asSequence().forEach { entry ->
                if (entry.name == skipFile) return@forEach

                zip.getInputStream(entry).use { input ->
                    val filePath = destDirectory + File.separator + entry.name

                    if (!entry.isDirectory) {
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    } else {
                        // if the entry is a directory, make the directory
                        val dir = File(filePath)
                        dir.mkdir()
                    }
                }
            }
        }
    }

    /**
     * Extracts a zip entry (file entry)
     * @param inputStream
     * @param destFilePath
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }
}