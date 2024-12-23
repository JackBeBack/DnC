package de.jackBeBack.dnc.data

import Transform
import UnitEntity
import de.jackBeBack.dnc.viewmodel.MapStateViewModel

val basicAI = { state: MapStateViewModel, self: UnitEntity, player: UnitEntity? ->
    val occupied = state.units.value.map { it.position }
    val distanceToPlayer = self.position.distanceTo(player?.position)

    if (distanceToPlayer == 1){
        //attack the player
        state.attack(weakFist.copy(source = self, target = player!!.position)){
            self.position
        }
        null
    }else{
        //move towards the player
        val valid = state.generatePointsAtDistance(self.position, self.speed){ x, y ->
            Transform(x, y) != player?.position && Transform(x, y) !in occupied
        }

        val sorted = valid.sortedBy { it.distanceTo(player?.position) }
        sorted.first()
    }
}