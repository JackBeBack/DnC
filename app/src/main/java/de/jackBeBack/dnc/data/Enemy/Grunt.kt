package de.jackBeBack.dnc.data.Enemy

import EntityType
import Resource
import StatsEntity
import Transform
import UnitEntity
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.data.basicAI
import de.jackBeBack.dnc.viewmodel.MapStateViewModel
import java.util.UUID

class Grunt(
    override val id: UUID = UUID.randomUUID(),
    override val name: String = "Grunt",
    override val resId: Int = R.drawable.grunt,
    override val type: EntityType = EntityType.ENEMY,
    override val stats: StatsEntity = StatsEntity(intelligent = 4),
    override val successRoll: Boolean? = null,
    override val amorClass: Int = 12,
    override val hp: Resource = Resource(16),
    override val mp: Resource = Resource(0),
    override val position: Transform = Transform(5, 2),
    override val speed: Int = 2,
    override val ai: (state: MapStateViewModel, self: UnitEntity, player: UnitEntity?) -> Transform? = basicAI
) : UnitEntity(id, name, type, resId, stats, successRoll, amorClass, hp, mp, position, speed, 0, Resource(), Resource())