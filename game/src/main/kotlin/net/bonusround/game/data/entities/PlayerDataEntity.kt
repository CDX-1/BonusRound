package net.bonusround.game.data.entities

import net.bonusround.game.data.tables.PlayerDataTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PlayerDataEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PlayerDataEntity>(PlayerDataTable)

    var uuid by PlayerDataTable.uuid
    var bits by PlayerDataTable.bits
    var firstSeen by PlayerDataTable.firstSeen
    var lastSeen by PlayerDataTable.lastSeen
    var firstQuit by PlayerDataTable.firstQuit
    var lastQuit by PlayerDataTable.lastQuit
}