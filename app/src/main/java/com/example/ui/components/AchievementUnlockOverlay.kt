package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.Achievement
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun AchievementUnlockOverlay(
    achievement: Achievement?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = achievement != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (achievement == null) return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Confetti()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(96.dp)
                ) {
                    Icon(
                        imageVector = when(achievement.iconRes) {
                            "fire" -> Icons.Rounded.LocalFireDepartment
                            "timer" -> Icons.Rounded.Timer
                            "repeat" -> Icons.Rounded.Repeat
                            else -> Icons.Rounded.CheckCircle
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(24.dp).fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Achievement Unlocked!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Nice!")
                }
            }
        }

        // Auto dismiss after 4 seconds if not clicked
        LaunchedEffect(achievement) {
            delay(4000)
            onDismiss()
        }
    }
}

class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var rotation: Float,
    var rotSpeed: Float
)

@Composable
fun Confetti() {
    var particles by remember { mutableStateOf(emptyList<ConfettiParticle>()) }
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan)
    
    LaunchedEffect(Unit) {
        val p = List(100) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = -0.1f - Random.nextFloat() * 0.5f,
                vx = (Random.nextFloat() - 0.5f) * 0.02f,
                vy = 0.01f + Random.nextFloat() * 0.02f,
                color = colors.random(),
                size = 10f + Random.nextFloat() * 20f,
                rotation = Random.nextFloat() * 360f,
                rotSpeed = (Random.nextFloat() - 0.5f) * 10f
            )
        }
        particles = p
        
        while(isActive) {
            delay(16)
            particles = particles.map {
                it.apply {
                    x += vx
                    y += vy
                    rotation += rotSpeed
                }
            }.filter { it.y < 1.2f }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val px = p.x * size.width
            val py = p.y * size.height
            rotate(p.rotation, Offset(px + p.size/2, py + p.size/2)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(px, py),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
