package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ui.Styles

@Composable
fun LoadingDialog(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        elevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .border(
                    BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Dialog(
                title = text,
                onCloseRequest = { },
                undecorated = false,
                resizable = false,
                enabled = false
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight()
                ) {
                    Text(
                        text = text,
                        style = Styles.TextStyleBold(20.sp),
                        modifier = Modifier.padding(start = 8.dp,end = 8.dp, bottom = 8.dp)
                    )
                    CircularProgressIndicator()
                }
            }
        }

    }

}