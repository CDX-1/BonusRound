package net.bonusround.game.data.containers

import net.bonusround.api.data.DataContainer
import net.bonusround.game.data.entities.DodgeballRatingEntity
import net.bonusround.game.data.tables.DodgeballRatingTable
import java.util.UUID

data class DodgeballRatingContainer(
    val uuid: UUID,
    var format: String,
    var wins: Int,
    var losses: Int,
    var rating: Int
) : DataContainer<Int, DodgeballRatingEntity, DodgeballRatingTable>() {
    override fun getTable(): DodgeballRatingTable {
        return DodgeballRatingTable
    }
}
