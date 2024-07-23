package net.bonusround.game.games.api

import net.bonusround.game.EventListener
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent

object QueueManager {

    private val inQueue = HashMap<Player, Queue>()
    private val inGame = HashMap<Player, Game>()
    private val queues = HashMap<String, Queue>()

    init {

        EventListener(PlayerQuitEvent::class.java) { event ->
            if (inGame.contains(event.player)) {
                inGame.remove(event.player)
            }
            if (inQueue.contains(event.player)) {
                inQueue.remove(event.player)
            }
        }

    }

    fun setQueue(player: Player, queue: Queue?) {
        if (queue == null) {
            inQueue.remove(player)
        } else {
            inQueue[player] = queue
        }
    }

    infix fun isInQueue(player: Player): Boolean {
        return inQueue.contains(player)
    }

    infix fun isInGame(player: Player): Boolean {
        return inGame.contains(player)
    }

    infix fun getGame(player: Player): Game? {
        return inGame[player]
    }

    fun setGame(player: Player, game: Game): Boolean {
        if (inGame[player] != null) return false
        inGame[player] = game
        return true
    }

    infix fun releasePlayer(player: Player): Boolean {
        if (!inGame.contains(player)) return false
        inGame.remove(player)
        return true
    }

    infix fun registerQueue(queue: Queue) {
        queues[queue.id] = queue
    }

    infix fun getQueue(id: String): Queue? {
        return queues[id]
    }

    infix fun getQueueOf(player: Player): Queue? {
        return inQueue[player]
    }

}