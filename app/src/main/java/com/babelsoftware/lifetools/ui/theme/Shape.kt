package com.babelsoftware.lifetools.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // Butonlar, kartlar gibi orta boyutlu bileşenler için
    large = RoundedCornerShape(24.dp), // Modal pencereler, alt sayfalar gibi büyük bileşenler için
    extraLarge = RoundedCornerShape(32.dp)
)