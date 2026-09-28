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

import org.slf4j.LoggerFactory
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.rememberTrayState

fun main() {
    val logger = LoggerFactory.getLogger("BundleToolUI-Desktop")
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.error("Uncaught exception in thread ${thread.name}", throwable)
    }

    application {
        initKoin()

    var isMainWindowOpen by remember { mutableStateOf(true) }
    val appIcon = painterResource("files/launcher.png")
    
    Tray(
        icon = appIcon,
        state = rememberTrayState(),
        tooltip = "BundleTool UI",
        onAction = { isMainWindowOpen = true },
        menu = {
            Item("Open BundleTool UI", onClick = { isMainWindowOpen = true })
            Item("Exit", onClick = ::exitApplication)
        }
    )

    if (isMainWindowOpen) {
        Window(
            onCloseRequest = { isMainWindowOpen = false }, // Hide window instead of exitApplication
            state = rememberWindowState(
                width = 1200.dp, height = 900.dp,
                position = WindowPosition(Alignment.Center)
            ),
            title = stringResource(Res.string.app_name),
            icon = appIcon
        ) {
            val splashViewModel = remember { GlobalContext.get().get<SplashViewModel>() }
            val splashState by splashViewModel.uiState.collectAsState()
    
            when (splashState.state) {
                SplashState.CHECKING, SplashState.NEEDS_SETUP, SplashState.DOWNLOADING -> {
                    SplashWindow(viewModel = splashViewModel, onFinish = { /* Handled by state change */ })
                }
                SplashState.DONE -> {
                    App {
                        isMainWindowOpen = false
                    }
                }
            }
        }
    }
}}
