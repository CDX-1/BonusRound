package net.bonusround.api.data

import org.bukkit.entity.Player

interface PlayerTable {
    fun onJoin(player: Player)
    fun onQuit(player: Player)
}