package net.bonusround.api.gui

import org.bukkit.entity.Player

abstract class Gui(val id: String) {
    abstract fun open(vararg players: Player)
}