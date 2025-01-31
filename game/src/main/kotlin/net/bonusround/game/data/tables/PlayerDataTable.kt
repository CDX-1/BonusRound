package net.bonusround.game.data.tables

import kotlinx.coroutines.delay
import net.bonusround.api.data.DataContainerService
import net.bonusround.api.data.SavingPlayerTable
import net.bonusround.api.data.createTableName
import net.bonusround.api.utils.asyncTransaction
import net.bonusround.api.utils.launch
import net.bonusround.api.utils.minutes
import net.bonusround.game.data.containers.PlayerDataContainer
import net.bonusround.game.data.entities.PlayerDataEntity
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

object PlayerDataTable : IntIdTable(createTableName("player_data")), SavingPlayerTable {
    override fun save(player: Player) {
        launch {
            DataContainerService.getContainers(
                PlayerDataContainer::class,
                Int::class,
                UUID::class
            )!![player.uniqueId]?.let { data ->
                asyncTransaction {
                    val entity = PlayerDataEntity.find { uuid eq player.uniqueId }.firstOrNull()
                    entity?.let {
                        it.bits = data.bits
                        it.firstSeen = data.firstSeen
                        it.lastSeen = data.lastSeen
                        it.firstQuit = data.firstQuit
                        it.lastQuit = data.lastQuit
                    } ?: run {
                        PlayerDataEntity.new {
                            uuid = player.uniqueId
                            bits = data.bits
                            firstSeen = data.firstSeen
                            lastSeen = data.lastSeen
                            firstQuit = data.firstQuit
                            lastQuit = data.lastQuit
                        }
                    }
                }
            }
        }
    }

    override fun saveAll() {
        Bukkit.getServer().onlinePlayers.forEach { player ->
            save(player)
        }
    }

    override fun onJoin(player: Player) {
        launch {
            val playerData = asyncTransaction {
                val entity = PlayerDataEntity.find(uuid eq player.uniqueId).firstOrNull()
                return@asyncTransaction PlayerDataContainer(
                    player.uniqueId,
                    bits = entity?.bits ?: 0,
                    firstSeen = entity?.firstSeen ?: System.currentTimeMillis(),
                    lastSeen = entity?.lastSeen ?: System.currentTimeMillis(),
                    firstQuit = entity?.firstQuit ?: 0,
                    lastQuit = entity?.lastQuit ?: 0,
                )
            }

            DataContainerService.getContainers(PlayerDataContainer::class, Int::class, UUID::class)!![player.uniqueId] =
                playerData
        }
    }

    override fun onQuit(player: Player) {
        save(player)
        DataContainerService.getContainers(
            PlayerDataContainer::class,
            Int::class,
            UUID::class
        )!!.remove(player.uniqueId)
    }

    init {
        launch {
            while (true) {
                saveAll()
                delay(minutes().toMillis(2))
            }
        }
    }

    val uuid = uuid("uuid")
    val bits = integer("bits")
    val firstSeen = long("first_seen")
    val lastSeen = long("last_seen")
    val firstQuit = long("first_quit")
    val lastQuit = long("last_quit")
}