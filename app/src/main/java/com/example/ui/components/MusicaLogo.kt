package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MusicaLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(com.example.ui.theme.LighterCream)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize(0.6f)
        ) {
            val width = size.width
            val height = size.height
            
            val strokeWidth = width * 0.12f
            val color = com.example.ui.theme.DeepSoil
            val accentColor = com.example.ui.theme.ActiveAccentColor
            
            // Left stem
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.85f),
                end = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.15f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            // Right stem
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.15f),
                end = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.85f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            // Diagonal connection
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(width * 0.15f, height * 0.15f)
                lineTo(width * 0.5f, height * 0.7f)
                lineTo(width * 0.85f, height * 0.15f)
            }
            drawPath(
                path = path,
                color = accentColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
            
            // Music note circles at endpoints
            drawCircle(
                color = accentColor,
                radius = strokeWidth * 1.2f,
                center = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.85f)
            )
            
            drawCircle(
                color = color,
                radius = strokeWidth * 1.2f,
                center = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.15f)
            )
        }
    }
}
