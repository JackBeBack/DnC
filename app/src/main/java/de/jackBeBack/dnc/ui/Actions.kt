package de.jackBeBack.dnc.ui.theme

import Player
import UnitEntity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.jackBeBack.dnc.R

@Composable
fun Actions(modifier: Modifier, player: UnitEntity?) {
    if (player == null) return
    Column(modifier.animateContentSize()) {
        Row {
            val image = painterResource(id = R.drawable.action) // Replace with your image resource ID
            repeat(player.action.current){
                Image(
                    painter = image,
                    contentDescription = "My Image Description", // Provide a content description
                    modifier = Modifier.size(25.dp) // Apply any desired modifiers
                )
            }
        }
    }
}