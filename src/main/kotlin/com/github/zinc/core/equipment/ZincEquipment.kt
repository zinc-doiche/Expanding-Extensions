package com.github.zinc.core.equipment

import com.github.zinc.core.player.PlayerData
import com.github.zinc.util.extension.getPersistent
import com.github.zinc.util.extension.setPersistent
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ZincEquipment(
    var equipment: ItemStack,
    val constraint: Status
) {
    fun canUse(playerData: PlayerData): Boolean {
        return true
    }

    companion object fun of(itemStack: ItemStack) = ZincEquipment(
        itemStack,
        Status()
    )
}

data class Status(
    var strength: Int = 0,
    var swiftness: Int = 0,
    var balance: Int = 0,
    var concentration: Int = 0
) {
    constructor() : this(0,0,0,0)

    constructor(
        strength: Double = .0,
        swiftness: Double = .0,
        balance: Double = .0,
        concentration: Double = .0
    ) : this(strength.toInt(), swiftness.toInt(), balance.toInt(), concentration.toInt())

    operator fun times(times: Double)
        = Status(strength * times, swiftness * times, balance * times, concentration * times)

    fun add(status: Status) {
        strength += status.strength
        swiftness += status.swiftness
        balance += status.balance
        concentration += status.concentration
    }

    fun setStatus(itemStack: ItemStack) {
        itemStack.type.name.split('_').run {

            //enchants
            itemStack.itemMeta.enchants.entries.forEach{
                enchantMap[it.key]?.let { constraint ->
                    when(constraint) {
                        is Status -> add(constraint)
                        is Pair<*, *> -> {
                            add(constraint.first as Status)
                            itemStack.addLevelConstraint(constraint.second as Int)
                        }
                        is Int -> itemStack.addLevelConstraint(constraint)
                    }
                }
            }

            when(size) {
                //NON-TYPED tools
                1 -> toolMap[get(0)]?.let(::add) ?: return

                //TYPED tools
                2 -> {
                    if(get(0) == "FISHING") {
                        concentration = 30
                        balance = 30
                        return
                    }

                    val amplifier: Double = materialMap[get(0)] ?: return
                    val constraint = typedToolMap[get(1)]?.times(amplifier) ?: return

                    add(constraint)
                }
            }
        }
    }
}

private const val WOOD = "WOODEN"
private const val STONE = "STONE"
private const val IRON = "IRON"
private const val GOLD = "GOLDEN"
private const val DIAMOND = "DIAMOND"
private const val NETHERITE = "NETHERITE"

private val materialMap: Map<String, Double> = mapOf(WOOD to .0, STONE to .2, IRON to 1.0, GOLD to .5, DIAMOND to 1.5, NETHERITE to 2.5)

private const val BOW = "BOW"
private const val CROSSBOW = "CROSSBOW"
private const val TRIDENT = "TRIDENT"
private const val SHIELD = "SHIELD"
private const val FISHING_ROD = "FISHING_ROD"

private val toolMap: Map<String, Status> = mapOf(
    BOW to Status(concentration = 150),
    CROSSBOW to Status(strength = 70, concentration = 70),
    TRIDENT to Status(concentration = 30, strength = 30, balance = 30),
    SHIELD to Status(balance = 100),
    FISHING_ROD to Status(concentration = 30, balance = 30)
)

private const val PICKAXE ="PICKAXE"
private const val AXE = "AXE"
private const val SWORD = "SWORD"

private val typedToolMap: Map<String, Status> = mapOf(
    PICKAXE to Status(balance = 60, strength = 20),
    AXE to Status(strength = 60, balance = 20),
    SWORD to Status(swiftness = 60, strength = 20)
)

private const val HELMET = "HELMET"
private const val CHESTPLATE = "CHESTPLATE"
private const val LEGGINGS = "LEGGINGS"
private const val BOOTS = "BOOTS"

private val equipmentMap: Map<String, Status> = mapOf(
    HELMET to Status(balance = 60, concentration = 20),
    CHESTPLATE to Status(balance = 80),
    LEGGINGS to Status(balance = 40, strength = 40),
    BOOTS to Status(balance = 40, swiftness = 40)
)

