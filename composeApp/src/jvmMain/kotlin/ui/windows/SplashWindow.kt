package ui.windows

import aabtoapk.composeapp.generated.resources.Res
import aabtoapk.composeapp.generated.resources.app_name
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import ui.components.FilePickerField
import ui.windows.viewmodel.SplashState
import ui.windows.viewmodel.SplashViewModel

@Composable
fun SplashWindow(
    viewModel: SplashViewModel,
    onFinish: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.state) {
        if (state.state == SplashState.DONE) {
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp).fillMaxWidth()
        ) {
            Image(
                painter = painterResource("files/launcher.png"),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            when (state.state) {
                SplashState.CHECKING -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                SplashState.DOWNLOADING -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    CircularProgressIndicator(
                        progress = { state.downloadProgress },
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                SplashState.NEEDS_SETUP -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "BundleTool is required to run this app.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (state.message.isNotBlank()) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(onClick = { viewModel.downloadBundleTool() }) {
                        Text("Download Automatically (Recommended)")
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    FilePickerField(
                        label = "Already have it?",
                        value = "",
                        placeholder = "Select local bundletool.jar",
                        fileExtensionFilter = ".jar",
                        clickableText = data.model.ClickableText(
                            text = "Download Manually from Browser",
                            url = "https://github.com/google/bundletool/releases/latest"
                        ),
                        onPick = { viewModel.selectLocalFile(it) }
                    )
                }
                SplashState.DONE -> {
                    // Will finish automatically
                }
            }
        }
    }
}