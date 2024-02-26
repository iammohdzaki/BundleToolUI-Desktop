package theme.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.Styles

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckboxWithText(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, toolTipText: String = "") {
    val density = LocalDensity.current // to calculate the intrinsic size of vector images (SVG, XML)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange.invoke(it) },
        )
        Text(
            text = label,
            style = Styles.TextStyleMedium(14.sp),
        )
        if (toolTipText.isNotEmpty()) {
            TooltipArea(
                tooltip = {
                    // composable tooltip content
                    Surface(
                        modifier = Modifier.shadow(4.dp),
                        color = Color(255, 255, 210),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = toolTipText,
                            style = Styles.TextStyleMedium(12.sp),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                },
                delayMillis = 200, // in milliseconds
                tooltipPlacement = TooltipPlacement.CursorPoint(
                    alignment = Alignment.BottomEnd,
                    offset = DpOffset.Zero // tooltip offset
                )
            ){
                Icon(
                    painter = useResource("info.svg") { loadSvgPainter(it, density) },
                    contentDescription = "Info",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}