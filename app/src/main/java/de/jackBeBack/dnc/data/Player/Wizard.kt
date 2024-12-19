package de.jackBeBack.dnc.data.Player

import EntityType
import Player
import Resource
import StatsEntity
import Transform
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.data.channelMana
import de.jackBeBack.dnc.data.fist
import de.jackBeBack.dnc.data.fireBall1
import de.jackBeBack.dnc.data.iceShard
import java.util.UUID

class Wizard(
    override val id: UUID = UUID.randomUUID(),
    override val name: String = "Wizard",
    override val resId: Int = R.drawable.wizard,
    override val type: EntityType = EntityType.PLAYER,
    override val stats: StatsEntity = wizardStats,
    override val hp: Resource = Resource(),
    override val mp: Resource = Resource(),
    override val position: Transform = Transform(5, 5),
    override val speed: Int = 2,
    override val experience: Int = 0,
    override val action: Resource = Resource(4),
    override val bonusAction: Resource = Resource(1, 1)
) : Player(id, name, type, resId, stats, hp, mp, position, speed, experience, action, bonusAction, listOf(
    fireBall1,
    iceShard,
    fist,
    channelMana
))

val wizardStats = StatsEntity(
    level = 1,
    strength = 8,
    dexterity = 13,
    constitution = 14,
    intelligent = 15,
    wisdom = 12,
    charisma = 10
)