package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.io.File
import org.slf4j.LoggerFactory

@Composable
fun SuccessFolderLink(path: String) {
    val log = LoggerFactory.getLogger("SuccessFolderLink")
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                try {
                    val file = File(path)
                    if (file.exists()) {
                        Desktop.getDesktop().open(file.takeIf { it.isDirectory } ?: file.parentFile)
                    }
                } catch (e: Exception) {
                    log.error("Failed to open folder: $path", e)
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = "Open Folder",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Open Output Folder",
            style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
