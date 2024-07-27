package net.bonusround.game.data.providers

import net.bonusround.api.data.DataContainerService
import net.bonusround.game.data.containers.DodgeballRatingContainer
import net.bonusround.game.data.containers.PlayerDataContainer
import net.bonusround.game.games.Dodgeball
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val playerDataProviders = HashMap<UUID, PlayerDataProvider>()

class PlayerDataProvider private constructor(private val player: Player) {

    companion object {
        fun getPlayer(player: Player): PlayerDataProvider {
            return playerDataProviders.getOrPut(player.uniqueId) { PlayerDataProvider((player)) }
        }
    }

    val playerData: PlayerDataContainer?
        get() {
            return DataContainerService.getContainers(PlayerDataContainer::class, Int::class, UUID::class)
                ?.get(player.uniqueId)
        }

    val dodgeballRatings: ConcurrentHashMap<Dodgeball.Format, DodgeballRatingContainer>?
        get() {
            return DataContainerService.getContainersMap(
                DodgeballRatingContainer::class,
                Int::class,
                UUID::class,
                Dodgeball.Format::class
            )?.get(player.uniqueId)
        }
}