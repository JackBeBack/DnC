package de.jackBeBack.dnc

import Transform
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs

class Utility {
    companion object{
        fun cutImageIntoTiles(bitmap: Bitmap, x: Int, y: Int): Array<ImageBitmap> {
            val width = bitmap.width / x
            val height = bitmap.height / y

            val tiles = Array(x * y) { ImageBitmap(width, height) }
            var index = 0
            for (i in 0 until x) {
                for (j in 0 until y) {
                    val tileBitmap = Bitmap.createBitmap(bitmap, i * width, j * height, width, height)
                    tiles[index++] = tileBitmap.asImageBitmap()
                }
            }
            return tiles
        }

        @Composable
        fun getScreenSizeInPixels(): IntSize {
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            return with(density) {
                IntSize(configuration.screenWidthDp.dp.toPx().toInt(), configuration.screenHeightDp.dp.toPx().toInt())
                 }
        }
    }
}

fun Transform.distanceTo(x: Int, y: Int): Int{
    return abs(x - this.x) + abs(y - this.y)
}