import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun LabelRow(text: String) {
    Text(
        text = "$text: ",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
    )
}