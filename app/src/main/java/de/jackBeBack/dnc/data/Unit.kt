import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.data.Attack
import de.jackBeBack.dnc.viewmodel.MapStateViewModel
import java.lang.Integer.max
import java.util.UUID
import kotlin.math.roundToInt

@Stable
data class StatsEntity(
    val level: Int = 1,
    val strength: Int = 0,
    val dexterity: Int = 0,
    val constitution: Int = 0,
    val intelligent: Int = 0,
    val wisdom: Int = 0,
    val charisma: Int = 0
)

@Stable
data class Resource(
    val max: Int = 10,
    val current: Int = max,
    val buff: Int = 0
) {
    override fun toString(): String {
        return "[$current/$max + $buff]"
    }

    fun percentage(): Float {
        return (this.current.toFloat() / this.max)
    }

    fun hasEnough(amount: Int): Boolean{
        return current >= amount
    }
}

fun Resource.use(amount: Int = 1): Resource {
    return this.copy(current = max(current - amount, 0))
}

@Stable
data class Transform(
    val x: Int = 0,
    val y: Int = 0
){
    fun distanceTo(other: Transform?): Int {
        if (other == null) return Int.MAX_VALUE
        val dx = (x - other.x).toDouble()
        val dy = (y - other.y).toDouble()
        return kotlin.math.sqrt(dx * dx + dy * dy).toInt()
    }
}

open class UnitEntity(
    open val id: UUID = UUID.randomUUID(),
    open val name: String = "Unknown",
    open val type: EntityType,
    open val resId: Int = 0,
    open val stats: StatsEntity = StatsEntity(),
    open val hp: Resource = Resource(),
    open val mp: Resource = Resource(),
    open val position: Transform = Transform(),
    open val speed: Int = 1,
    open val experience: Int,
    open val action: Resource,
    open val bonusAction: Resource,
    open val attacks: List<Attack> = listOf(),
    open val ai: (state: MapStateViewModel, self: UnitEntity, player: UnitEntity?) -> Transform? = {_, _, _ -> null}
) {
    fun update(
        id: UUID = this.id,
        name: String = this.name,
        type: EntityType = this.type,
        resId: Int = this.resId,
        stats: StatsEntity = this.stats,
        hp: Resource = this.hp,
        mp: Resource = this.mp,
        position: Transform = this.position,
        speed: Int = this.speed,
        experience: Int = this.experience,
        action: Resource = this.action,
        bonusAction: Resource = this.bonusAction,
        attacks: List<Attack> = this.attacks,
        ai: (state: MapStateViewModel, self: UnitEntity, player: UnitEntity?) -> Transform? = this.ai
    ): UnitEntity {
        return UnitEntity(
            id,
            name,
            type,
            resId,
            stats,
            hp,
            mp,
            position,
            speed,
            experience,
            action,
            bonusAction,
            attacks,
            ai
        )
    }

    fun isDead(): Boolean {
        return this.hp.current <= 0
    }

    fun isPlayer(): Boolean {
        return this.type == EntityType.PLAYER
    }

    fun hasAction(): Boolean {
        return this.action.current > 0
    }

    fun resetActions(): UnitEntity {
        return this.update(
            action = this.action.copy(current = this.action.max),
            bonusAction = this.bonusAction.copy(current = this.bonusAction.max)
        )
    }
}

open class Player(
    override val id: UUID = UUID.randomUUID(),
    override val name: String = "Player",
    override val type: EntityType = EntityType.PLAYER,
    override val resId: Int = 0,
    override val stats: StatsEntity = StatsEntity(),
    override val hp: Resource = Resource(),
    override val mp: Resource = Resource(),
    override val position: Transform = Transform(),
    override val speed: Int = 1,
    override val experience: Int = 0,
    override val action: Resource = Resource(2, 2),
    override val bonusAction: Resource = Resource(1, 1),
    override val attacks: List<Attack> = listOf()
) : UnitEntity(
    id,
    name,
    type,
    resId,
    stats,
    hp,
    mp,
    position,
    speed,
    experience,
    action,
    bonusAction,
    attacks
)

open class Enemy(
    override val id: UUID = UUID.randomUUID(),
    override val name: String = "Enemy",
    override val type: EntityType = EntityType.ENEMY,
    override val resId: Int = 0,
    override val stats: StatsEntity = StatsEntity(),
    override val hp: Resource = Resource(),
    override val mp: Resource = Resource(),
    override val position: Transform = Transform(),
    override val speed: Int = 1
) : UnitEntity(id, name, type, resId, stats, hp, mp, position, speed, 0, Resource(), Resource())


fun UnitEntity.useAction(amount: Int): UnitEntity {
    return this.update(action = this.action.use(amount))
}


enum class EntityType {
    PLAYER,
    ENEMY
}