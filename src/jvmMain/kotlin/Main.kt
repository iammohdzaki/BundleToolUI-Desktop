import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import theme.Styles
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.text.Style

@Composable
@Preview
fun App() {

    val coroutineScope = rememberCoroutineScope()

    var logs by remember { mutableStateOf("========== Logs View ==========\n\n") }
    var isLoading by remember { mutableStateOf(false) }
    var isOpen by remember { mutableStateOf(false) }
    if (isOpen) {
        FileDialog { fileName, directory ->
            isOpen = false
            val cmd = Constant.getCommand().replace("INPUT_FILE_PATH", "${directory}$fileName")
                .replace("OUTPUT_FILE_NAME", "${directory}${fileName.split(".")[0]}")
            Log.i("Command $cmd")
            logs += "Executing Command : \n$cmd\n"

            coroutineScope.launch(Dispatchers.IO) {
                isLoading = true
                val runtime = Runtime.getRuntime()
                val startTime = System.currentTimeMillis()
                try {
                    val process = runtime.exec(cmd)
                    process.waitFor()
                    val endTime = System.currentTimeMillis()
                    if (process.exitValue() == 0) {
                        Log.i("Command Executed in ${((endTime - startTime) / 1000)}s")
                        logs += "\nCommand Executed in ${((endTime - startTime) / 1000)}s\n"
                        FileHelper.performFileOperations(directory, fileName) { status, message ->
                            isLoading = false
                            Log.i("STATUS - $status\nMESSAGE - $message")
                            logs += "\nFiles Operations Starting...\n$message\n"
                        }
                    }
                } catch (e: Exception) {
                    isLoading = false
                    logs += "Failed -> ${e.printStackTrace()}"
                }
            }
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(12.dp))
            Text(
                text = "Aab To Apk",
                style = Styles.TextStyleBold(28.sp)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 30.dp),
                    strokeWidth = 4.dp
                )
            } else {
                Button(
                    onClick = {
                        isOpen = true
                    },
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                        .fillMaxWidth(0.32f),
                ) {
                    Text(
                        text = "Choose File",
                        style = Styles.TextStyleMedium(16.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                modifier = Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                value = logs,
                textStyle = Styles.TextStyleMedium(16.sp),
                onValueChange = {},
            )
        }
    }
}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (fileName: String, directory: String) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(file, directory)
                }
            }
        }
    },
    dispose = FileDialog::dispose
)


fun main() = application {
    Log.showLogs = true
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
