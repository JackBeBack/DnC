package de.jackBeBack.dnc.data

import kotlin.random.Random

data class RollEntity(val value: Int, val target: Int?, val advantage: Boolean) {
    fun isSuccess(): Boolean {
        return if (target == null) true else {
            value >= target
        }
    }
}

fun rollD20(target: Int? = null): RollEntity {
    return RollEntity(1 + Random.nextInt(20), target, false)
}

fun rollD8(target: Int? = null): RollEntity {
    return RollEntity(1 + Random.nextInt(8), target, false)
}