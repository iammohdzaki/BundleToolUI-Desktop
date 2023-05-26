object Constant {
    const val SUCCESS = 0
    const val FAILURE = 1

    fun getCommand(): String {
        return "bundletool build-apks --mode=universal --bundle=INPUT_FILE_PATH --output=OUTPUT_FILE_NAME.apks"
    }
}