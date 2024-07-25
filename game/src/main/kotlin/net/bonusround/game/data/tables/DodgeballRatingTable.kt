package net.bonusround.game.data.tables

import net.bonusround.api.data.DataContainerService
import net.bonusround.api.data.PlayerTable
import net.bonusround.api.data.createTableName
import net.bonusround.api.utils.asyncTransaction
import net.bonusround.api.utils.launch
import net.bonusround.game.data.containers.DodgeballRatingContainer
import net.bonusround.game.data.entities.DodgeballRatingEntity
import net.bonusround.game.games.Dodgeball
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DodgeballRatingTable : IntIdTable(createTableName("dodgeball_rating")), PlayerTable {
    override fun onJoin(player: Player) {
        launch {
            val ratings = ConcurrentHashMap<Dodgeball.Format, DodgeballRatingContainer>()

            Dodgeball.Format.entries.forEach { format ->
                asyncTransaction {
                    val entity = DodgeballRatingEntity.find { uuid eq player.uniqueId and (DodgeballRatingTable.format eq format.name.lowercase()) }.firstOrNull()
                    ratings[format] = DodgeballRatingContainer(
                        player.uniqueId,
                        format = format.name.lowercase(),
                        wins = entity?.wins ?: 0,
                        losses = entity?.losses ?: 0,
                        rating = entity?.rating ?: 0
                    )
                }
            }

            DataContainerService.getContainersMap(DodgeballRatingContainer::class, Int::class, UUID::class, Dodgeball.Format::class)!![player.uniqueId] = ratings
        }
    }

    override fun onQuit(player: Player) {
        DataContainerService.getContainersMap(
            DodgeballRatingContainer::class,
            Int::class,
            UUID::class,
            Dodgeball.Format::class
        )!![player.uniqueId]?.let { map ->
            map.forEach { (f, data) ->
                launch {
                    asyncTransaction {
                        val entity = DodgeballRatingEntity.find { uuid eq player.uniqueId and (format eq f.name.lowercase()) }.firstOrNull()
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

    val uuid = uuid("uuid")
    val format = varchar("format", 10)
    val wins = integer("wins")
    val losses = integer("losses")
    val rating = integer("rating")
}