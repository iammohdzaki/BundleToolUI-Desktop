package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.Styles

@Composable
fun CustomTextField(value: String, label: String, forPassword: Boolean = true, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(0.4f).height(50.dp)
                .border(
                    BorderStroke(width = 2.dp, color = MaterialTheme.colors.primary),
                    shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp, topStart = 10.dp, bottomStart = 10.dp)
                ),
            value = value,
            singleLine = true,
            visualTransformation = if (forPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            label = {
                Text(
                    text = label,
                    style = Styles.TextStyleMedium(16.sp),
                    fontWeight = FontWeight.Medium,
                )
            },
            textStyle = Styles.TextStyleMedium(16.sp),
            onValueChange = {
                onValueChange.invoke(it)
            },
        )
    }
}