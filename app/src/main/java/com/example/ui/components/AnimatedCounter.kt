package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Smoothly animates an integer counter, ideal for stat cards.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight = FontWeight.Bold
) {
    var displayedValue by remember { mutableStateOf(targetValue) }

    LaunchedEffect(targetValue) {
        if (targetValue == displayedValue) return@LaunchedEffect
        val diff = targetValue - displayedValue
        val steps = 12
        val stepSize = diff / steps
        repeat(steps) {
            displayedValue += stepSize
            kotlinx.coroutines.delay(20)
        }
        displayedValue = targetValue
    }

    AnimatedContent(
        targetState = displayedValue,
        transitionSpec = {
            (slideInVertically(animationSpec = tween(200)) { it / 4 } + fadeIn(tween(200))) togetherWith
                (slideOutVertically(animationSpec = tween(200)) { -it / 4 } + fadeOut(tween(200)))
        },
        modifier = modifier,
        label = "counter"
    ) { value ->
        Text(
            text = value.toString(),
            style = style,
            fontWeight = fontWeight
        )
    }
}
