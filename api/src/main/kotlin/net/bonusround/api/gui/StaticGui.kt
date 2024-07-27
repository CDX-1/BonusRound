package net.bonusround.api.gui

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import org.bukkit.entity.Player

abstract class StaticGui(id: String) : Gui(id) {
    protected abstract var gui: ChestGui
    override fun open(vararg players: Player) {
        players.forEach { gui.show(it) }
    }
}