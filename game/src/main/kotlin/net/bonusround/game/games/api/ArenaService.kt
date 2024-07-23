package net.bonusround.game.games.api

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.*
import net.bonusround.game.Main

class ArenaService {

    companion object {

        fun createProvider(): ArenaProvider {
            return ArenaProvider()
        }

        suspend fun awaitArena(provider: ArenaProvider, searchInterval: Long, timeout: Long): Arena? {
            val arenaDeferred = CompletableDeferred<Arena?>()

            val job = Main.instance.launch {
                withContext(Dispatchers.IO) {
                    while (arenaDeferred.isActive) {
                        provider.arenas.forEach { (_, arena) ->
                            if (!arena.isReservable()) return@forEach
                            arenaDeferred.complete(arena)
                            return@withContext
                        }
                        delay(searchInterval)
                    }
                }
            }

            return try {
                withTimeout(timeout) {
                    arenaDeferred.await()
                }
            } catch (error: TimeoutCancellationException) {
                job.cancel()
                null
            }
        }

    }

}