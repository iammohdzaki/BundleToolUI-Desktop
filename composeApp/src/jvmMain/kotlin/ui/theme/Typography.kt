package ui.theme

import aabtoapk.composeapp.generated.resources.Res
import aabtoapk.composeapp.generated.resources.sans_bold
import aabtoapk.composeapp.generated.resources.sans_medium
import aabtoapk.composeapp.generated.resources.sans_regular
import aabtoapk.composeapp.generated.resources.sans_thin
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Created by Zaki on 21-07-2024.
 */
@Composable
fun SansFontFamily() = FontFamily(
    org.jetbrains.compose.resources.Font(Res.font.sans_thin, FontWeight.Light),
    org.jetbrains.compose.resources.Font(Res.font.sans_regular, FontWeight.Normal),
    org.jetbrains.compose.resources.Font(Res.font.sans_medium, FontWeight.Medium),
    org.jetbrains.compose.resources.Font(Res.font.sans_bold, FontWeight.Bold)
)

@Composable
fun SansTypography() = Typography().run {
    val fontFamily = SansFontFamily()
    copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily)
    )
}