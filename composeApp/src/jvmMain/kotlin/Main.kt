import aabtoapk.composeapp.generated.resources.Res
import aabtoapk.composeapp.generated.resources.app_name
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import di.initKoin
import org.jetbrains.compose.resources.stringResource
import ui.App

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 800.dp, height = 1000.dp,
            position = WindowPosition(Alignment.Center)
        ),
        title = stringResource(Res.string.app_name),
        icon = painterResource("files/launcher.png")
    ) {
        App {}
    }
}