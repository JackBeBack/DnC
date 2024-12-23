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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.scale
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.Utility
import de.jackBeBack.dnc.data.DamageType
import de.jackBeBack.dnc.data.Tile
import de.jackBeBack.dnc.data.TileType
import de.jackBeBack.dnc.ui.theme.BottomSheetNavigation
import de.jackBeBack.dnc.viewmodel.GameState
import de.jackBeBack.dnc.viewmodel.MapStateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

val DEBUG = false

@Composable
fun MapCanvas(
    tiles: Array<Tile>,
    x: Int,
    y: Int,
    selectedUnit: UnitEntity?,
    bottomSheetNavigation: BottomSheetNavigation,
    onClick: (Int, Int) -> Unit
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
    val scope = rememberCoroutineScope()

    val units by mapStateViewModel.units.collectAsState()
    val attack by mapStateViewModel.attack.collectAsState()
    val selectedAttack by mapStateViewModel.selectedAttack.collectAsState()
    val gameState by mapStateViewModel.gameState.collectAsState()

    val player by remember { derivedStateOf { units.firstOrNull { it.isPlayer() } } }

    var isAttacking by remember { mutableStateOf(false) }
    var attackOffset by remember {
        mutableStateOf(attack?.target?.let {
            Offset(
                ((attack!!.target?.x ?: 0) * sizeX).toFloat(),
                ((attack!!.target?.y ?: 0) * sizeY).toFloat()
            )
        } ?: Offset.Zero)
    }
    val attackAnimation = animateOffsetAsState(attackOffset, animationSpec = if (isAttacking) tween(
        ATTACK_DURATION.toInt()
    ) else snap(), finishedListener = {
        if (isAttacking) {
            scope.launch {
                mapStateViewModel.applyDamage()
            }
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
                ((attack!!.target?.x ?: 0) * sizeX).toFloat(),
                ((attack!!.target?.y ?: 0) * sizeY).toFloat()
            )
            mapStateViewModel.updateUnit(attack?.source?.useAction(1))
        }
    }

    val attackAngle = attack?.getDirection() ?: 0F
    var fist by remember { mutableStateOf<ImageBitmap?>(null) } // Initialize with placeholder/null.
    var fireball by remember { mutableStateOf<ImageBitmap?>(null) } // Initialize with placeholder/null.
    var iceshard by remember { mutableStateOf<ImageBitmap?>(null) } // Initialize with placeholder/null.
    val dice = remember { BitmapFactory.decodeResource(context.resources, R.drawable.dice).scale(sizeX/4, sizeY/4).asImageBitmap() }
    val check = remember { BitmapFactory.decodeResource(context.resources, R.drawable.check).scale(sizeX/4, sizeY/4).asImageBitmap() }
    val cross = remember { BitmapFactory.decodeResource(context.resources, R.drawable.cross).scale(sizeX/4, sizeY/4).asImageBitmap() }

    // Update the image resources whenever attack or context changes.
    LaunchedEffect(attack, context) {
        // Load your Bitmaps and process them in the background.
        withContext(Dispatchers.IO) {
            fist = Utility.rotateBitmap(
                BitmapFactory.decodeResource(context.resources, R.drawable.fist),
                attackAngle
            ).scale(sizeX / 2, sizeY / 2).asImageBitmap()

            fireball = Utility.rotateBitmap(
                BitmapFactory.decodeResource(context.resources, R.drawable.fireball),
                attackAngle
            ).scale(sizeX / 2, sizeY / 2).asImageBitmap()

            iceshard = Utility.rotateBitmap(
                BitmapFactory.decodeResource(context.resources, R.drawable.iceshard),
                attackAngle
            ).scale(sizeX / 2, sizeY / 2).asImageBitmap()
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
                    val unitOnTile = mapStateViewModel.getUnitOnTile(tileX, tileY)
                    when (gameState) {
                        GameState.PlayerSelect -> {
                            if (unitOnTile?.isPlayer() == false){
                                bottomSheetNavigation.changeBottomSheetOpen(true)
                            }
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
                                scope.launch {
                                    mapStateViewModel.attack(
                                        selectedAttack?.copy(
                                            source = player,
                                            target = Transform(tileX, tileY)
                                        )
                                    )
                                }
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
                val (visibleStart, visibleEnd) = calculateVisibleTiles(
                    screenSize.toSize(),
                    scale,
                    -offset,
                    tileSize = Size(sizeX.toFloat(), sizeY.toFloat()),
                    tileCount = Size(x.toFloat(), y.toFloat())
                )
                for (i in visibleStart.x.toInt() until visibleEnd.x.toInt()) {
                    for (j in visibleStart.y.toInt() until visibleEnd.y.toInt()) {
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
                                    //hp bars
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
                                    //Roll Success
                                    if (unit.successRoll != null){
                                        drawImage(dice,
                                            Offset(
                                                unitAnimations[index].first.value,
                                                unitAnimations[index].second.value
                                            ).plus(Offset(sizeX*3/4f, sizeY*3/4f))
                                        )
                                        drawImage(if (unit.successRoll == true) check else cross,
                                            Offset(
                                                unitAnimations[index].first.value,
                                                unitAnimations[index].second.value
                                            ).plus(Offset(sizeX*3/4f, sizeY*3/4f))
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
                    val attackSprite = when (attack?.type) {
                        DamageType.PHYSICAL -> fist
                        DamageType.FIRE -> fireball
                        DamageType.ICE -> iceshard
                        else -> null
                    }

                    if (attackSprite != null) {
                        drawImage(
                            attackSprite,
                            attackAnimation.value.plus(Offset(sizeX / 4f, sizeY / 4f))
                        )
                    }

                }
            }
        }
    }
}

fun calculateVisibleTiles(
    screenSize: Size,
    scale: Float,
    offset: Offset,
    tileSize: Size,
    tileCount: Size
): Pair<Offset, Offset> {
    val startX =
        (((offset.x * scale) / tileSize.width) - tileSize.width).coerceIn(0f, tileCount.width)
    val startY =
        (((offset.y * scale) / tileSize.height) - tileSize.height).coerceIn(0f, tileCount.height)

    val endX =
        ((((offset.x + screenSize.width) * scale) / tileSize.width) + tileSize.width).coerceIn(
            0f,
            tileCount.width
        )
    val endY =
        ((((offset.y + screenSize.height) * scale) / tileSize.height) + tileSize.height).coerceIn(
            0f,
            tileCount.height
        )

    return Pair(Offset(startX, startY), Offset(endX, endY))
}

fun isPointVisible(
    screenSize: Size,
    scale: Float,
    translateOffset: Offset,
    pointOffset: Offset,
    tileSize: Size
): Boolean {
    // Calculate visible area
    val visibleArea = calculateVisibleTiles(
        screenSize,
        scale,
        translateOffset,
        tileSize,
        Size(Float.MAX_VALUE, Float.MAX_VALUE)
    )

    // Check if the point is within the visible area
    return pointOffset.x >= visibleArea.first.x &&
            pointOffset.y >= visibleArea.first.y &&
            pointOffset.x <= visibleArea.second.x &&
            pointOffset.y <= visibleArea.second.y
}