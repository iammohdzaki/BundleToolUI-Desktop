package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.model.ClickableText
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.JFileChooser

@Composable
fun FilePickerField(
    label: String,
    value: String,
    placeholder: String,
    dialogTitle: String = "Select File",
    clickableText: ClickableText? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    fileExtensionFilter: String? = null, // e.g. ".aab" or ".jks",
    selectFolder: Boolean = false,
    onPick: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        LaunchedEffect(Unit) {
            val path = if (selectFolder)
                pickFolder(dialogTitle)
            else
                pickFile(dialogTitle, fileExtensionFilter)

            if (path != null) onPick(path)
            showDialog = false
        }
    }

    Column(modifier = modifier) {

        // Label (above field)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Field + Icon (in one Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(8.dp)
                ).clickable { showDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File path text
            Text(
                text = value.ifEmpty { placeholder },
                color = if (value.isNotEmpty())
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )

            // Folder button
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(
                            topEnd = 8.dp,
                            bottomEnd = 8.dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selectFolder) Icons.Default.Folder else Icons.Default.FolderOpen,
                    contentDescription = if (selectFolder) "Select Folder" else "Select File",
                    tint = Color.White
                )
            }
        }
        clickableText?.let {
            ClickableLinkText(
                text = it.text,
                url = it.url,
                highlightColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Opens a native desktop file dialog and returns the selected path (or null if canceled).
 */
fun pickFile(title: String, extension: String?): String? {
    return try {
        val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD).apply {
            isMultipleMode = false
            if (extension != null) file = "*$extension"
        }
        dialog.isVisible = true
        dialog.file?.let { file ->
            dialog.directory + file
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Opens a native folder picker dialog (works on desktop JVM).
 */
fun pickFolder(title: String): String? {
    return try {
        val chooser = JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false
        }
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}