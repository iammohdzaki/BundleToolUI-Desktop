import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import local.FileStorageHelper
import theme.Styles
import theme.components.CheckboxWithText
import theme.components.ChooseFileTextField
import theme.components.CustomTextField
import utils.*
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import utils.Strings
import java.nio.file.Paths

@Composable
@Preview
fun App(fileStorageHelper: FileStorageHelper, savedPath: String?) {
    val coroutineScope = rememberCoroutineScope()
    var logs by remember { mutableStateOf("========== Logs View ==========\n\n") }
    var bundletoolPath by remember { mutableStateOf("") }
    var aabFilePath by remember { mutableStateOf(Pair<String, String>("", "")) }
    var isLoading by remember { mutableStateOf(false) }
    var isOpen by remember { mutableStateOf(false) }
    var isExecute by remember { mutableStateOf(false) }
    var fileDialogType by remember { mutableStateOf(0) }
    var saveJarPath by remember { mutableStateOf(false) }
    var isOverwrite by remember { mutableStateOf(false) }
    var isAapt2PathEnabled by remember { mutableStateOf(false) }
    var aapt2Path by remember { mutableStateOf("") }
    var isUniversalMode by remember { mutableStateOf(true) }
    var signingMode by remember { mutableStateOf(SigningMode.DEBUG) }
    var keyStorePath by remember { mutableStateOf("") }
    var keyStorePassword by remember { mutableStateOf("") }
    var keyAlias by remember { mutableStateOf("") }
    var keyPassword by remember { mutableStateOf("") }

    savedPath?.let {
        bundletoolPath = it
        saveJarPath = true
    }

    if (isOpen && !isLoading) {
        FileDialog { fileName, directory ->
            isOpen = false
            when (fileDialogType) {
                FileDialogType.BUNDLETOOL -> bundletoolPath = "${directory}$fileName"
                FileDialogType.AAPT2 -> aapt2Path = "${directory}$fileName"
                FileDialogType.KEY_STORE_PATH -> keyStorePath = "${directory}$fileName"
                else -> {
                    aabFilePath = Pair(directory, fileName)
                }
            }
        }
    }

    if (isExecute) {
        isExecute = false
        //Get Command to Execute
        val (cmd, isValid) = CommandBuilder()
            .bundletoolPath(bundletoolPath)
            .aabFilePath(aabFilePath)
            .isOverwrite(isOverwrite)
            .isUniversalMode(isUniversalMode)
            .isAapt2PathEnabled(isAapt2PathEnabled)
            .aapt2Path(aapt2Path)
            .signingMode(signingMode)
            .keyStorePath(keyStorePath)
            .keyStorePassword(keyStorePassword)
            .keyAlias(keyAlias)
            .keyPassword(keyPassword)
            .validateAndGetCommand()
        if (isValid) {
            Log.i("Command $cmd")
            logs += "Executing Command : \n$cmd\n"
            coroutineScope.launch(Dispatchers.IO) {
                //Save Path in Storage
                if (savedPath == null) {
                    fileStorageHelper.save("path", bundletoolPath)
                } else {
                    if(fileStorageHelper.delete("path"))
                        saveJarPath = false
                }

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
        } else {
            Log.i("Error $cmd")
            logs += "\nError -> $cmd"
            isLoading = false
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(modifier = Modifier.padding(12.dp))
            Text(
                text = Strings.APP_NAME,
                style = Styles.TextStyleBold(28.sp),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Bundle tool select flow
                ChooseFileTextField(
                    bundletoolPath,
                    Strings.SELECT_BUNDLETOOL_JAR,
                    onSelect = {
                        fileDialogType = FileDialogType.BUNDLETOOL
                        isOpen = true
                    }
                )
                CheckboxWithText(
                    Strings.SAVE_JAR_PATH,
                    saveJarPath,
                    onCheckedChange = {
                        saveJarPath = it
                    },
                    Strings.SAVE_JAR_PATH_INFO
                )
            }
            val downloadInfo = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                    append(Strings.DOWNLOAD_BUNDLETOOL)
                }
                addStringAnnotation(
                    tag = Strings.URL,
                    annotation = Constant.BUNDLE_DOWNLOAD_LINK,
                    start = 0,
                    end = length
                )
            }
            ClickableText(
                text = downloadInfo,
                style = Styles.TextStyleMedium(14.sp),
                modifier = Modifier.padding(start = 16.dp),
                onClick = { offset ->
                    val annotations = downloadInfo.getStringAnnotations(Strings.URL, offset, offset)
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
                Strings.SELECT_AAB_FILE,
                onSelect = {
                    fileDialogType = FileDialogType.AAB
                    isOpen = true
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = Strings.OPTIONS_FOR_BUILD_APKS,
                style = Styles.TextStyleBold(20.sp),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CheckboxWithText(
                    Strings.OVERWRITE,
                    isOverwrite,
                    onCheckedChange = {
                        isOverwrite = it
                    },
                    Strings.OVERWRITE_INFO
                )
                CheckboxWithText(
                    Strings.MODE_UNIVERSAL,
                    isUniversalMode,
                    onCheckedChange = {
                        isUniversalMode = it
                    },
                    Strings.MODE_UNIVERSAL_INFO
                )
                CheckboxWithText(
                    Strings.AAPT2_PATH,
                    isAapt2PathEnabled,
                    onCheckedChange = {
                        isAapt2PathEnabled = it
                    },
                    Strings.AAPT2_PATH_INFO
                )
            }
            if (isAapt2PathEnabled) {
                ChooseFileTextField(
                    aapt2Path,
                    Strings.SELECT_AAPT2_FILE,
                    onSelect = {
                        fileDialogType = FileDialogType.AAPT2
                        isOpen = true
                    }
                )
            }
            Text(
                text = Strings.SIGNING_MODE,
                style = Styles.TextStyleBold(16.sp),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CheckboxWithText(
                    Strings.DEBUG,
                    signingMode == SigningMode.DEBUG,
                    onCheckedChange = {
                        signingMode = if (it) SigningMode.DEBUG
                        else SigningMode.RELEASE
                    }
                )
                CheckboxWithText(
                    Strings.RELEASE,
                    signingMode == SigningMode.RELEASE,
                    onCheckedChange = {
                        signingMode = if (it) SigningMode.RELEASE
                        else SigningMode.DEBUG
                    }
                )
            }
            if (signingMode == SigningMode.RELEASE) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChooseFileTextField(
                        keyStorePath,
                        Strings.KEYSTORE_PATH,
                        onSelect = {
                            fileDialogType = FileDialogType.KEY_STORE_PATH
                            isOpen = true
                        }
                    )
                    CustomTextField(
                        keyStorePassword,
                        Strings.KEYSTORE_PASSWORD,
                        onValueChange = {
                            keyStorePassword = it
                        }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomTextField(
                        keyAlias,
                        Strings.KEY_ALIAS,
                        forPassword = false,
                        onValueChange = {
                            keyAlias = it
                        }
                    )
                    CustomTextField(
                        keyPassword,
                        Strings.KEY_PASSWORD,
                        onValueChange = {
                            keyPassword = it
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 40.dp)
                        .padding(start = 16.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
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
                        text = Strings.EXECUTE,
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
        object : FileDialog(parent, Strings.CHOOSE_FILE, LOAD) {
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
    val fileStorageHelper = FileStorageHelper()
    //Check if path for bundletool exists in local storage
    val path = fileStorageHelper.read("path") as String?
    Log.showLogs = true
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 1000.dp, height = 1000.dp,
            position = WindowPosition(Alignment.Center)
        ),
        title = Strings.APP_NAME
    ) {
        App(fileStorageHelper, path)
    }
}