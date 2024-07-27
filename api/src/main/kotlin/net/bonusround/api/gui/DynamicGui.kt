package net.bonusround.api.gui

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import org.bukkit.entity.Player

abstract class DynamicGui(id: String) : Gui(id) {
    protected abstract fun createInventory(player: Player): ChestGui
    override fun open(vararg players: Player) {
        players.forEach { createInventory(it).show(it) }
    }
}