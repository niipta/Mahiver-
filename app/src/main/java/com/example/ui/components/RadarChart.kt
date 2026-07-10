package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class RadarData(
    val label: String,
    val progress: Float, // 0.0 to 1.0
    val color: Color
)

@Composable
fun RadarChart(data: List<RadarData>) {
    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
            Text("Add subjects to see balance chart", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    if (data.size == 1) {
        Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
            Text("Add more subjects for balance chart", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.fillMaxWidth().height(240.dp).padding(16.dp)) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = min(centerX, centerY) * 0.8f // leave room for labels
        
        val numSides = data.size
        val angleStep = (2 * Math.PI / numSides).toFloat()

        // 1. Draw 5 concentric polygon rings
        for (i in 1..5) {
            val ringRadius = maxRadius * (i / 5f)
            val ringPath = Path()
            for (j in 0 until numSides) {
                val angle = j * angleStep - Math.PI.toFloat() / 2f // start from top
                val x = centerX + ringRadius * cos(angle)
                val y = centerY + ringRadius * sin(angle)
                if (j == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = outlineColor,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw axis lines
        for (j in 0 until numSides) {
            val angle = j * angleStep - Math.PI.toFloat() / 2f
            val x = centerX + maxRadius * cos(angle)
            val y = centerY + maxRadius * sin(angle)
            drawLine(
                color = outlineColor,
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
            
            // 4. Draw labels
            val labelRadius = maxRadius * 1.15f
            val lx = centerX + labelRadius * cos(angle)
            val ly = centerY + labelRadius * sin(angle)
            val labelText = data[j].label
            
            val textLayoutResult = textMeasurer.measure(
                text = labelText,
                style = TextStyle(fontSize = 12.sp, color = labelColor)
            )
            val textWidth = textLayoutResult.size.width
            val textHeight = textLayoutResult.size.height
            
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(lx - textWidth / 2f, ly - textHeight / 2f)
            )
        }

        // 3. Draw filled data polygon
        val dataPath = Path()
        for (j in 0 until numSides) {
            val angle = j * angleStep - Math.PI.toFloat() / 2f
            val progress = data[j].progress * animationProgress.value
            val valRadius = maxRadius * progress
            val x = centerX + valRadius * cos(angle)
            val y = centerY + valRadius * sin(angle)
            if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()
        
        drawPath(
            path = dataPath,
            color = Color(0xFFFFD700).copy(alpha = 0.3f)
        )
        drawPath(
            path = dataPath,
            color = Color(0xFFFFD700),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
