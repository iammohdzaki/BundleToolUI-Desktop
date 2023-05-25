import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
@Preview
fun App() {

    var isOpen by remember { mutableStateOf(false) }

    if (isOpen) {
        FileDialog(
            onCloseRequest = {
                isOpen = false
                val file = File(it)
                println("Result ${file.absolutePath}")
            }
        )
    }

    MaterialTheme {
        Button(
            onClick = {
                isOpen = true
            },
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth(0.32f),
        ) {
            Text(
                text = "Choose File",
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(file)
                }
            }
        }
    },
    dispose = FileDialog::dispose
)


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 500.dp, height = 500.dp,
            position = WindowPosition(Alignment.Center)
        ),
        title = "Aab To Apk"
    ) {
        App()
    }
}
