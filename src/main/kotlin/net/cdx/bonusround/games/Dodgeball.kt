package net.cdx.bonusround.games

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.cdx.bonusround.*
import net.cdx.bonusround.games.api.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.function.Consumer

private lateinit var dodgeballArenaProvider: ArenaProvider
private val inGame = HashMap<Player, Game>()

private val dodgeball1v1 = Consumer<Game> { game ->
    suspendingAsync {
        game.onPlayerLost { player ->
            inGame.remove(player)
            game.release(cancelJob = true)
        }

        game.broadcast("Waiting for available arena...")

        val arenaSearch = CompletableDeferred<Arena?>()

        Main.instance.launch {
            withContext(Dispatchers.IO) {
                arenaSearch.complete(ArenaService.awaitArena(dodgeballArenaProvider, seconds().toMillis(1), minutes().toMillis(2)))
            }
        }

        val arena = arenaSearch.await()

        if (arena == null) {
            game.broadcast("Failed to find available arena in reasonable time")
            game.release(cancelJob = true)
            return@suspendingAsync
        }

        game.broadcast("Arena found: ${arena.id}")

        sync {
            val red = game.players[0]
            val blue = game.players[1]

            red.teleport(Location(arena.origin.world, arena.origin.x, arena.origin.y, arena.origin.z + 16))
            blue.teleport(Location(arena.origin.world, arena.origin.x, arena.origin.y, arena.origin.z - 16))

            inGame[red] = game
            inGame[blue] = game

            game.broadcast("Automatically releasing in 10 seconds for testing")

            delay(seconds().toMillis(10)) {
                game.broadcast("game over, releasing")

                game.players.forEach { inGame.remove(it) }
                game.release(cancelJob = true)
            }
        }
    }
}

class Dodgeball : Registrable {
    override fun register() {

        // Arenas

        dodgeballArenaProvider = ArenaService.createProvider()
            .addArena("test", Location(Bukkit.getWorld("arenas"), 0.5, 65.0, 0.5))

        // 1v1s

        QueueManager.registerQueue(
            QueueBuilder(
                "dodgeball_1v1",
                "Dodgeball 1v1"
            )
                .setMeta(QueueMeta(
                    2,
                    2
                ))
                .setGameInit(dodgeball1v1)
                .build()
        )

    }
}