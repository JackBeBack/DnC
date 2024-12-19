package de.jackBeBack.dnc.data

import Transform
import UnitEntity
import use
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

data class Attack(
    val name: String,
    val source: UnitEntity?,
    val target: Transform?,
    val type: DamageType,
    val areaOfEffect: (selfPos: Transform?, target: Transform?) -> Boolean = { _, _ -> true },
    val effectOnTarget: (target: UnitEntity) -> UnitEntity? = { t -> t },
    val effectOnSource: (source: UnitEntity) -> UnitEntity? = { s -> s }
){
    fun getDirection(): Float {
        if (source == null || target == null) return 0F
        // Calculate the angle between the line defined by the points and the x-axis
        val angle = atan2((source.position.y - target.y).toDouble(), (source.position.x - target.x).toDouble()) * (180 / PI)

        // Shift the range from (-180,180) to (0,360)
        val shiftedAngle = (if (angle < 0) angle + 360 else angle) - 90

        return shiftedAngle.toFloat()
    }
}

val fireBall1 = Attack("Fire Ball 1", null, null,
    areaOfEffect = { selfPos, target ->  circularAreaOfEffect(5, selfPos, target) },
    type = DamageType.FIRE,
    effectOnSource = { s ->
        val manaCost = 3
        return@Attack if (s.mp.hasEnough(manaCost)) {
            s.update(mp = s.mp.use(manaCost))
        } else {
            null
        }
    },
    effectOnTarget = { p ->
        p.update(hp = p.hp.use(1))
    })

val iceShard = Attack("Ice Shard", null, null,
    areaOfEffect = { selfPos, target ->  circularAreaOfEffect(1, selfPos, target) },
    type = DamageType.ICE,
    effectOnSource = { s ->
        val manaCost = 2
        return@Attack if (s.mp.hasEnough(manaCost)) {
            s.update(mp = s.mp.use(manaCost), speed = max(0, s.speed-1))
        } else {
            null
        }
    },
    effectOnTarget = { p ->
        p.update(hp = p.hp.use(2))
    })

val channelMana = Attack("Channel Mana", null, null,
    areaOfEffect = { selfPos, target ->  circularAreaOfEffect(1, selfPos, target) },
    type = DamageType.HEALING,
    effectOnSource = {s -> s.update(mp = s.mp.refill(4))},
    effectOnTarget = { p -> p
    })


val fist= Attack("Fist", null, null,
    areaOfEffect = { selfPos, target ->  circularAreaOfEffect(1, selfPos, target) },
    type = DamageType.PHYSICAL,
    effectOnTarget = { p ->
        val damage = 2
        p.update(hp = p.hp.use(damage))
    }
)

enum class DamageType {
    FIRE,
    WATER,
    ICE,
    GROUND,
    PHYSICAL,
    HEALING
}