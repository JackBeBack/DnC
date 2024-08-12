package de.jackBeBack.dnc.viewmodel

import Player
import Transform
import UnitEntity
import Wizard
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.Utility
import de.jackBeBack.dnc.data.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MapState : ViewModel() {
    private val _tiles: MutableStateFlow<Array<Tile>> = MutableStateFlow(emptyArray())
    val tiles: StateFlow<Array<Tile>> = _tiles.asStateFlow()

    private val _size: MutableStateFlow<IntSize> = MutableStateFlow(IntSize.Zero)
    val size: StateFlow<IntSize> = _size.asStateFlow()

    private val _units: MutableStateFlow<Array<UnitEntity>> = MutableStateFlow(emptyArray())
    val units: StateFlow<Array<UnitEntity>> = _units.asStateFlow()

    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.PlayerSelect)
    val gameState = _gameState.asStateFlow()


    fun loadMap1(context: Context) {
        val (x, y) = 11 to 14
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.dungeon3)
        _size.update { IntSize(x, y) }
        val tiles: Array<Tile> = Utility.cutImageIntoTiles(bitmap, x, y).map {
            Tile(img = it, imgAlpha = 0.5f, tint = null, tintAlpha = 0.5f)
        }.toTypedArray()
        _tiles.update { tiles }

        _units.update { arrayOf(Wizard()) }
    }

    fun resetTiles() {
        _tiles.update { currentTiles ->
            currentTiles.map { tile ->
                tile.copy(imgAlpha = 1f, tint = null, tintAlpha = 0.5f)
            }.toTypedArray()
        }
    }

    fun advanceGameState() {
        _gameState.update {
            when (it) {
                GameState.PlayerSelect -> GameState.PlayerMove
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
                    if (it is Player) it.update(position = Transform(pos.first, pos.second)) else it
                } else {
                    it
                }
            }.toTypedArray()
        }
    }
}

enum class GameState {
    PlayerSelect,
    PlayerMove,
}