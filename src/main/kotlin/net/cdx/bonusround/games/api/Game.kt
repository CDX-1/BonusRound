package net.cdx.bonusround.games.api

import net.cdx.bonusround.EventListener
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.function.Consumer

class Game(val players: ArrayList<Player>) {

    private val onPlayerLostHandlers = ArrayList<Consumer<Player>>()

    fun onPlayerLost(handler: Consumer<Player>) {
        onPlayerLostHandlers.add(handler)
    }

    init {
        EventListener(PlayerQuitEvent::class.java) { event ->
            if (!players.contains(event.player)) return@EventListener
            players.remove(event.player)
            onPlayerLostHandlers.forEach { handler ->
                handler.accept(event.player)
            }
        }
    }

    fun release(player: Player? = null) {
        if (player != null) {
            QueueManager releasePlayer player
        } else {
            players.forEach { pl ->
                QueueManager releasePlayer pl
            }
        }
    }

}