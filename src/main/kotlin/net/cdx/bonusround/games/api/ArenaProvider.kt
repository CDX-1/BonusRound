package net.cdx.bonusround.games.api

import org.bukkit.Location

class ArenaProvider {

    val arenas = HashMap<String, Arena>()

    fun addArena(id: String, origin: Location): ArenaProvider {
        arenas[id] = Arena(id, origin)
        return this
    }

}