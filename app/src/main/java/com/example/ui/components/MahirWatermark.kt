package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MahirColors

/**
 * "Powered by Mahir" branding watermark.
 * Shows at the bottom of screens.
 */
@Composable
fun MahirWatermark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mahir",
            style = MaterialTheme.typography.labelSmall,
            color = MahirColors.gold(),
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
        Text(
            text = "Verse",
            style = MaterialTheme.typography.labelSmall,
            color = MahirColors.gold().copy(alpha = 0.6f),
            fontWeight = FontWeight.Light
        )
    }
}
