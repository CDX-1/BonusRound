package net.bonusround.api.game

import kotlinx.coroutines.Job
import net.bonusround.api.utils.EventListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashMap

class Game(val players: ArrayList<Player>) {

    lateinit var job: Job
    private val onEventHandlers = ArrayList<Consumer<GameEvent>>()
    private val abilities = HashMap<PlayerAbility, Consumer<Player>>()
    private val abilityCooldowns = HashMap<UUID, HashMap<String, Long>>()

    fun registerPlayerAbility(ability: PlayerAbility, handler: Consumer<Player>) {
        abilities[ability] = handler
    }

    fun callAbility(ability: PlayerAbility, player: Player) {
        val last = abilityCooldowns[player.uniqueId]?.get(ability.id) ?: 0
        if (System.currentTimeMillis() - last < ability.cooldown) return
        abilityCooldowns.getOrPut(player.uniqueId) { HashMap() }[ability.id] = System.currentTimeMillis()
        abilities[ability]?.accept(player)
    }

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

    @Suppress("UNUSED")
    fun broadcast(message: String) {
        players.forEach { it.sendMessage(message) }
    }

    fun broadcast(message: Component) {
        players.forEach { it.sendMessage(message) }
    }

    @Suppress("UNUSED")
    fun broadcast(title: Component, subtitle: Component) {
        broadcast(Title.title(title, subtitle))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun broadcast(title: Title) {
        players.forEach { it.showTitle(title) }
    }

//    fun broadcast(title: Title) {
//        broadcast(
//            Formatter.title(
//                Formatter()
//                    .usePrefix(false),
//                title
//            )
//        )
//    }
    // TODO

    fun release(player: Player? = null, cancelJob: Boolean = false, returnToLobby: Boolean = true) {
        callEvent(GameEvent("release"))
        val lobby = Location(Bukkit.getWorld("world"), 0.5, 65.0, 0.5)
        if (player != null) {
            QueueManager releasePlayer player
            players.remove(player)
            if (returnToLobby) player.teleport(lobby)
        } else {
            val iterator = players.iterator()
            while (iterator.hasNext()) {
                val pl = iterator.next()
                QueueManager releasePlayer pl
                if (returnToLobby) pl.teleport(lobby)
                iterator.remove()
            }
        }
        if (cancelJob) {
            job.cancel()
        }
    }

}