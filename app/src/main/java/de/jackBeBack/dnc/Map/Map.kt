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
import de.jackBeBack.dnc.data.TileType
import de.jackBeBack.dnc.viewmodel.GameState
import de.jackBeBack.dnc.viewmodel.MapState
import kotlinx.coroutines.delay
import kotlin.math.abs

val DEBUG = false

@Composable
fun MapCanvas(tiles: Array<Tile>, x: Int, y: Int, onClick: (Int, Int) -> Unit) {
    if (tiles.isEmpty()) return
    var scale by remember { mutableStateOf(1f) }
    val offset by MapState.current.canvasOffset.collectAsState()
    val screenSize = Utility.getScreenSizeInPixels()

    val animatedX by animateFloatAsState(offset.x, label = "animated X value")
    val animatedY by animateFloatAsState(offset.y, label = "animated Y value")

    val (sizeX, sizeY) = tiles.first().img.width to tiles.first().img.height

    val context = LocalContext.current

    val units by MapState.current.units.collectAsState()
    val gameState by MapState.current.gameState.collectAsState()

    LaunchedEffect(gameState) {
        when(gameState){
            GameState.PlayerSelect -> {
                units.forEach {
                    if (it is Player) MapState.current.moveCanvasToTile(it.position.x, it.position.y)
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        /*
        for(i in 0 until x){
            for (j in 0 until y){
                delay(500)
                MapState.current.moveCanvasToTile(i, j)
                println("Moving to Tile ($i, $j)")
            }
        }
         */
    }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                //scale = (scale * zoom).coerceIn(0.5f, 4f)
                scale = 1f
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
                MapState.current.updateCanvasOffset(Offset(newX, newY))
            }
        }
        .pointerInput(units) {
            detectTapGestures(
                onTap = { tapOffset ->
                    // Convert tapOffset to canvas coordinates
                    val canvasX = (tapOffset.x + abs(offset.x))
                    val canvasY = (tapOffset.y + abs(offset.y))

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
                        if (!DEBUG) {
                            tile.tint?.let {
                                drawRect(
                                    it,
                                    topLeft = topLeft,
                                    alpha = tile.tintAlpha,
                                    size = Size(tile.img.width.toFloat(), tile.img.height.toFloat())
                                )
                            }
                        }else{
                            val c = when(tile.type){
                                TileType.ACCESSIBLE -> {
                                    Color.Green
                                }
                                TileType.INACCESSIBLE -> {
                                    Color.Red
                                }
                                TileType.SLOW -> {
                                    Color.Yellow
                                }
                            }
                            drawRect(
                                c,
                                topLeft = topLeft,
                                alpha = 0.5f,
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