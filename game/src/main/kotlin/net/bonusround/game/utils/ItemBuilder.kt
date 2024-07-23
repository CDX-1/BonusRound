package net.bonusround.game.utils

import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.iface.ReadWriteItemNBT
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemBuilder(material: Material) {

    private val itemStack: ItemStack = ItemStack(material)
    private val itemMeta: ItemMeta = itemStack.itemMeta

    fun displayName(component: Component? = null): ItemBuilder {
        itemMeta.displayName(component)
        return this
    }

    fun count(count: Int): ItemBuilder {
        itemStack.amount = count
        return this
    }

    fun lore(vararg lore: Component): ItemBuilder {
        itemMeta.lore(lore.toMutableList())
        return this
    }

    fun customModelData(customModelData: Int): ItemBuilder {
        itemMeta.setCustomModelData(customModelData)
        return this
    }

    fun enchant(enchantment: Enchantment, level: Int): ItemBuilder {
        itemMeta.addEnchant(enchantment, level, true)
        return this
    }

    fun unbreakable(unbreakable: Boolean, hide: Boolean = false): ItemBuilder {
        itemMeta.isUnbreakable = unbreakable
        if (hide) {
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        }
        return this
    }

    fun droppable(droppable: Boolean): ItemBuilder {
        NBT.modify(itemStack) { nbt: ReadWriteItemNBT ->
            nbt.setBoolean("droppable", droppable)
        }
        return this
    }

    fun flags(vararg flags: ItemFlag): ItemBuilder {
        itemMeta.addItemFlags(*flags)
        return this
    }

    fun item(): ItemStack {
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    fun atSlot(slot: Int, vararg players: Player) {
        val item = item()
        players.forEach { player ->
            player.inventory.setItem(slot, item)
        }
    }

    fun atSlot(slot: EquipmentSlot, vararg players: Player) {
        val item = item()
        players.forEach { player ->
            player.inventory.setItem(slot, item)
        }
    }

    fun atSlotAll(slot: Int) {
        atSlot(slot, *Bukkit.getServer().onlinePlayers.toTypedArray())
    }

    fun atSlotAll(slot: EquipmentSlot) {
        atSlot(slot, *Bukkit.getServer().onlinePlayers.toTypedArray())
    }

    fun give(vararg players: Player) {
        val item = item()
        players.forEach { player ->
            player.inventory.addItem(item)
        }
    }

    fun giveAll() {
        give(*Bukkit.getServer().onlinePlayers.toTypedArray())
    }

}