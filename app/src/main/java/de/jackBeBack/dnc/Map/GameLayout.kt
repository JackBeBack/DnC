package de.jackBeBack.dnc.Map

import MapCanvas
import UnitEntity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.jackBeBack.dnc.distanceTo
import de.jackBeBack.dnc.ui.theme.BottomSheet
import de.jackBeBack.dnc.ui.theme.Chip
import de.jackBeBack.dnc.ui.theme.UnitInfo
import de.jackBeBack.dnc.viewmodel.GameState
import de.jackBeBack.dnc.viewmodel.MapState

@Composable
fun GameLayout() {
    val context = LocalContext.current
    val gameState by MapState.current.gameState.collectAsState()

    LaunchedEffect(Unit) {
        MapState.current.loadMap1(context)
    }
    LaunchedEffect(gameState) {
        if (gameState != GameState.PlayerMove) MapState.current.resetTiles()
    }
    val tiles by MapState.current.tiles.collectAsState()
    val size by MapState.current.size.collectAsState()
    val units by MapState.current.units.collectAsState()

    var selectedUnit by remember { mutableStateOf<UnitEntity?>(null) }


    var showBottomSheet by remember { mutableStateOf(false) }
    var lastTap by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    var unitOnTap: UnitEntity? = null
    LaunchedEffect(lastTap) {
        unitOnTap = units.firstOrNull {
            it.position.x == lastTap?.first && it.position.y == lastTap?.second
        }

        if (unitOnTap != null) {
            if (gameState == GameState.PlayerSelect) {
                selectedUnit = unitOnTap
                showBottomSheet = true
            }
        }
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.EnemyTurn) MapState.current.enemyTurn()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapCanvas(tiles, size.width, size.height) { x, y ->
            lastTap = x to y
            if (selectedUnit != null && gameState == GameState.PlayerMove && MapState.current.getUnitOnTile(x, y) == null) {
                val distance = selectedUnit?.position?.distanceTo(x, y)
                if ((distance ?: 0) <= (selectedUnit?.speed ?: 0)){
                    MapState.current.moveUnit(selectedUnit!!, x to y)
                    MapState.current.advanceGameState()
                    lastTap = null
                }
            }
        }
        Chip(gameState.toString(), Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
    }



    BottomSheet(
        showBottomSheet,
        onDismissRequest = {
            lastTap = null
            showBottomSheet = false
        },
    ) {
        UnitInfo(selectedUnit,
            onMove = {
                MapState.current.advanceGameState()
                lastTap?.let {
                    MapState.current.showMoves(it, selectedUnit?.speed,true, Color.Yellow)
                    MapState.current.showMoves(it, selectedUnit?.speed,false, Color.Green)
                }
                showBottomSheet = false
            })
    }
}