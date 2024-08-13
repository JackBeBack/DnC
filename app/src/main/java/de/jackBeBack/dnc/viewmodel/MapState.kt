package de.jackBeBack.dnc.viewmodel

import Enemy
import Grunt
import Player
import Transform
import UnitEntity
import Wizard
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.Utility
import de.jackBeBack.dnc.data.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class MapState(val screenSize: IntSize) : ViewModel() {
    private val _tiles: MutableStateFlow<Array<Tile>> = MutableStateFlow(emptyArray())
    val tiles: StateFlow<Array<Tile>> = _tiles.asStateFlow()

    private val _tilesSize: MutableStateFlow<Pair<Int, Int>> = MutableStateFlow(0 to 0)
    val tilesSize: StateFlow<Pair<Int, Int>> = _tilesSize.asStateFlow()

    private val _size: MutableStateFlow<IntSize> = MutableStateFlow(IntSize.Zero)
    val size: StateFlow<IntSize> = _size.asStateFlow()

    private val _units: MutableStateFlow<Array<UnitEntity>> = MutableStateFlow(emptyArray())
    val units: StateFlow<Array<UnitEntity>> = _units.asStateFlow()

    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.PlayerSelect)
    val gameState = _gameState.asStateFlow()

    private val _canvasOffset: MutableStateFlow<Offset> = MutableStateFlow(Offset.Zero)
    val canvasOffset = _canvasOffset.asStateFlow()

    companion object {
        lateinit var current: MapState
    }

    init {
        current = this
    }


    fun loadMap1(context: Context) {
        _tilesSize.update { 11 to 14 }
        val (x, y) = _tilesSize.value
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.dungeon3)
        _size.update { IntSize(x, y) }
        val tiles: Array<Tile> = Utility.cutImageIntoTiles(bitmap, x, y).map {
            Tile(img = it, imgAlpha = 0.5f, tint = null, tintAlpha = 0.5f)
        }.toTypedArray()
        _tiles.update { tiles }

        _units.update { arrayOf(Wizard(), Grunt()) }
    }

    fun resetTiles() {
        _tiles.update { currentTiles ->
            currentTiles.map { tile ->
                tile.copy(imgAlpha = 1f, tint = null, tintAlpha = 0.5f)
            }.toTypedArray()
        }
    }

    fun enemyTurn(){
        val enemies = units.value.filter { it is Enemy }
        enemies.forEach {
            moveUnit(it, it.position.x + 1 to it.position.y + 0)
        }
        advanceGameState()
    }

    fun advanceGameState() {
        _gameState.update {
            when (it) {
                GameState.PlayerSelect -> GameState.PlayerMove
                GameState.PlayerMove -> GameState.EnemyTurn
                else -> GameState.PlayerSelect
            }
        }
    }

    fun showMoves(pos: Pair<Int, Int>, distance: Int?, sprint: Boolean, color: Color) {
        if (distance == null) return
        val (startX, startY) = pos
        val maxDistance = if (sprint) distance * 2 else distance

        // Update the _tiles state flow
        _tiles.update { currentTiles ->
            currentTiles.mapIndexed { index, tile ->
                val tileY = index % _size.value.height
                val tileX = index / _size.value.height

                val manhattanDistance = Math.abs(tileX - startX) + Math.abs(tileY - startY)

                if (manhattanDistance <= maxDistance) {
                    tile.copy(imgAlpha = 1f, tint = color, tintAlpha = 0.5f)
                } else {
                    tile.copy(imgAlpha = 0.5f)
                }
            }.toTypedArray()
        }
    }

    fun moveUnit(unit: UnitEntity, pos: Pair<Int, Int>) {
        _units.update { currentUnits ->
            currentUnits.map {
                if (it.id == unit.id) {
                    it.update(position = Transform(pos.first, pos.second))
                } else {
                    it
                }
            }.toTypedArray()
        }
    }

    fun moveCanvasToTile(x: Int, y: Int) {
        if (x < 0 || x > tilesSize.value.first) return
        if (y < 0 || y > tilesSize.value.second) return
        if (tiles.value.isEmpty()) return
        val tilePixelSize = tiles.value.first().img.width to tiles.value.first().img.height
        val scale = 1f
        _canvasOffset.update {
            val newX = -(x * tilePixelSize.first + tilePixelSize.first / 2f - screenSize.width / 2f)
            val newY =
                -(y * tilePixelSize.second + tilePixelSize.second / 2f - screenSize.height / 2f)
            Offset(
                newX.coerceIn(-(tilesSize.value.first * tilePixelSize.first * scale), 0f),
                newY.coerceIn(
                    -(tilesSize.value.second * tilePixelSize.second * scale), 0f
                )
            )
        }
    }

    fun getUnitOnTile(x: Int, y: Int): UnitEntity?{
        val ret = units.value.filter {
            it.position.x == x && it.position.y == y
        }
        return if (ret.size == 1) ret.first() else null
    }

    //update methods
    fun updateCanvasOffset(new: Offset) {
        _canvasOffset.update { new }
    }
}

enum class GameState {
    PlayerSelect,
    PlayerMove,
    EnemyTurn
}