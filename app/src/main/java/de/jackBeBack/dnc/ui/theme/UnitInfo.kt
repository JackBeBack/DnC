package de.jackBeBack.dnc.ui.theme

import Player
import UnitEntity
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale

@Composable
fun UnitInfo(selectedUnit: UnitEntity?, onMove: () -> Unit = {}) {
    if (selectedUnit == null) return
    val context = LocalContext.current
    val img = BitmapFactory.decodeResource(context.resources, selectedUnit.resId).scale(400, 400)
        .asImageBitmap()
    when(selectedUnit){
        is Player -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ){
                Row {
                    Column {
                        Image(img, contentDescription = "Unit Image")
                        Row {
                            Column {
                                Text("Strength:")
                                Text("Dexterity:")
                                Text("Constitution:")
                                Text("Intelligent:")
                                Text("Wisdom:")
                                Text("Charisma:")
                            }
                            Spacer(Modifier.size(8.dp))
                            Column {
                                Text(selectedUnit.stats.strength.toString())
                                Text(selectedUnit.stats.dexterity.toString())
                                Text(selectedUnit.stats.constitution.toString())
                                Text(selectedUnit.stats.intelligent.toString())
                                Text(selectedUnit.stats.wisdom.toString())
                                Text(selectedUnit.stats.charisma.toString())
                            }
                        }
                    }
                    Column {
                        Text("${selectedUnit.name} Level: ${selectedUnit.stats.level}")
                        ResourceBar(selectedUnit.hp, Color.Red)
                        Spacer(Modifier.size(2.dp))
                        ResourceBar(selectedUnit.mp, Color.Blue)
                    }

                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Card(modifier = Modifier
                        .size(100.dp)
                        .background(Color.DarkGray), onClick = {
                        onMove()
                    }) {
                        Box(Modifier.fillMaxSize()){
                            Text("Move", modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Card(modifier = Modifier.size(100.dp)) {
                        Box(Modifier.fillMaxSize()){
                            Text("Attack", modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }

            }
        }
    }
}