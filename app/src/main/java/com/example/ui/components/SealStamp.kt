package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrauncesFamily

@Composable
fun SealStamp(
    text: String,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    color: Color = MaterialTheme.colorScheme.tertiary
) {
    Box(
        modifier = modifier
            .rotate(-6f)
            .size(72.dp)
            .drawBehind {
                drawCircle(
                    color = color,
                    radius = size.minDimension / 2 - 2.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .border(3.dp, color, CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text.uppercase(),
                color = color,
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
            if (subtext != null) {
                Text(
                    text = subtext.uppercase(),
                    color = color,
                    fontFamily = FrauncesFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
