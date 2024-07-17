package net.cdx.bonusround.games.api

import kotlinx.coroutines.Job
import net.cdx.bonusround.EventListener
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.function.Consumer

class Game(val players: ArrayList<Player>) {

    lateinit var job: Job
    private val onPlayerLostHandlers = ArrayList<Consumer<Player>>()
    private val onEventHandlers = ArrayList<Consumer<HashMap<String, Any>>>()

    fun onPlayerLost(handler: Consumer<Player>) {
        onPlayerLostHandlers.add(handler)
    }

    fun onEvent(handler: Consumer<HashMap<String, Any>>) {
        onEventHandlers.add(handler)
    }

    fun callEvent(parameters: HashMap<String, Any>) {
        onEventHandlers.forEach { handler ->
            handler.accept(parameters)
        }
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

    fun broadcast(message: String) {
        players.forEach { it.sendMessage(message) }
    }

    fun broadcast(message: Component) {
        players.forEach { it.sendMessage(message) }
    }

    fun release(player: Player? = null, cancelJob: Boolean = false, returnToLobby: Boolean = true) {
        val lobby = Location(Bukkit.getWorld("world"), 0.5, 65.0, 0.5)
        if (player != null) {
            QueueManager releasePlayer player
            if (returnToLobby) player.teleport(lobby)
        } else {
            players.forEach { pl ->
                QueueManager releasePlayer pl
               if (returnToLobby) pl.teleport(lobby)
            }
        }
        if (cancelJob) {
            job.cancel()
        }
    }

}