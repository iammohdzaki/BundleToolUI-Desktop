import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import command.CommandBuilder
import command.CommandExecutor
import local.FileStorageHelper
import ui.Styles
import ui.components.ButtonWithToolTip
import ui.components.CheckboxWithText
import ui.components.ChooseFileTextField
import ui.components.CustomTextField
import ui.components.LoadingDialog
import utils.Constant
import utils.DBConstants
import utils.FileDialogType
import utils.FileHelper
import utils.Log
import utils.SigningMode
import utils.Strings
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.net.URI

@Composable
@Preview
fun App(fileStorageHelper: FileStorageHelper, savedPath: String?, adbSavedPath: String?) {
    val density = LocalDensity.current // to calculate the intrinsic size of vector images (SVG, XML)
    val coroutineScope = rememberCoroutineScope()
    var logs by remember { mutableStateOf("") }
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
    var isAutoUnzip by remember { mutableStateOf(true) }
    var savedJarPath by remember { mutableStateOf(savedPath) }
    var isAdbSetupDone by remember { mutableStateOf(false) }
    var adbPath by remember { mutableStateOf("") }
    var showLoadingDialog by remember { mutableStateOf(Pair("", false)) }

    // TODO: (Fixed this issue need to test more!) - Can't update file path once saved, For now Delete path.kb file inside storage directory.
    savedJarPath?.let {
        bundletoolPath = it
        saveJarPath = true
    }

    // Check if ADB Setup is Done or Not
    adbSavedPath?.let {
        adbPath = it
        isAdbSetupDone = true
    }

    if (isOpen && !isLoading) {
        FileDialog { fileName, directory ->
            isOpen = false
            if (fileName.isNullOrEmpty() || directory.isNullOrEmpty()) {
                return@FileDialog
            }
            when (fileDialogType) {
                FileDialogType.BUNDLETOOL -> bundletoolPath = "$directory$fileName"
                FileDialogType.AAPT2 -> aapt2Path = "$directory$fileName"
                FileDialogType.KEY_STORE_PATH -> keyStorePath = "$directory$fileName"
                FileDialogType.ADB_PATH -> {
                    adbPath = "$directory$fileName"
                    // Show Loading Here
                    showLoadingDialog = Pair(Strings.VERIFYING_ADB_PATH, true)
                    CommandExecutor().executeCommand(
                        CommandBuilder()
                            .verifyAdbPath(true, adbPath)
                            .getAdbVerifyCommand(), coroutineScope,
                        onSuccess = {
                            logs += it
                            isAdbSetupDone = true
                            Log.i("Saving Path in DB $adbPath")
                            fileStorageHelper.save(DBConstants.ADB_PATH, adbPath)
                            // Hide Loading
                            Thread.sleep(1000L)
                            showLoadingDialog = Pair(Strings.VERIFYING_ADB_PATH, false)
                        },
                        onFailure = {
                            logs += it
                            isAdbSetupDone = false
                            // Hide Loading
                            showLoadingDialog = Pair(Strings.VERIFYING_ADB_PATH, false)
                        }
                    )
                }

                else -> {
                    aabFilePath = Pair(directory, fileName)
                }
            }
        }
    }

    if (showLoadingDialog.second) {
        LoadingDialog(showLoadingDialog.first)
    }

    if (isExecute) {
        isExecute = false
        // Get Command to Execute
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
            // logs += "Executing Command : \n$cmd\n"
            CommandExecutor()
                .executeCommand(
                    cmd,
                    coroutineScope,
                    onSuccess = {
                        logs += "$it\n"
                        // Do further file operation after new apks is generated
                        // From Auto Zip you can control further file operations.
                        if (isAutoUnzip) {
                            FileHelper.performFileOperations(aabFilePath.first, aabFilePath.second) { status, message ->
                                isLoading = false
                                Log.i("STATUS - $status\nMESSAGE - $message")
                                logs += "\nFiles Operations Starting...\n$message\n"
                            }
                        } else {
                            logs += "\nFile will be saved at ${aabFilePath.first.removeSuffix("\\")}.\n"
                            isLoading = false
                        }

                        // Save Path in Storage
                        // If We don't have any saved path in file storage and save jar path option is checked.Then,we can save new value in storage.
                        if (savedJarPath == null && saveJarPath) {
                            fileStorageHelper.save("path", bundletoolPath)
                        }
                        // If we have saved path in file storage, and it is not changed and save jar path is checked. Then, we can update the value in file storage.
                        else if (savedJarPath != null && savedJarPath != bundletoolPath && saveJarPath) {
                            fileStorageHelper.save("path", bundletoolPath)
                        } else {
                            if (fileStorageHelper.delete("path"))
                                saveJarPath = false
                        }
                    },
                    onFailure = {
                        isLoading = false
                        logs += "Failed -> ${it.printStackTrace()}"
                    }
                )
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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Strings.APP_NAME,
                    style = Styles.TextStyleBold(28.sp),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                )
                ButtonWithToolTip(
                    if (isAdbSetupDone) Strings.ABD_SETUP_DONE else Strings.SETUP_ADB,
                    onClick = {
                        fileDialogType = FileDialogType.ADB_PATH
                        isOpen = true
                    },
                    Strings.SETUP_ADB_INFO,
                    icon = if (isAdbSetupDone) "done" else "info"
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bundle tool select flow
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
            Text(
                text = Strings.FILE_OPTIONS,
                style = Styles.TextStyleBold(16.sp),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )
            CheckboxWithText(
                Strings.AUTO_UNZIP,
                isAutoUnzip,
                onCheckedChange = {
                    isAutoUnzip = it
                },
                Strings.AUTO_UNZIP
            )
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
                        .wrapContentWidth()
                ) {
                    Text(
                        text = Strings.EXECUTE,
                        style = Styles.TextStyleMedium(16.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = Strings.LOGS_VIEW,
                    style = Styles.TextStyleBold(20.sp),
                    modifier = Modifier.padding(start = 0.dp, end = 0.dp, bottom = 8.dp)
                )
                Button(
                    modifier = Modifier.padding(end = 0.dp, bottom = 8.dp),
                    onClick = {
                        logs = ""
                    }
                ) {
                    Text(
                        text = Strings.CLEAR_LOGS,
                        style = Styles.TextStyleBold(13.sp)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Icon(
                        painter = useResource("clear.svg") { loadSvgPainter(it, density) },
                        contentDescription = "Clear",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
            }
            TextField(
                modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                value = logs,
                textStyle = Styles.TextStyleMedium(16.sp),
                onValueChange = {}
            )
        }
    }
}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (fileName: String?, directory: String?) -> Unit
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
    // Check if path for bundletool exists in local storage
    val path = fileStorageHelper.read(DBConstants.BUNDLETOOL_PATH) as String?
    val adbPath = fileStorageHelper.read(DBConstants.ADB_PATH) as String?
    Log.showLogs = true
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 1200.dp, height = 1000.dp,
            position = WindowPosition(Alignment.Center)
        ),
        title = Strings.APP_NAME
    ) {
        App(fileStorageHelper, path, adbPath)
    }
}