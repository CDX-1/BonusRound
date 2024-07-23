package net.cdx.bonusround.games.api

import net.cdx.bonusround.EventListener
import net.cdx.bonusround.Main
import net.cdx.bonusround.config.lang
import net.cdx.bonusround.utils.async
import net.cdx.bonusround.utils.sync
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.function.Consumer
import java.util.function.Function

class Queue(
    val id: String,
    val formattedName: String,
    val meta: QueueMeta,
    private val startGame: Consumer<Game>,
    private var matchmaker: Function<ArrayList<Player>, ArrayList<Game>>? = null,
) {

    private val players = ArrayList<Player>()

    val size: Int
        get() = players.size

    init {
        if (matchmaker == null) {
            matchmaker = Function { players ->
                val games = ArrayList<Game>()
                var lastPlayer: Player? = null
                players.forEach { player ->
                    if (lastPlayer == null) {
                        lastPlayer = player
                    } else {
                        games.add(Game(arrayListOf(lastPlayer!!, player)))
                    }
                }
                return@Function games
            }
        }

        EventListener(PlayerQuitEvent::class.java) { event ->
            if (!players.contains(event.player)) return@EventListener
            this removePlayer event.player
        }
    }

    infix fun addPlayer(player: Player): Boolean {
        if (QueueManager isInQueue player) return false
        if (QueueManager isInGame player) return false
        players.add(player)
        QueueManager.setQueue(player, this)
        tick()
        return true
    }

    infix fun removePlayer(player: Player): Boolean {
        if (!players.contains(player)) return false
        players.remove(player)
        if (QueueManager getQueueOf player != this) return false
        QueueManager.setQueue(player, null)
        tick()
        return true
    }

    private fun tick() {
        if (players.size < meta.minPlayers) return
        if (matchmaker == null) {
            Main.logger.severe("Queue '$id' cannot make a match due to missing matchmaker")
            return
        }
        val games = matchmaker!!.apply(players)
        games.forEach { game ->
            game.players.forEach { player ->
                removePlayer(player)
                QueueManager.setGame(player, game)
            }
            async {
                game.job = sync {
                    game.broadcast(lang().games.general.matchFoundTitle)
                    startGame.accept(game)
                }
            }
        }
    }

}