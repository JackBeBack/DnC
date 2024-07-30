import android.graphics.BitmapFactory
import android.graphics.ColorFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.Utility
import de.jackBeBack.dnc.data.Tile
import kotlin.math.abs

@Composable
fun MapCanvas(tiles: Array<Tile>, units: Array<UnitEntity>, x: Int, y: Int, onClick: (Int, Int) -> Unit) {
    if (tiles.isEmpty()) return
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(-1174f, -1869f)) }

    val animatedX by animateFloatAsState(offset.x)
    val animatedY by animateFloatAsState(offset.y)

    val screenSize = Utility.getScreenSizeInPixels()

    val (sizeX, sizeY) = tiles.first().img.width to tiles.first().img.height

    val context = LocalContext.current

    Canvas(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(0.5f, 4f)
                val newX =
                    (offset.x + pan.x).coerceIn(
                        -(x * sizeX.toFloat() * scale - screenSize.width),
                        0f
                    )
                val newY =
                    (offset.y + pan.y).coerceIn(
                        -(y * sizeY.toFloat() * scale - screenSize.height),
                        0f
                    )
                offset = Offset(newX, newY)
            }
        }
        .pointerInput(units) {
            detectTapGestures(
                onTap = { tapOffset ->
                    // Convert tapOffset to canvas coordinates
                    val canvasX = (tapOffset.x + abs(offset.x))
                    val canvasY = (tapOffset.y + abs(offset.y))

                    println("$offset")
                    println("$sizeX, $sizeY")
                    println("${tapOffset.x + offset.x}, ${tapOffset.y + offset.y}")

                    // Determine the tile indices
                    val tileX = (canvasX / sizeX).toInt()
                    val tileY = (canvasY / sizeY).toInt()

                    if (tileX in 0 until x && tileY in 0 until y) {
                        onClick(tileX, tileY)
                    } else {
                        println("Tap was outside the grid")
                    }
                }
            )
        }) {
        scale(scale) {
            translate(animatedX, animatedY) {
                for (i in 0 until x) {
                    for (j in 0 until y) {
                        val tile = tiles[j + y * i]
                        val topLeft = Offset((i * sizeX).toFloat(), (j * sizeY).toFloat())
                        drawImage(
                            tile.img,
                            topLeft,
                            alpha = tile.imgAlpha,
                        )
                        tile.tint?.let {
                            drawRect(
                                it,
                                topLeft = topLeft,
                                alpha = tile.tintAlpha,
                                size = Size(tile.img.width.toFloat(), tile.img.height.toFloat())
                            )
                        }
                        for(unit in units){
                            if(unit.position.x == i && unit.position.y == j) {
                                val img = BitmapFactory.decodeResource(context.resources, unit.resId).scale(sizeX, sizeY)
                                    .asImageBitmap()
                                drawImage(
                                    img,
                                    Offset((i * sizeX).toFloat(), (j * sizeY).toFloat())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}