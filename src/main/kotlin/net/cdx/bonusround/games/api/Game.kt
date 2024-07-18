package net.cdx.bonusround.games.api

import kotlinx.coroutines.Job
import net.cdx.bonusround.EventListener
import net.cdx.bonusround.utils.Formatter
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.function.Consumer

class Game(val players: ArrayList<Player>) {

    lateinit var job: Job
    private val onEventHandlers = ArrayList<Consumer<GameEvent>>()

    fun onEvent(handler: Consumer<GameEvent>) {
        onEventHandlers.add(handler)
    }

    fun callEvent(event: GameEvent) {
        onEventHandlers.forEach { handler ->
            handler.accept(event)
        }
    }

    init {
        EventListener(PlayerQuitEvent::class.java) { event ->
            if (!players.contains(event.player)) return@EventListener
            players.remove(event.player)
            callEvent(GameEvent("disconnect", hashMapOf(Pair("player", event.player))))
        }
    }

    fun broadcast(message: String) {
        players.forEach { it.sendMessage(message) }
    }

    fun broadcast(message: Component) {
        players.forEach { it.sendMessage(message) }
    }

    fun broadcast(title: Component, subtitle: Component) {
        broadcast(Title.title(title, subtitle))
    }

    fun broadcast(title: Title) {
        players.forEach { it.showTitle(title) }
    }

    fun broadcast(title: net.cdx.bonusround.config.serializers.Title) {
        broadcast(Formatter.title(Formatter()
            .usePrefix(false),
            title))
    }

    fun release(player: Player? = null, cancelJob: Boolean = false, returnToLobby: Boolean = true) {
        callEvent(GameEvent("release"))
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