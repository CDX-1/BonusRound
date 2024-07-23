package net.bonusround.api.game

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.*
import net.bonusround.api.BonusRoundAPI

class ArenaService {

    companion object {

        fun createProvider(): ArenaProvider {
            return ArenaProvider()
        }

        suspend fun awaitArena(provider: ArenaProvider, searchInterval: Long, timeout: Long): Arena? {
            val arenaDeferred = CompletableDeferred<Arena?>()

            val job = BonusRoundAPI.main.launch {
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