package utils

import java.io.File
import java.io.IOException

object FileHelper {

    fun performFileOperations(directory: String, fileName: String, fileStatus: (Int, String) -> Unit) {
        val oldFile = File(directory, "${fileName.split(".")[0]}.apks")
        val newFile = File(directory, "${fileName.split(".")[0]}.zip")
        if (FileUtils.renameFile(oldFile, newFile)) {
            Log.i("CHANGED NAME\n Unzipping...")
            try {
                FileUtils.unzip(newFile, directory)
                fileStatus.invoke(Constant.SUCCESS, "Rename and Unzip Successful\nFile Saved at ${directory.removeSuffix("\\")}.")
                Log.i("TRYING DELETING FILE")
                val value = FileUtils.deleteFile(newFile)
                Log.i("DELETE STATUS : $value")
            } catch (exception: IOException) {
                Log.i("Unzipping Failed: ${exception.printStackTrace()}")
                fileStatus.invoke(Constant.FAILURE, "Unzipping Failed: ${exception.printStackTrace()}")
            }
        } else {
            Log.i("FAILED RENAMING THE FILE!!")
            fileStatus.invoke(Constant.FAILURE, "FAILED RENAMING THE FILE!!")
        }
    }

}