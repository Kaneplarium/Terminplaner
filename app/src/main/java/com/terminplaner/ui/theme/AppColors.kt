package com.terminplaner.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object AppColors {
    val Red = Color(0xFFE53935)
    val Yellow = Color(0xFFFFEB3B)
    val Green = Color(0xFF4CAF50)
    val Blue = Color(0xFF2196F3)
    val Pink = Color(0xFFE91E63)

    val presetColors = listOf(
        Red, Yellow, Green, Blue, Pink
    )

    fun getColorInt(color: Color): Int = color.toArgb()

    fun fromArgb(argb: Int): Color = Color(argb)
}