package ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import utils.Strings
import java.awt.FileDialog
import java.awt.Frame

@Composable
fun FileDialog(
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