package ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
fun ClickableLinkText(
    text: String,
    url: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
) {
    val uriHandler = LocalUriHandler.current

    val annotatedText = buildAnnotatedString {
        append(text)
        addStyle(
            style = SpanStyle(
                color = highlightColor,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline
            ),
            start = 0,
            end = text.length
        )
        addStringAnnotation(
            tag = "URL",
            annotation = url,
            start = 0,
            end = text.length
        )
    }

    Text(
        text = annotatedText,
        style = textStyle,
        modifier = modifier
            .clickable {
                annotatedText.getStringAnnotations("URL", 0, text.length)
                    .firstOrNull()?.let {
                        uriHandler.openUri(it.item)
                    }
            }
    )
}