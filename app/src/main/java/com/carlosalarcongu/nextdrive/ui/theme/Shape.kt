package com.carlosalarcongu.nextdrive.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),  // Para Checkboxes y etiquetas
    medium = RoundedCornerShape(6.dp), // Para Botones y Tarjetas (Corte minimalista)
    large = RoundedCornerShape(8.dp)   // Para Diálogos y paneles grandes
)