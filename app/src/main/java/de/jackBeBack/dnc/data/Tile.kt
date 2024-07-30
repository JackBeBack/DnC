package de.jackBeBack.dnc.data

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

@Stable
data class Tile(val img: ImageBitmap,
                val imgAlpha: Float,
                val tint: Color?,
                val tintAlpha: Float)