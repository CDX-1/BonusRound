package net.bonusround.game.data.schemas.containers

import net.bonusround.game.data.DataContainer
import net.bonusround.game.data.schemas.entities.PlayerDataEntity
import net.bonusround.game.data.schemas.tables.PlayerDataTable
import java.util.*

data class PlayerDataContainer(
    val id: UUID,
    var bits: Int = 0,
    var firstSeen: Long = 0,
    var lastSeen: Long = 0,
    var firstQuit: Long = 0,
    var lastQuit: Long = 0,
) : DataContainer<UUID, PlayerDataEntity, PlayerDataTable>() {
    override fun getTable(): PlayerDataTable {
        return PlayerDataTable
    }
}
