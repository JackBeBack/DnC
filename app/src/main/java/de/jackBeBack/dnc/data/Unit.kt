import androidx.compose.runtime.Stable
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
){
    open fun updatePosition(new: Transform): UnitEntity {
        return UnitEntity(id, name, resId, stats, hp, mp, new)
    }
}

open class Player(
    override val id: UUID,
    override val name: String,
    override val resId: Int,
    override val stats: StatsEntity,
    override val hp: Resource,
    override val mp: Resource,
    override val position: Transform,
    open val experience: Int
) : UnitEntity(id, name, resId, stats, hp, mp, position)

open class Enemy(
    override val id: UUID,
    override val name: String,
    override val resId: Int,
    override val stats: StatsEntity,
    override val hp: Resource,
    override val mp: Resource,
    override val position: Transform
) : UnitEntity(id, name, resId, stats, hp, mp, position)

class Wizard() : Player(
    id = UUID.randomUUID(),
    name = "Wizard",
    resId = R.drawable.wizard,
    stats = StatsEntity(),
    hp = Resource(),
    mp = Resource(),
    position = Transform(5, 10),
    experience = 0
)