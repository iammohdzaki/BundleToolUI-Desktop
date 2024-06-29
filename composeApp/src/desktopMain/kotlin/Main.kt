import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import di.mainModules
import di.viewModelModules
import local.FileStorageHelper
import org.koin.compose.KoinApplication
import ui.screens.HomeScreen
import utils.DBConstants
import utils.Log
import utils.Strings

fun main() = application {
    val fileStorageHelper = FileStorageHelper()
    // Check if path for bundletool exists in local storage
    val path = fileStorageHelper.read(DBConstants.BUNDLETOOL_PATH) as String?
    val adbPath = fileStorageHelper.read(DBConstants.ADB_PATH) as String?
    val icon = painterResource("launcher.png")
    Log.showLogs = true
    Window(
        icon = icon,
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 1200.dp, height = 1000.dp,
            position = WindowPosition(Alignment.Center)
        ),
        title = Strings.APP_NAME
    ) {
        KoinApplication(
            application = {
                modules(viewModelModules(), mainModules())
            }
        ) {
            Navigator(screen = HomeScreen(fileStorageHelper, path, adbPath))
        }
    }
}