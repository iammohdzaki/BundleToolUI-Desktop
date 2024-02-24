import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import theme.Styles
import theme.components.ChooseFileTextField
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI

@Composable
@Preview
fun App() {
    val coroutineScope = rememberCoroutineScope()
    var logs by remember { mutableStateOf("========== Logs View ==========\n\n") }
    var bundletoolPath by remember { mutableStateOf("") }
    var aabFilePath by remember { mutableStateOf(Pair<String, String>("", "")) }
    var isLoading by remember { mutableStateOf(false) }
    var isOpen by remember { mutableStateOf(false) }
    var isExecute by remember { mutableStateOf(false) }
    var isBundletool by remember { mutableStateOf(false) }

    if (isOpen && !isLoading) {
        FileDialog { fileName, directory ->
            isOpen = false
            if (isBundletool) {
                //Handle Error for Unknown files here
                bundletoolPath = "${directory}$fileName"
            } else {
                aabFilePath = Pair(directory, fileName)
            }
        }
    }

    if (isExecute) {
        isExecute = false
        //Get Command to Execute
        val cmd =
            Constant.getCommand(bundletoolPath).replace("INPUT_FILE_PATH", "${aabFilePath.first}${aabFilePath.second}")
                .replace("OUTPUT_FILE_NAME", "${aabFilePath.first}${aabFilePath.second}".split(".")[0])
        Log.i("Command $cmd")
        logs += "Executing Command : \n$cmd\n"

        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            val runtime = Runtime.getRuntime()
            val startTime = System.currentTimeMillis()
            try {
                //Launch Runtime to execute command
                val process = runtime.exec(cmd)
                // Read and log error output
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                Log.i("Process Error Output:")
                while (errorReader.readLine().also { logs += "\n ERROR -> $it" } != null) {
                    isLoading = false
                }
                process.waitFor()
                val endTime = System.currentTimeMillis()
                if (process.exitValue() == 0) {
                    Log.i("Command Executed in ${((endTime - startTime) / 1000)}s")
                    logs += "\nCommand Executed in ${((endTime - startTime) / 1000)}s\n"
                    // Do further file operation after new apks is generated
                    FileHelper.performFileOperations(aabFilePath.first, aabFilePath.second) { status, message ->
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

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(modifier = Modifier.padding(12.dp))
            Text(
                text = "Android Bundletool UI",
                style = Styles.TextStyleBold(28.sp),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            //Bundle tool select flow
            ChooseFileTextField(
                bundletoolPath,
                "Select Bundletool Jar",
                onSelect = {
                    isOpen = true
                    isBundletool = true
                }
            )
            val downloadInfo = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Blue,textDecoration = TextDecoration.Underline)) {
                    append("Download Bundletool from here")
                }
                addStringAnnotation(
                    tag = "URL",
                    annotation = "https://github.com/google/bundletool/releases",
                    start = 0,
                    end = length
                )
            }
            ClickableText(
                text = downloadInfo,
                style = Styles.TextStyleNormal(14.sp),
                modifier = Modifier.padding(start = 16.dp),
                onClick = {offset ->
                    val annotations = downloadInfo.getStringAnnotations("URL", offset, offset)
                    if (annotations.isNotEmpty()) {
                        val uri = URI(annotations.first().item)
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(uri)
                        } else {
                            // Desktop not supported, handle as necessary
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            ChooseFileTextField(
                "${aabFilePath.first}${aabFilePath.second}",
                "Select Aab File",
                onSelect = {
                    isOpen = true
                    isBundletool = false
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 40.dp).padding(start = 16.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                    strokeWidth = 4.dp
                )
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        isExecute = true
                    },
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                        .wrapContentWidth(),
                ) {
                    Text(
                        text = "Execute",
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
            width = 800.dp, height = 800.dp,
            position = WindowPosition(Alignment.Center)
        ),
        title = "Aab To Apk"
    ) {
        App()
    }
}