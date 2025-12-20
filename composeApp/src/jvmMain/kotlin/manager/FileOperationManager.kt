package manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.files.FileActionResult
import utils.files.FileUtilsImpl
import utils.files.IFileUtils
import java.io.File

class FileOperationManager(
    private val fileUtils: IFileUtils = FileUtilsImpl
) {

    suspend fun handleBundletoolOutput(
        directory: String,
        fileName: String,
        isUniversal: Boolean
    ): FileActionResult = withContext(Dispatchers.IO) {
        if (!isUniversal) {
            return@withContext FileActionResult.Success("Skipped file operations (not universal mode).")
        }

        val baseName = fileName.substringBeforeLast(".")
        val oldFile = File(directory, "$baseName.apks")
        val newFile = File(directory, "$baseName.zip")

        if (!fileUtils.renameFile(oldFile, newFile)) {
            return@withContext FileActionResult.Failure("Failed to rename ${oldFile.name}")
        }

        return@withContext try {
            fileUtils.unzip(newFile, directory)
            val deleted = fileUtils.deleteFile(newFile)
            val message = buildString {
                append("Renamed and unzipped successfully.\n")
                append("Output: $directory")
                if (!deleted) append("\nWarning: Unable to delete temporary zip file.")
            }
            FileActionResult.Success(message)
        } catch (e: Exception) {
            FileActionResult.Failure("Unzipping failed: ${e.localizedMessage}")
        }
    }
}