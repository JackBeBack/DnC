package de.jackBeBack.dnc.ui.theme

import Player
import UnitEntity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.jackBeBack.dnc.viewmodel.GameState
import de.jackBeBack.dnc.viewmodel.MapStateViewModel

@Composable
fun UnitActions(unit: UnitEntity, nav: BottomSheetNavigation) {
    val mapStateViewModel = remember { MapStateViewModel.global }
    Column(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            if (unit.isPlayer()) {
                Text("Actions: ${unit.action}")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                nav.changeMenu(BottomSheetMenuType.ATTACKS)
            }) {
                Text("Attack")
            }
            Button(onClick = {
                mapStateViewModel.showMoves(
                    pos = unit.position,
                    distance = 1,
                    false,
                    Color.Cyan
                )
                nav.changeBottomSheetOpen(false)
                mapStateViewModel.advanceGameState(GameState.PlayerMove)
            }) {
                Text("Move")
            }
            Button(onClick = { nav.changeMenu(BottomSheetMenuType.INFO) }) {
                Text("Info")
            }
        }
    }
}