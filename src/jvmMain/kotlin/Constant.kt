import java.io.File
import java.net.URLDecoder

object Constant {
    const val SUCCESS = 0
    const val FAILURE = 1
    private const val bundleToolFileName = "bundletool-all-1.15.6.jar"

    fun getCommand(): String {
        if(Utils.isWindowsOS()){
            val jarFileEncodedUrl = object {}.javaClass.classLoader.getResource(bundleToolFileName)
            val decodedJarPath = URLDecoder.decode(jarFileEncodedUrl.path, "UTF-8")
            val bundletool = File(decodedJarPath)
            return "java -jar \"${bundletool.absolutePath}\" build-apks --mode=universal --bundle=\"INPUT_FILE_PATH\" --output=\"OUTPUT_FILE_NAME.apks\""
        }
        return "bundletool build-apks --mode=universal --bundle=INPUT_FILE_PATH --output=OUTPUT_FILE_NAME.apks"
    }
}