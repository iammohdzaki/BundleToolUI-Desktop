package ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.Styles

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ButtonWithToolTip(
    label: String,
    onClick: () -> Unit,
    toolTipText: String = "",
    icon: String = "info",
    buttonColors: ButtonColors = ButtonDefaults.buttonColors()
) {
    val density = LocalDensity.current // to calculate the intrinsic size of vector images (SVG, XML)
    Button(
        onClick = {
            onClick.invoke()
        },
        colors = buttonColors,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            .wrapContentWidth()
    ) {
        Text(
            text = label,
            style = Styles.TextStyleMedium(16.sp),
            color = Color.White,
            fontWeight = FontWeight.Medium
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
                            color = Color.Black,
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
            ) {
                Icon(
                    painter = useResource("$icon.svg") { loadSvgPainter(it, density) },
                    contentDescription = icon,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}