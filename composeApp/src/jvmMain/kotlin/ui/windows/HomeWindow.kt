package ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ui.components.NavigationItem
import ui.components.Sidebar
import ui.windows.screens.BuildApksScreen
import ui.windows.viewmodel.HomeViewModel

@Composable
fun HomeWindow(viewModel: HomeViewModel) {
    var selectedItem by remember { mutableStateOf(NavigationItem.BUILD_APKS) }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        Sidebar(
            selectedItem = selectedItem,
            onItemSelected = { selectedItem = it }
        )

        androidx.compose.material3.VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Content Area
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (selectedItem) {
                NavigationItem.BUILD_APKS -> BuildApksScreen(viewModel)
                NavigationItem.EXTRACT_APKS -> ui.windows.screens.ExtractApksScreen(
                    viewModel = org.koin.compose.koinInject(),
                    onNavigateToDeviceSpec = { selectedItem = NavigationItem.GET_DEVICE_SPEC }
                )
                NavigationItem.INSTALL_APKS -> ui.windows.screens.InstallApksScreen(org.koin.compose.koinInject())
                NavigationItem.GET_DEVICE_SPEC -> ui.windows.screens.GetDeviceSpecScreen(org.koin.compose.koinInject())
                NavigationItem.GET_SIZE -> ui.windows.screens.GetSizeScreen(
                    viewModel = org.koin.compose.koinInject(),
                    onNavigateToDeviceSpec = { selectedItem = NavigationItem.GET_DEVICE_SPEC }
                )
                NavigationItem.SETTINGS -> ui.windows.screens.SettingsScreen(org.koin.compose.koinInject())
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}
