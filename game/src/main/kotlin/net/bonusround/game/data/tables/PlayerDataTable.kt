package net.bonusround.game.data.tables

import com.github.shynixn.mccoroutine.bukkit.launch
import net.bonusround.api.data.DataContainerService
import net.bonusround.api.data.createTableName
import net.bonusround.api.utils.EventListener
import net.bonusround.api.utils.asyncTransaction
import net.bonusround.game.Main
import net.bonusround.game.data.containers.PlayerDataContainer
import net.bonusround.game.data.entities.PlayerDataEntity
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.upsert
import java.util.*

object PlayerDataTable : UUIDTable(createTableName("player_data")) {
    fun init() {
        EventListener(PlayerJoinEvent::class.java) { event ->
            Main.instance.launch {
                val playerData = asyncTransaction {
                    val entity = PlayerDataEntity.findById(event.player.uniqueId)
                    return@asyncTransaction PlayerDataContainer(
                        event.player.uniqueId,
                        bits = entity?.bits ?: 0,
                        firstSeen = entity?.firstSeen ?: System.currentTimeMillis(),
                        lastSeen = entity?.lastSeen ?: System.currentTimeMillis(),
                        firstQuit = entity?.firstQuit ?: 0,
                        lastQuit = entity?.lastQuit ?: 0,
                    )
                }

                DataContainerService.getContainers(PlayerDataContainer::class, UUID::class)!![event.player.uniqueId] =
                    playerData
            }
        }

        EventListener(PlayerQuitEvent::class.java) { event ->
            Main.instance.launch {
                DataContainerService.getContainers(
                    PlayerDataContainer::class,
                    UUID::class
                )!![event.player.uniqueId]?.let { data ->
                    asyncTransaction {
                        PlayerDataTable.upsert {
                            it[id] = event.player.uniqueId
                            it[bits] = data.bits
                            it[firstSeen] = data.firstSeen
                            it[lastSeen] = data.lastSeen
                            it[firstQuit] = if (data.firstQuit == 0L) System.currentTimeMillis() else data.firstQuit
                            it[lastQuit] = System.currentTimeMillis()
                        }
                    }
                }
            }
        }
    }

    val bits = integer("bits")
    val firstSeen = long("first_seen")
    val lastSeen = long("last_seen")
    val firstQuit = long("first_quit")
    val lastQuit = long("last_quit")
}