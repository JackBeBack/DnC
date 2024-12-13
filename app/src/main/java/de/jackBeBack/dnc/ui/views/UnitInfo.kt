package de.jackBeBack.dnc.ui.theme

import Player
import UnitEntity
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import de.jackBeBack.dnc.data.Player.Wizard

@Composable
fun UnitInfo(selectedUnit: UnitEntity?, nav: BottomSheetNavigation) {
    if (selectedUnit == null) return
    val context = LocalContext.current
    val img = BitmapFactory.decodeResource(context.resources, selectedUnit.resId).scale(400, 400)
        .asImageBitmap()

    Column(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp)
    ) {
        Row {
            Column {
                Text("Str:")
                Text("Dex:")
                Text("Con:")
                Text("Int:")
                Text("Wis:")
                Text("Cha:")
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text("${selectedUnit.stats.strength}")
                Text("${selectedUnit.stats.dexterity}")
                Text("${selectedUnit.stats.constitution}")
                Text("${selectedUnit.stats.intelligent}")
                Text("${selectedUnit.stats.wisdom}")
                Text("${selectedUnit.stats.charisma}")
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                //Image(img, contentDescription = "Unit Image", modifier = Modifier.size(65.dp))
                Text("${selectedUnit.name} Level: ${selectedUnit.stats.level}")
                ResourceBar("Health", selectedUnit.hp, Color.Red)
                Spacer(Modifier.size(2.dp))
                ResourceBar("Mana", selectedUnit.mp, Color.Blue)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UnitInfoPreview(){
    UnitInfo(Wizard(), BottomSheetNavigation())
}