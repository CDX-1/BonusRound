package net.cdx.bonusround.data

import net.cdx.bonusround.data.schemas.containers.PlayerDataContainer
import org.bukkit.entity.Player
import java.util.*

private val playerDataProviders = HashMap<UUID, PlayerDataProvider>()

class PlayerDataProvider private constructor(private val player: Player) {

    companion object {
        fun getPlayer(player: Player): PlayerDataProvider {
            return playerDataProviders.getOrPut(player.uniqueId) { PlayerDataProvider((player)) }
        }
    }

    private var playerDataContainer: PlayerDataContainer? = null

    val playerData: PlayerDataContainer?
        get() {
            if (playerDataContainer == null) {
                playerDataContainer =
                    DataContainerService.getContainers(PlayerDataContainer::class, UUID::class)?.get(player.uniqueId)
            }
            return playerDataContainer
        }
}