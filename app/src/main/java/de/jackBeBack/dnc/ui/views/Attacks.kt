package de.jackBeBack.dnc.ui.views

import UnitEntity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.jackBeBack.dnc.data.Attack
import de.jackBeBack.dnc.ui.theme.ResourceBar
import de.jackBeBack.dnc.viewmodel.GameState
import de.jackBeBack.dnc.viewmodel.MapStateViewModel

@Composable
fun Attacks(attacks: List<Attack>?, player: UnitEntity?) {
    if (attacks == null) return
    Column {
        player?.let { ResourceBar("Mana", it.mp, Color.Blue) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(attacks) { attack ->
                Attack(attack = attack, player)
            }
        }
    }
}

@Composable
fun Attack(attack: Attack, player: UnitEntity?) {
    val mapStateViewModel = remember { MapStateViewModel.global }
    Card(
        modifier = Modifier
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(2.dp),
        enabled = player?.hasAction() == true && attack.effectOnSource(player) != null,
        onClick = {
            mapStateViewModel.advanceGameState(GameState.PlayerAttack)
            mapStateViewModel.setSelectedAttack(attack)
            mapStateViewModel.showAreaOfEffect(attack.copy(source = player))
        }) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(attack.name, modifier = Modifier.align(Alignment.Center))
        }
    }
}