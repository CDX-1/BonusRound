package net.bonusround.game.data.entities

import net.bonusround.game.data.tables.DodgeballRatingTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DodgeballRatingEntity(uuid: EntityID<Int>) : IntEntity(uuid) {
    companion object : IntEntityClass<DodgeballRatingEntity>(DodgeballRatingTable)

    var uuid by DodgeballRatingTable.uuid
    var format by DodgeballRatingTable.format
    var wins by DodgeballRatingTable.wins
    var losses by DodgeballRatingTable.losses
    var rating by DodgeballRatingTable.rating
}