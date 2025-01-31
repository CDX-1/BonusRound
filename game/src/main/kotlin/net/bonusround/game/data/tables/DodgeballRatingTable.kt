package net.bonusround.game.data.tables

import kotlinx.coroutines.delay
import net.bonusround.api.data.DataContainerService
import net.bonusround.api.data.SavingPlayerTable
import net.bonusround.api.data.createTableName
import net.bonusround.api.utils.asyncTransaction
import net.bonusround.api.utils.launch
import net.bonusround.api.utils.minutes
import net.bonusround.game.Main
import net.bonusround.game.data.containers.DodgeballRatingContainer
import net.bonusround.game.data.entities.DodgeballRatingEntity
import net.bonusround.game.games.Dodgeball
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DodgeballRatingTable : IntIdTable(createTableName("dodgeball_rating")), SavingPlayerTable {
    override fun save(player: Player) {
        DataContainerService.getContainersMap(
            DodgeballRatingContainer::class,
            Int::class,
            UUID::class,
            Dodgeball.Format::class
        )!![player.uniqueId]?.let { map ->
            map.forEach { (f, data) ->
                launch {
                    asyncTransaction {
                        val entity =
                            DodgeballRatingEntity.find { uuid eq player.uniqueId and (format eq f.name.lowercase()) }
                                .firstOrNull()
                        entity?.let {
                            it.wins = data.wins
                            it.losses = data.losses
                            it.rating = data.rating
                        } ?: run {
                            DodgeballRatingEntity.new {
                                uuid = player.uniqueId
                                format = f.name.lowercase()
                                wins = data.wins
                                losses = data.losses
                                rating = data.rating
                            }
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
            val ratings = ConcurrentHashMap<Dodgeball.Format, DodgeballRatingContainer>()

            Dodgeball.Format.entries.forEach { format ->
                asyncTransaction {
                    val entity =
                        DodgeballRatingEntity.find { uuid eq player.uniqueId and (DodgeballRatingTable.format eq format.name.lowercase()) }
                            .firstOrNull()
                    ratings[format] = DodgeballRatingContainer(
                        player.uniqueId,
                        format = format.name.lowercase(),
                        wins = entity?.wins ?: 0,
                        losses = entity?.losses ?: 0,
                        rating = entity?.rating ?: 0
                    )
                    Main.logger.info("Entity for ${player.name} null status: ${entity == null}")
                }
            }

            DataContainerService.getContainersMap(
                DodgeballRatingContainer::class,
                Int::class,
                UUID::class,
                Dodgeball.Format::class
            )!![player.uniqueId] = ratings
            Main.logger.info("Applied ratings for player ${player.name} in mem")
        }
    }

    override fun onQuit(player: Player) {
        save(player)
        DataContainerService.getContainersMap(
            DodgeballRatingContainer::class,
            Int::class,
            UUID::class,
            Dodgeball.Format::class
        )!!.remove(player.uniqueId)
    }

    init {
        launch {
            while (true) {
                PlayerDataTable.saveAll()
                delay(minutes().toMillis(2))
            }
        }
    }

    val uuid = uuid("uuid")
    val format = varchar("format", 10)
    val wins = integer("wins")
    val losses = integer("losses")
    val rating = integer("rating")
}