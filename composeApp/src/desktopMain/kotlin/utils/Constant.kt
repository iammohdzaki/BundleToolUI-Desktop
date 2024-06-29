package utils

object Constant {
    const val SUCCESS = 0
    const val FAILURE = 1
    const val BUNDLE_DOWNLOAD_LINK = "https://github.com/google/bundletool/releases"
}

object DBConstants {
    const val BUNDLETOOL_PATH = "bundletool_path"
    const val ADB_PATH = "adb_path"
}

object FileDialogType {
    const val AAB = -1
    const val BUNDLETOOL = 1
    const val AAPT2 = 2
    const val KEY_STORE_PATH = 3
    const val ADB_PATH = 4
}

object SigningMode {
    const val DEBUG = 1
    const val RELEASE = 2
}