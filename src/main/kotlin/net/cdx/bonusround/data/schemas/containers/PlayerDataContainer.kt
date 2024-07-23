package net.cdx.bonusround.data.schemas.containers

import net.cdx.bonusround.data.DataContainer
import net.cdx.bonusround.data.schemas.entities.PlayerDataEntity
import net.cdx.bonusround.data.schemas.tables.PlayerDataTable
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
