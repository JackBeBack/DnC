import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.scale
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.Utility
import de.jackBeBack.dnc.data.DamageType
import de.jackBeBack.dnc.data.Tile
import de.jackBeBack.dnc.data.TileType
import de.jackBeBack.dnc.viewmodel.GameState
import de.jackBeBack.dnc.viewmodel.MapStateViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

val DEBUG = false

@Composable
fun MapCanvas(
    tiles: Array<Tile>, x: Int, y: Int, selectedUnit: UnitEntity?, onClick: (Int, Int) -> Unit
) {
    if (tiles.isEmpty()) return
    val ATTACK_DURATION = 1000L
    val mapStateViewModel = remember { MapStateViewModel.global }
    var scale by remember { mutableStateOf(1f) }
    val offset by mapStateViewModel.canvasOffset.collectAsState()
    val screenSize = Utility.getScreenSizeInPixels()

    val res = LocalContext.current.resources

    val animatedX by animateFloatAsState(offset.x, label = "animated X value")
    val animatedY by animateFloatAsState(offset.y, label = "animated Y value")

    val (sizeX, sizeY) = tiles.first().img.width to tiles.first().img.height

    val context = LocalContext.current

    val units by mapStateViewModel.units.collectAsState()
    val attack by mapStateViewModel.attack.collectAsState()
    val selectedAttack by mapStateViewModel.selectedAttack.collectAsState()
    val gameState by mapStateViewModel.gameState.collectAsState()

    val player by remember { derivedStateOf { units.firstOrNull { it.isPlayer() } } }

    var isAttacking by remember { mutableStateOf(false) }
    var attackOffset by remember {
        mutableStateOf(attack?.target?.let {
            Offset(
                (attack!!.target!!.x * sizeX).toFloat(),
                (attack!!.target!!.y * sizeY).toFloat()
            )
        } ?: Offset.Zero)
    }
    val attackAnimation = animateOffsetAsState(attackOffset, animationSpec = if (isAttacking) tween(
        ATTACK_DURATION.toInt()
    ) else snap(), finishedListener = {
        if (isAttacking) {
            mapStateViewModel.applyDamage()
            isAttacking = false
            mapStateViewModel.advanceGameState(GameState.PlayerSelect)
        }
    })

    LaunchedEffect(attack) {
        if (attack?.target == null || attack?.source == null) return@LaunchedEffect

        isAttacking = false
        attackOffset = Offset(
            (attack!!.source!!.position.x * sizeX).toFloat(),
            (attack!!.source!!.position.y * sizeY).toFloat()
        )

        if (attack?.source?.hasAction() == true) {
            delay(500)
            isAttacking = true
            attackOffset = Offset(
                (attack!!.target!!.x * sizeX).toFloat(),
                (attack!!.target!!.y * sizeY).toFloat()
            )
            mapStateViewModel.updateUnit(attack?.source?.useAction(1))
        }
    }


    val unitAnimations = units.map { unitEntity ->
        animateFloatAsState(
            (unitEntity.position.x * sizeX).toFloat(),
            label = "x", animationSpec = tween(1000)
        ) to
                animateFloatAsState(
                    (unitEntity.position.y * sizeY).toFloat(),
                    label = "y", animationSpec = tween(1000)
                )
    }

    val attackAngel = attack?.getDirection() ?: 0F
    val fist = Utility.rotateBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.fist), attackAngel).scale(sizeX/2, sizeY/2).asImageBitmap()

    Canvas(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                //scale = (scale * zoom).coerceIn(0.5f, 4f)
                scale = 1f
                val newX = (offset.x + pan.x).coerceIn(
                    -(x * sizeX.toFloat() * scale - screenSize.width), 0f
                )
                val newY = (offset.y + pan.y).coerceIn(
                    -(y * sizeY.toFloat() * scale - screenSize.height), 0f
                )
                mapStateViewModel.updateCanvasOffset(Offset(newX, newY))
            }
        }
        .pointerInput(units, selectedUnit, selectedAttack, player) {
            detectTapGestures(onTap = { tapOffset ->
                // Convert tapOffset to canvas coordinates
                val canvasX = (tapOffset.x + abs(offset.x))
                val canvasY = (tapOffset.y + abs(offset.y))

                // Determine the tile indices
                val tileX = (canvasX / sizeX).toInt()
                val tileY = (canvasY / sizeY).toInt()

                if (tileX in 0 until x && tileY in 0 until y) {
                    onClick(tileX, tileY)
                    when (gameState) {
                        GameState.PlayerSelect -> {

                        }

                        GameState.PlayerMove -> {
                            selectedUnit?.let {
                                mapStateViewModel.moveUnit(
                                    it,
                                    Transform(tileX, tileY)
                                )
                            }
                        }

                        GameState.PlayerAttack -> {
                            if (selectedAttack != null) {
                                mapStateViewModel.attack(
                                    selectedAttack?.copy(
                                        source = player,
                                        target = Transform(tileX, tileY)
                                    )
                                )
                            }
                        }

                        GameState.EnemyTurn -> {

                        }
                    }
                } else {
                    println("Tap was outside the grid")
                }
            })
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
                        } else {
                            val c = when (tile.type) {
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
                    }
                }
                units.forEachIndexed { index, unit ->
                    for (j in 0 until y) {
                        for (i in 0 until x) {
                            if (unit.position.x == i && unit.position.y == j) {
                                if (unit.isDead()) {

                                } else {
                                    val img =
                                        BitmapFactory.decodeResource(context.resources, unit.resId)
                                            .scale(sizeX, sizeY).asImageBitmap()
                                    drawImage(
                                        img,
                                        Offset(
                                            unitAnimations[index].first.value,
                                            unitAnimations[index].second.value
                                        )
                                    )
                                    if (true) {
                                        drawRect(
                                            Color.Companion.Red,
                                            Offset(
                                                unitAnimations[index].first.value,
                                                unitAnimations[index].second.value
                                            ),
                                            Size(sizeX.toFloat(), 10f)
                                        )
                                        drawRect(
                                            Color.Companion.Green,
                                            Offset(
                                                unitAnimations[index].first.value,
                                                unitAnimations[index].second.value
                                            ),
                                            Size(sizeX.toFloat() * unit.hp.percentage(), 10f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (isAttacking) {
                    val halfSize = Offset(
                        -sizeX.toFloat() / 2,
                        -sizeY.toFloat() / 2
                    )
                    when (attack?.type) {
                        DamageType.PHYSICAL -> {
                            drawImage(
                                fist,
                                attackAnimation.value.plus(Offset(sizeX/4f, sizeY/4f))
                            )
                        }

                        else -> drawCircle(
                            Color.Red,
                            50f,
                            attackAnimation.value.minus(halfSize)
                        )
                    }
                }
            }
        }
    }
}