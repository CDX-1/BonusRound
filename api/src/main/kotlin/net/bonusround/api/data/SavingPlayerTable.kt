package net.bonusround.api.data

import org.bukkit.entity.Player

interface SavingPlayerTable : PlayerTable {
    fun save(player: Player)
    fun saveAll()
}