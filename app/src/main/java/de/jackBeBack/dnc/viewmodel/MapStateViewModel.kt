package de.jackBeBack.dnc.viewmodel

import Player
import Transform
import UnitEntity
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.Utility
import de.jackBeBack.dnc.data.Attack
import de.jackBeBack.dnc.data.DamageType
import de.jackBeBack.dnc.data.Enemy.Grunt
import de.jackBeBack.dnc.data.Player.Wizard
import de.jackBeBack.dnc.data.Tile
import de.jackBeBack.dnc.data.TileType
import de.jackBeBack.dnc.data.map1Types
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import use
import java.util.UUID
import kotlin.math.max

class MapStateViewModel(val screenSize: IntSize) : ViewModel() {
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

    private var _selectedUnit = MutableStateFlow<UnitEntity?>(null)
    val selectedUnit: StateFlow<UnitEntity?> = _selectedUnit

    private var _selectedPlayer = MutableStateFlow<UUID?>(null)
    val selectedPlayer: Flow<Player?> = _selectedPlayer.map { uuid ->
        uuid?.let { nonNullUUID ->
            _units.value.firstOrNull { it.id == nonNullUUID } as? Player
        }
    }

    private var _isAttacking = MutableStateFlow(false)
    val isAttacking: StateFlow<Boolean> = _isAttacking

    private var _attack = MutableStateFlow<Attack?>(null)
    val attack: StateFlow<Attack?> = _attack

    private var _selectedAttack = MutableStateFlow<Attack?>(null)
    val selectedAttack: StateFlow<Attack?> = _selectedAttack

    companion object {
        lateinit var global: MapStateViewModel
    }

    init {
        global = this
    }

    fun getTile(x: Int, y: Int): Tile? {
        return if (x in 0 until tilesSize.value.first && y in 0 until tilesSize.value.second) {
            tiles.value[y + tilesSize.value.second * x]
        } else {
            null
        }
    }


