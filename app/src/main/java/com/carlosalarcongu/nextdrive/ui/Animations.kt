package com.carlosalarcongu.nextdrive.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun SpeedometerLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "speedometer")
    val currentSpeed by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f, // Medio círculo
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "needle"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.size(80.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 8.dp.toPx()

            // Fondo del velocímetro (Gris claro)
            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = 180f, sweepAngle = 180f, useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Zona roja (Revoluciones altas)
            drawArc(
                color = Color(0xFFE53935),
                startAngle = 315f, sweepAngle = 45f, useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Aguja indicadora
            rotate(degrees = currentSpeed, pivot = center) {
                drawLine(
                    color = primaryColor,
                    start = center,
                    end = Offset(center.x - 24.dp.toPx(), center.y), // Apunta a la izquierda (0 vel)
                    strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round
                )
            }
            // Centro de la aguja
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = center)
            drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
        }
    }
}