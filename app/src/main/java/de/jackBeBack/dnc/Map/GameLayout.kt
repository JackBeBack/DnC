package de.jackBeBack.dnc.Map

import Enemy
import MapCanvas
import Player
import UnitEntity
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.util.wrapMappedColumns
import de.jackBeBack.dnc.data.DamageType
import de.jackBeBack.dnc.ui.theme.Actions
import de.jackBeBack.dnc.ui.theme.BottomSheet
import de.jackBeBack.dnc.ui.theme.BottomSheetMenuType
import de.jackBeBack.dnc.ui.theme.BottomSheetNavigation
import de.jackBeBack.dnc.ui.theme.Chip
import de.jackBeBack.dnc.ui.theme.UnitActions
import de.jackBeBack.dnc.ui.theme.UnitInfo
import de.jackBeBack.dnc.ui.views.Attacks
import de.jackBeBack.dnc.viewmodel.GameState
import de.jackBeBack.dnc.viewmodel.MapStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameLayout() {
    val context = LocalContext.current
    val mapStateViewModel = remember { MapStateViewModel.global }
    val gameState by mapStateViewModel.gameState.collectAsState()
    val bottomSheetNavigation = remember { BottomSheetNavigation.global }

    val scope = rememberCoroutineScope()

    val selected by mapStateViewModel.selectedUnit.collectAsState()

    val isBottomSheetOpen by bottomSheetNavigation.isBottomSheetOpen.collectAsState()

    val tiles by mapStateViewModel.tiles.collectAsState()
    val size by mapStateViewModel.size.collectAsState()
    val units by mapStateViewModel.units.collectAsState()

    val player by remember { derivedStateOf { units.firstOrNull { it.isPlayer() } } }
    val selectedAttack by mapStateViewModel.selectedAttack.collectAsState()

    LaunchedEffect(Unit) {
        mapStateViewModel.loadMap1(context)
        delay(500)
        player?.position?.let { p ->
            mapStateViewModel.moveCanvasToTile(p.x, p.y)
        }
    }

    LaunchedEffect(player) {
        mapStateViewModel.selectPlayer(player)
    }

    LaunchedEffect(selected) {
        if (gameState != GameState.PlayerAttack) {
            if (selected?.isPlayer() == true) {
                bottomSheetNavigation.changeBottomSheetOpen(true)
            } else {
                mapStateViewModel.showMoves(selected?.position, selected?.speed, false, Color.Yellow)

            }
        } else {
            //is PlayerAttack
        }
    }

    LaunchedEffect(selectedAttack) {
        if (selected != null) {
            bottomSheetNavigation.changeBottomSheetOpen(false)
        }
    }

    var lastTap by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    var unitOnTap: UnitEntity? = null

    LaunchedEffect(gameState) {
        if (gameState == GameState.EnemyTurn) mapStateViewModel.enemyTurn()
    }

    if (units.firstOrNull { !it.isPlayer() && !it.isDead() } == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("YOU WIN", modifier = Modifier.align(Alignment.Center))
            Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = {
                mapStateViewModel.loadMap1(context)
            }) {
                Text("RESET")
            }
        }
    } else if (units.firstOrNull { it.isPlayer() && !it.isDead() } == null){
        Box(modifier = Modifier.fillMaxSize()) {
            Text("YOU LOSE", modifier = Modifier.align(Alignment.Center))
            Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = {
                mapStateViewModel.loadMap1(context)
            }) {
                Text("RESET")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            MapCanvas(tiles, size.width, size.height, selected) { x, y ->
                //On Tile Click
                lastTap = x to y
                unitOnTap = units.firstOrNull {
                    it.position.x == lastTap?.first && it.position.y == lastTap?.second
                }
                mapStateViewModel.select(unitOnTap)
                mapStateViewModel.resetTiles()
            }
            Chip(
                gameState.toString(),
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
            ) {

                Actions(Modifier, player)
                Chip("End Turn",
                    Modifier
                        .clickable { mapStateViewModel.advanceGameState(GameState.EnemyTurn) })
            }
        }
    }

    BottomSheet(
        isBottomSheetOpen,
        onDismissRequest = {
            lastTap = null
            bottomSheetNavigation.changeBottomSheetOpen(false)
            bottomSheetNavigation.changeMenu(BottomSheetMenuType.ACTION)
            mapStateViewModel.select(null)
            mapStateViewModel.resetTiles()
        },
    ) { nav ->
        val type by nav.menuType.collectAsState()

        selected?.let { unit ->
            if (unit.isPlayer()) {
                when (type) {
                    BottomSheetMenuType.ACTION -> UnitActions(unit, nav)
                    BottomSheetMenuType.INFO -> UnitInfo(unit, nav)
                    BottomSheetMenuType.ATTACKS -> Attacks(player?.attacks, player, nav)
                }
            }
        }
    }
}