package net.bonusround.game.data.schemas.entities

import net.bonusround.game.data.schemas.tables.PlayerDataTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class PlayerDataEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<PlayerDataEntity>(PlayerDataTable)

    var bits by PlayerDataTable.bits
    var firstSeen by PlayerDataTable.firstSeen
    var lastSeen by PlayerDataTable.lastSeen
    var firstQuit by PlayerDataTable.firstQuit
    var lastQuit by PlayerDataTable.lastQuit
}