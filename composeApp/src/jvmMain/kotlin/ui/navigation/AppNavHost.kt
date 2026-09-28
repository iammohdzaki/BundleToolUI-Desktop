package ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import ui.windows.HomeWindow
import ui.windows.SettingsWindow
import ui.windows.viewmodel.HomeViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.HomeRoute
    ) {
        composable<Routes.HomeRoute> {
            val viewModel = koinViewModel<HomeViewModel>()
            HomeWindow(viewModel)
        }
        composable<Routes.SettingsRoute> {
            SettingsWindow()
        }
    }
}