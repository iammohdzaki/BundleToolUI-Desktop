package ui

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

object Styles {

    fun TextStyleNormal(size: TextUnit) =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontFamily = codeFontFamily,
            fontSize = size
        )

    fun TextStyleMedium(size: TextUnit) =
        TextStyle(
            fontWeight = FontWeight.Medium,
            fontFamily = codeFontFamily,
            fontSize = size
        )

    fun TextStyleSemiBold(size: TextUnit) =
        TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontFamily = codeFontFamily,
            fontSize = size
        )

    fun TextStyleBold(size: TextUnit) =
        TextStyle(
            fontWeight = FontWeight.Bold,
            fontFamily = codeFontFamily,
            fontSize = size
        )
}