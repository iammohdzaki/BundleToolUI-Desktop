package ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.Styles

@Composable
fun TextWithIcon(label: String, onIconClick: () -> Unit) {
    val density = LocalDensity.current // to calculate the intrinsic size of vector images (SVG, XML)
    Row {
        Text(
            text = label,
            style = Styles.TextStyleMedium(16.sp),
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
        Icon(
            painter = useResource("info.svg") { loadSvgPainter(it, density) },
            contentDescription = "Info",
            modifier = Modifier.padding(start = 8.dp)
                .clickable { onIconClick.invoke() }
        )
    }
}