package de.jackBeBack.dnc.data

import Transform
import UnitEntity
import use
import kotlin.math.PI
import kotlin.math.atan2

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
    areaOfEffect = { selfPos, target ->  circularAreaOfEffect(1, selfPos, target) },
    type = DamageType.FIRE,
    effectOnSource = { s ->
        val manaCost = 2
        return@Attack if (s.mp.hasEnough(manaCost)) {
            s.update(mp = s.mp.use(manaCost))
        } else {
            null
        }
    },
    effectOnTarget = { p ->
        p.update(hp = p.hp.use())
    })

val fireBall2 = Attack("Fire Ball 2", null, null,
    areaOfEffect = { selfPos, target ->  circularAreaOfEffect(2, selfPos, target) },
    type = DamageType.FIRE,
    effectOnSource = { s ->
        val manaCost = 4
        return@Attack if (s.mp.hasEnough(manaCost)) {
            s.update(mp = s.mp.use(manaCost))
        } else {
            null
        }
    },
    effectOnTarget = { p ->
        val damage = 2
        p.update(hp = p.hp.use(damage))
    })

val fireBall3 = Attack("Fire Ball 3", null, null,
    areaOfEffect = { selfPos, target ->  circularAreaOfEffect(3, selfPos, target) },
    type = DamageType.FIRE,
    effectOnSource = { s ->
        val manaCost = 6
        return@Attack if (s.mp.hasEnough(manaCost)) {
            s.update(mp = s.mp.use(manaCost))
        } else {
            null
        }
    },
    effectOnTarget = { p ->
        val damage = 3
        p.update(hp = p.hp.use(damage))
    })

val fireBall4 = Attack("Fire Ball 4", null, null,
    areaOfEffect = { selfPos, target ->  circularAreaOfEffect(4, selfPos, target) },
    type = DamageType.FIRE,
    effectOnSource = { s ->
        val manaCost = 8
        return@Attack if (s.mp.hasEnough(manaCost)) {
            s.update(mp = s.mp.use(manaCost))
        } else {
            null
        }
    },
    effectOnTarget = { p ->
        val damage = 4
        p.update(hp = p.hp.use(damage))
    })

val clubHit = Attack("ClubHit", null, null,
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
    PHYSICAL
}