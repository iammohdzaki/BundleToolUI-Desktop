package utils

import java.util.*

object Utils {

    fun isWindowsOS(): Boolean{
        return System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")
    }

}