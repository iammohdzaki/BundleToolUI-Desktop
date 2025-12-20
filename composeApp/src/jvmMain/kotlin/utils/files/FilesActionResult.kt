package utils.files

sealed class FileActionResult {
    data class Success(val message: String) : FileActionResult()
    data class Failure(val error: String) : FileActionResult()
}