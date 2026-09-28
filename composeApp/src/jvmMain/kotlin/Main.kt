import aabtoapk.composeapp.generated.resources.Res
import aabtoapk.composeapp.generated.resources.app_name
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ui.windows.SplashWindow
import ui.windows.viewmodel.SplashViewModel
import androidx.compose.runtime.collectAsState
import ui.windows.viewmodel.SplashState

import org.koin.core.context.GlobalContext

fun main() = application {
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 1200.dp, height = 900.dp,
            position = WindowPosition(Alignment.Center)
        ),
        title = stringResource(Res.string.app_name),
        icon = painterResource("files/launcher.png")
    ) {
        val splashViewModel = remember { GlobalContext.get().get<SplashViewModel>() }
        val splashState by splashViewModel.uiState.collectAsState()

        when (splashState.state) {
            SplashState.CHECKING, SplashState.NEEDS_SETUP, SplashState.DOWNLOADING -> {
                SplashWindow(viewModel = splashViewModel, onFinish = { /* Handled by state change */ })
            }
            SplashState.DONE -> {
                App {
                    // optional close handler
                }
            }
        }
    }
}