val STATUS_KEY: NamespacedKey = NamespacedKey.minecraft("status_added")
val STRENGTH: NamespacedKey = NamespacedKey.minecraft("str")
val SWIFTNESS: NamespacedKey = NamespacedKey.minecraft("swt")
val BALANCE: NamespacedKey = NamespacedKey.minecraft("bal")
val CONCENTRATION: NamespacedKey = NamespacedKey.minecraft("con")
private val LEVEL_CONSTRAINT_KEY: NamespacedKey = NamespacedKey.minecraft("level_constraint")

internal fun ItemStack.isEquipment(): Boolean {
    return this.type.name.let {
        it.contains(HELMET)     || it.contains(CHESTPLATE) ||
        it.contains(LEGGINGS)   || it.contains(BOOTS)
    }
}

internal fun ItemStack.isTool(): Boolean {
    return this.type.name.let {
        it.contains(PICKAXE)    || it.contains(AXE)         || it.contains(SWORD)       ||
        it.contains(BOW)        || it.contains(CROSSBOW)    || it.contains(TRIDENT)     ||
        it.contains(SHIELD)     || it.contains(FISHING_ROD)
    } || this.isEquipment()
}

private fun ItemStack.setLevelConstraint(level: Int) {
    val constraint = this.getPersistent(LEVEL_CONSTRAINT_KEY, PersistentDataType.INTEGER) ?: return
    if(constraint < level) this.setPersistent(LEVEL_CONSTRAINT_KEY, level, PersistentDataType.INTEGER)
}

private fun ItemStack.addLevelConstraint(level: Int) {
    val constraint = this.getPersistent(LEVEL_CONSTRAINT_KEY)?.toInt() ?: 0
    this.setPersistent(LEVEL_CONSTRAINT_KEY, level + constraint, PersistentDataType.INTEGER)
}

private val enchantMap: Map<Enchantment, Any> = mapOf(
    Enchantment.PROTECTION_ENVIRONMENTAL to Status(balance = 20),
    Enchantment.PROTECTION_FIRE to Status(balance = 10),
    Enchantment.PROTECTION_EXPLOSIONS to Status(balance = 10),
    Enchantment.PROTECTION_PROJECTILE to Status(balance = 10),
    Enchantment.THORNS to Status(strength = 15),

    //Helmet
    Enchantment.WATER_WORKER to Status(concentration = 10),

    //leggings
    Enchantment.SWIFT_SNEAK to Status(swiftness = 10),

    //Boots
    Enchantment.PROTECTION_FALL to Status(swiftness = 10),
    Enchantment.DEPTH_STRIDER to Status(swiftness = 10),
    Enchantment.FROST_WALKER to Status(swiftness = 10),
    Enchantment.SOUL_SPEED to Status(swiftness = 10),

    //damaging
    Enchantment.DAMAGE_ALL to Status(strength = 20),
    Enchantment.DAMAGE_UNDEAD to Status(strength = 20),
    Enchantment.DAMAGE_ARTHROPODS to Status(strength = 20),
    Enchantment.SWEEPING_EDGE to Status(strength = 10),

    //effects
    Enchantment.KNOCKBACK to Status(strength = 10),
    Enchantment.FIRE_ASPECT to Status(concentration = 20),
    Enchantment.LOOT_BONUS_MOBS to 50,

    //tools
    Enchantment.DIG_SPEED to Status(balance = 10, strength = 10),
    Enchantment.SILK_TOUCH to Status(concentration = 30),
    Enchantment.DURABILITY to 50,
    Enchantment.LOOT_BONUS_BLOCKS to 60,

    //bow
    Enchantment.ARROW_DAMAGE to Status(strength = 10, concentration = 30),
    Enchantment.ARROW_KNOCKBACK to Status(strength = 10),
    Enchantment.ARROW_FIRE to Status(concentration = 20),
    Enchantment.ARROW_INFINITE to 180,

    //fishing rod
    Enchantment.LUCK to 40,
    Enchantment.LURE to 40,

    //trident
    Enchantment.LOYALTY to Status(concentration = 30),
    Enchantment.IMPALING to Status(strength = 20),
    Enchantment.RIPTIDE to Pair(Status(swiftness = 30), 50),
    Enchantment.CHANNELING to 120,

    //crossbow
    Enchantment.MULTISHOT to Status(concentration = 30, strength = 30),
    Enchantment.QUICK_CHARGE to Status(concentration = 30, balance = 20),
    Enchantment.PIERCING to Status(strength = 20),

    //mending
    Enchantment.MENDING to 250
)
