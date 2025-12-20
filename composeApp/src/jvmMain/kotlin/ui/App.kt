package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import ui.navigation.AppNavHost
import ui.theme.AppTheme

@Preview
@Composable
fun App(onCloseWindow: () -> Unit) {
    KoinContext {
        AppTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .consumeWindowInsets(WindowInsets.safeDrawing) // tell Compose we handle them
                    .padding(WindowInsets.safeDrawing.asPaddingValues()) // apply padding
            ) {
                AppNavHost()
            }
        }
    }
}