import androidx.compose.runtime.Stable
import androidx.room.util.copy
import de.jackBeBack.dnc.R
import java.util.UUID

@Stable
data class StatsEntity(
    val level: Int = 1,
    val strength: Int = 0,
    val dexterity: Int = 0,
    val constitution: Int = 0,
    val intelligent: Int = 0,
    val wisdom: Int = 0,
    val charisma: Int = 0
) {
    override fun toString(): String {
        return """
            Strength: $strength
            Dexterity: $dexterity
            Constitution: $constitution
            Intelligent: $intelligent
            Wisdom: $wisdom
            Charisma: $charisma
        """.trimIndent()
    }
}

@Stable
data class Resource(
    val max: Int = 10,
    val current: Int = max,
    val buff: Int = 0
){
    override fun toString(): String {
        return "[$current/$max + $buff]"
    }
}

@Stable
data class Transform(
    val x: Int = 0,
    val y: Int = 0
)

open class UnitEntity(
    open val id: UUID,
    open val name: String,
    open val resId: Int,
    open val stats: StatsEntity,
    open val hp: Resource,
    open val mp: Resource,
    open val position: Transform
)

abstract class Player(
    override val id: UUID,
    override val name: String,
    override val resId: Int,
    override val stats: StatsEntity,
    override val hp: Resource,
    override val mp: Resource,
    override val position: Transform,
    open val experience: Int
) : UnitEntity(id, name, resId, stats, hp, mp, position){
    abstract fun update(
        id: UUID = this.id,
        name: String = this.name,
        resId: Int = this.resId,
        stats: StatsEntity = this.stats,
        hp: Resource = this.hp,
        mp: Resource = this.mp,
        position: Transform = this.position
    ): Player
}

open class Enemy(
    override val id: UUID,
    override val name: String,
    override val resId: Int,
    override val stats: StatsEntity,
    override val hp: Resource,
    override val mp: Resource,
    override val position: Transform
) : UnitEntity(id, name, resId, stats, hp, mp, position)



class Wizard(
    override val id: UUID = UUID.randomUUID(),
    override val name: String = "Wizard",
    override val resId: Int = R.drawable.wizard,
    override val stats: StatsEntity = StatsEntity(),
    override val hp: Resource = Resource(),
    override val mp: Resource = Resource(),
    override val position: Transform = Transform(5,5)
) : Player(
    id = id,
    name = name,
    resId = resId,
    stats = stats,
    hp = hp,
    mp = mp,
    position = position,
    experience = 0
) {
    override fun update(
        id: UUID,
        name: String,
        resId: Int,
        stats: StatsEntity,
        hp: Resource,
        mp: Resource,
        position: Transform
    ): Wizard{
        return Wizard(id, name, resId, stats, hp, mp, position)
    }
}