    fun loadMap1(context: Context) {
        _tilesSize.update { 11 to 14 }
        val (x, y) = _tilesSize.value
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.dungeon3)
        _size.update { IntSize(x, y) }
        val tiles: Array<Tile> =
            Utility.cutImageIntoTiles(bitmap, x, y).mapIndexed() { index, value ->
                Tile(
                    img = value,
                    imgAlpha = 0.5f,
                    tint = null,
                    tintAlpha = 0.5f,
                    map1Types.get(index)
                )
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

    suspend fun enemyTurn() {
        delay(500)
        val enemies = units.value.filter { !it.isPlayer() }
        enemies.forEach {
            val ret = it.ai(this, it, getUnitById(_selectedPlayer.value))
            moveUnit(it, ret)
        }
        advanceGameState(GameState.PlayerSelect)
        _units.update {
            it.map {
                if (it.isPlayer()) {
                    it.resetActions()
                } else {
                    it
                }
            }.toTypedArray()
        }
    }

    fun advanceGameState(new: GameState) {
        _gameState.update {
            new
        }
    }

    fun updateUnit(new: UnitEntity?) {
        if (new == null) return
        _units.update { list ->
            list.map {
                if (it.id == new.id) new else it
            }.toTypedArray()
        }
    }

    fun generatePointsAtDistance(
        origin: Transform,
        distance: Int,
        filter: (Int, Int) -> Boolean = { _, _ -> true }
    ): List<Transform> {
        val (originX, originY) = origin
        val points = mutableSetOf<Transform>()

        for (dx in -distance..distance) {
            val dy = distance - Math.abs(dx)

            // Add points in all four quadrants
            points.add(Transform(originX + dx, originY + dy))
            points.add(Transform(originX + dx, originY - dy))
        }

        // Apply custom filter and return as list
        return points.filter { (x, y) ->
            getTile(x, y)?.type == TileType.ACCESSIBLE && filter(
                x,
                y
            )
        }.toList()
    }

    fun select(new: UnitEntity?) {
        _selectedUnit.update { new }
    }

    fun showMoves(pos: Transform?, distance: Int?, sprint: Boolean, color: Color) {
        if (distance == null) return
        if (pos == null) return
        val (startX, startY) = pos
        val maxDistance = if (sprint) distance * 2 else distance

        // Update the _tiles state flow
        _tiles.update { currentTiles ->
            currentTiles.mapIndexed { index, tile ->
                val tileY = index % _size.value.height
                val tileX = index / _size.value.height

                val manhattanDistance = Math.abs(tileX - startX) + Math.abs(tileY - startY)

                if (manhattanDistance <= maxDistance && tile.type != TileType.INACCESSIBLE) {
                    tile.copy(imgAlpha = 1f, tint = color, tintAlpha = 0.5f)
                } else {
                    tile.copy(imgAlpha = 0.5f)
                }
            }.toTypedArray()
        }
    }

    fun showAreaOfEffect(attack: Attack, tint: Color = Color.Red){
        _tiles.update { currentTiles ->
            currentTiles.mapIndexed { index, tile ->
                val tileY = index % _size.value.height
                val tileX = index / _size.value.height

                if (attack.areaOfEffect(attack.source?.position, Transform(tileX, tileY))) {
                    tile.copy(imgAlpha = 1f, tint = tint, tintAlpha = 0.5f)
                } else {
                    tile.copy(imgAlpha = 0.5f)
                }
            }.toTypedArray()
        }
    }

    fun moveUnit(unit: UnitEntity, pos: Transform?) {
        if (unit.isPlayer()) {
            advanceGameState(GameState.PlayerSelect)
        }
        if (pos == null) return
        if (pos.x !in 0..tilesSize.value.first) return
        if (pos.y !in 0..tilesSize.value.second) return
        if (getTile(pos.x, pos.y)?.type == TileType.INACCESSIBLE) return
        if (unit.position.x == pos.x && unit.position.y == pos.y) {
            _selectedUnit.update { null }
            return
        }
        if (!unit.isPlayer() || unit.hasAction()) {
            _units.update { currentUnits ->
                currentUnits.map {
                    if (it.id == unit.id) {
                        it.update(
                            position = Transform(pos.x, pos.y),
                            action = it.action.use()
                        )
                    } else {
                        it
                    }
                }.toTypedArray()
            }
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

    fun getUnitOnTile(x: Int, y: Int): UnitEntity? {
        val ret = units.value.filter {
            it.position.x == x && it.position.y == y
        }
        return if (ret.size == 1) ret.first() else null
    }

    //update methods
    fun updateCanvasOffset(new: Offset) {
        _canvasOffset.update { new }
    }

    fun attack(
        attack: Attack?
    ) {
        if (attack?.source?.hasAction() == true) {
            _attack.update {
                attack
            }
        } else {
            advanceGameState(GameState.PlayerSelect)
        }
    }

    fun applyDamage() {
        var appliedDamage = false
        attack.value?.let { a ->
            _units.update { currentUnits ->
                currentUnits.map { unit ->
                    if (unit.position == a.target) {
                        //Unit is the Target
                        val ret = a.effectOnTarget(unit)
                        if (ret != null) {
                            appliedDamage = true
                            ret
                        }else{
                            unit
                        }
                    } else if (
                        unit.id == a.source?.id
                    ) {
                        //Unit is the Source
                        a.effectOnSource(unit) ?: unit
                    } else {
                        unit
                    }
                }.toTypedArray()
            }
            if (appliedDamage) {
                _attack.update { null }
                _selectedAttack.update { null }
            }
        }
    }

    fun setIsAttacking(new: Boolean) {
        _isAttacking.update { new }
    }

    fun selectPlayer(new: UnitEntity?) {
        _selectedPlayer.update { new?.id }
    }

    private fun getUnitById(id: UUID?): UnitEntity? {
        return _units.value.firstOrNull { it.id == id }
    }

    fun setSelectedAttack(new: Attack) {
        _selectedAttack.update { new }
    }

}

enum class GameState {
    PlayerSelect,
    PlayerMove,
    PlayerAttack,
    EnemyTurn
}