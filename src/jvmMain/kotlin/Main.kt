import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import java.awt.FileDialog
import java.awt.Frame

@Composable
@Preview
fun App() {

    var logs by remember { mutableStateOf("Logs:") }

    var isOpen by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    if (isOpen) {
        FileDialog { fileName, directory ->
            logs = "Logs: "
            isOpen = false
            val cmd = Constant.getCommand().replace("INPUT_FILE_PATH", "${directory}$fileName")
                .replace("OUTPUT_FILE_NAME", "${directory}${fileName.split(".")[0]}")
            Log.i("Command $cmd")
            showLoading = true
            val runtime = Runtime.getRuntime()
            val startTime = System.currentTimeMillis()
            val process = runtime.exec(cmd)
            process.waitFor()
            val endTime = System.currentTimeMillis()
            logs += if (process.exitValue() == 0) {
                Log.i("Command Executed in ${((endTime - startTime) / 1000)}s")
                "Command Executed in ${((endTime - startTime) / 1000)}s\n"
            }else{
                "Failed while running command"
            }

            FileHelper.performFileOperations(directory, fileName) { status, message ->
                Log.i("STATUS - $status\nMESSAGE - $message")
                logs += "STATUS - $status\nMESSAGE - $message"
                showLoading = false
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
                style = TextStyle(
                    fontSize = 28.sp
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
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
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = logs,
                style = TextStyle(
                    color = Color.Blue,
                    fontSize = 18.sp
                )
            )
            if (showLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 64.dp),
                    strokeWidth = 6.dp
                )
            }
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
