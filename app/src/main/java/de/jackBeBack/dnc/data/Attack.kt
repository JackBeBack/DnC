package de.jackBeBack.dnc.data

import Transform
import UnitEntity
import de.jackBeBack.dnc.viewmodel.MapStateViewModel
import kotlinx.coroutines.delay
import use
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.random.Random

data class Attack(
    val name: String,
    val description: String = "",
    val source: UnitEntity? = null,
    val target: Transform? = null,
    val rollTarget: Int = 10,
    val type: DamageType,
    val areaOfEffect: (selfPos: Transform?, target: Transform?) -> Boolean = { _, _ -> true },
    val effectOnTarget: suspend (target: UnitEntity) -> UnitEntity? = { t -> t },
    val effectOnSource: suspend (source: UnitEntity, check: Boolean) -> UnitEntity? = { s, c -> s }
) {
    fun getDirection(): Float {
        if (source == null || target == null) return 0F
        // Calculate the angle between the line defined by the points and the x-axis
        val angle = atan2(
            (source.position.y - target.y).toDouble(),
            (source.position.x - target.x).toDouble()
        ) * (180 / PI)

        // Shift the range from (-180,180) to (0,360)
        val shiftedAngle = (if (angle < 0) angle + 360 else angle) - 90

        return shiftedAngle.toFloat()
    }
}

val fireBall1 = Attack("Fire Ball",
    type = DamageType.FIRE,
    description = "A Fire Attack that deals d10 Damage",
    areaOfEffect = { selfPos, target -> circularAreaOfEffect(5, selfPos, target) },
    effectOnSource = { s, c ->
        val manaCost = 3
        return@Attack if (s.mp.hasEnough(manaCost)) {
            s.update(mp = s.mp.use(manaCost))
        } else {
            null
        }
    },
    effectOnTarget = { p ->
        val damageRoll = 1 + Random.nextInt(10)
        MapStateViewModel.global.roll(RollEntity(damageRoll, null, false))
        delay(1000)
        p.update(hp = p.hp.use(damageRoll))
    })

val iceShards = Attack("Ice Shards",
    areaOfEffect = { selfPos, target -> circularAreaOfEffect(1, selfPos, target) },
    type = DamageType.ICE,
    effectOnSource = { s, c ->
        val manaCost = 2
        return@Attack if (s.mp.hasEnough(manaCost)) {
            s.update(mp = s.mp.use(manaCost))
        } else {
            null
        }
    },
    effectOnTarget = { p ->
        val mapStateViewModel = MapStateViewModel.global
        val targets = p.position.getSurrounding()
        p.update(hp = p.hp.use(1))
    })

val iceShard = Attack("Ice Shard",
    areaOfEffect = { selfPos, target -> circularAreaOfEffect(1, selfPos, target) },
    type = DamageType.ICE,
    effectOnTarget = { p ->
        p.update(hp = p.hp.use(1))
    })

val channelMana = Attack("Channel Mana",
    areaOfEffect = { selfPos, target -> circularAreaOfEffect(1, selfPos, target) },
    type = DamageType.HEALING,
    effectOnSource = { s, c ->
        val manaRoll = rollD8()

        if (!c) {
            MapStateViewModel.global.roll(manaRoll)
            delay(1000)
            MapStateViewModel.global.showInfoText("Channeled ${manaRoll.value} Mana")
        }
        s.update(mp = s.mp.refill(manaRoll.value))
    },
    effectOnTarget = { p ->
        p
    })


val weakFist = Attack("Weak Fist",
    description = "Physical Attack that Deals 3 Damage to the Target but also 1 damage to the Source",
    areaOfEffect = { selfPos, target -> circularAreaOfEffect(1, selfPos, target) },
    type = DamageType.PHYSICAL,
    effectOnTarget = { p ->
        val damage = 3
        p.update(hp = p.hp.use(damage))
    },
    effectOnSource = { s, c ->
        s.update(hp = s.hp.use(1))
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

fun DamageType.isMagic(): Boolean {
    return when (this) {
        DamageType.FIRE, DamageType.WATER, DamageType.ICE, DamageType.GROUND -> true
        else -> false
    }
}