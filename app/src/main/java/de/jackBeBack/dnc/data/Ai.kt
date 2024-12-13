package de.jackBeBack.dnc.data

import Transform
import UnitEntity
import de.jackBeBack.dnc.viewmodel.MapStateViewModel

val basicAI = { state: MapStateViewModel, self: UnitEntity, player: UnitEntity? ->
    val distanceToPlayer = self.position.distanceTo(player?.position)


    if (distanceToPlayer == 1){
        //attack the player
        state.attack(clubHit.copy(source = self, target = player?.position))
        self.position

    }else{
        //move towards the player
        val valid = state.generatePointsAtDistance(self.position, self.speed){ x, y ->
            Transform(x, y) != player?.position
        }

        val sorted = valid.sortedBy { it.distanceTo(player?.position) }
        sorted.first()
    }
}