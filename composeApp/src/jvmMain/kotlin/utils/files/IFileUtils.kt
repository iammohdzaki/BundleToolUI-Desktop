package utils.files

import java.io.File

interface IFileUtils {
    fun renameFile(source: File, destination: File): Boolean
    fun unzip(zipFile: File, outputDir: String)
    fun deleteFile(file: File): Boolean
}