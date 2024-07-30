package de.jackBeBack.dnc.Map

import MapCanvas
import Player
import UnitEntity
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import de.jackBeBack.dnc.ui.theme.BottomSheet
import de.jackBeBack.dnc.viewmodel.MapState

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
                Image(img, contentDescription = "Unit Image")
                    Column {
                        Text("${selectedUnit.name} Level: ${selectedUnit.stats.level}")
                        Text(" HP: ${selectedUnit.hp}", color = Color.Red)
                        Text(" MP: ${selectedUnit.mp}", color = Color.Green)


                        Text(selectedUnit.stats.toString())
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

@Composable
fun GameLayout() {
    val context = LocalContext.current
    val mapState = remember { MapState() }
    LaunchedEffect(Unit) {
        mapState.loadMap1(context)
    }
    val tiles by mapState.tiles.collectAsState()
    val size by mapState.size.collectAsState()
    val units by mapState.units.collectAsState()

    var selectedUnit by remember { mutableStateOf<UnitEntity?>(null) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var isMoving by remember { mutableStateOf(false) }
    var lastTap by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(lastTap, units) {
        val unitOnTap = units.firstOrNull {
            it.position.x == lastTap?.first && it.position.y == lastTap?.second
        }
        if (unitOnTap != null) {
            selectedUnit = unitOnTap
        }
        if (!isMoving) {
            showBottomSheet = unitOnTap != null
        }else{
            isMoving = false
        }
    }

    MapCanvas(tiles, units, size.width, size.height) { x, y->
        val tile = tiles[y + size.height * x]
        lastTap = x to y
        println(tile)
        if (tile.tint != null) {
            mapState.moveUnit(selectedUnit!!, x to y)
        }
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
                isMoving = true
                lastTap?.let { mapState.showMoves(it, 2, false) }
                showBottomSheet = false
            })
    }
}