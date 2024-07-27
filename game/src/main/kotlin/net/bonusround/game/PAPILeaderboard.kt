package net.bonusround.game

import kotlinx.coroutines.delay
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.bonusround.api.utils.asyncTransaction
import net.bonusround.api.utils.launch
import net.bonusround.api.utils.millis
import net.bonusround.api.utils.minutes
import net.bonusround.game.data.tables.DodgeballRatingTable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SortOrder
import java.util.*

class PAPILeaderboard : PlaceholderExpansion() {
    companion object {
        private val leaderboards = HashMap<String, ArrayList<Pair<UUID, String>>>()
        private var lastUpdate = 0L

        suspend fun refreshLeaderboards() {
            lastUpdate = System.currentTimeMillis()
            leaderboards.clear()

            asyncTransaction {
                leaderboards["dodgeball_1v1_wins"] =
                    DodgeballRatingTable
                        .select(DodgeballRatingTable.uuid, DodgeballRatingTable.wins)
                        .where { DodgeballRatingTable.format eq "1v1" }
                        .orderBy(DodgeballRatingTable.wins to SortOrder.DESC)
                        .limit(10)
                        .map { it[DodgeballRatingTable.uuid] to it[DodgeballRatingTable.wins].toString() } as ArrayList<Pair<UUID, String>>

                leaderboards["dodgeball_1v1_rating"] =
                    DodgeballRatingTable
                        .select(DodgeballRatingTable.uuid, DodgeballRatingTable.rating)
                        .where { DodgeballRatingTable.format eq "1v1" }
                        .orderBy(DodgeballRatingTable.rating to SortOrder.DESC)
                        .limit(10)
                        .map { it[DodgeballRatingTable.uuid] to it[DodgeballRatingTable.rating].toString() } as ArrayList<Pair<UUID, String>>
            }
        }
    }

    init {
        launch {
            while (true) {
                refreshLeaderboards()
                delay(minutes().toMillis(5))
            }
        }
    }

    override fun getIdentifier(): String {
        return "brlb"
    }

    override fun getAuthor(): String {
        return Main.instance.pluginMeta.authors.joinToString(", ")
    }

    override fun getVersion(): String {
        return Main.instance.pluginMeta.version
    }

    override fun canRegister(): Boolean {
        return true
    }

    override fun persist(): Boolean {
        return true
    }

    private fun getLeaderboardValue(category: String, spot: Int): String {
        return leaderboards[category]?.getOrNull(spot)?.let {
            "${Bukkit.getOfflinePlayer(it.first).name}&8 - &#DB2B39${it.second}"
        } ?: "Loading..."
    }

    private fun handleDodgeball(args: List<String>): String {
        if (args.size <= 3) return "null"
        val format = args[1]
        val type = args[2]
        val spot = args[3].toIntOrNull() ?: return "null"
        return when (type) {
            "wins" -> getLeaderboardValue("dodgeball_${format}_wins", spot - 1)
            "rating" -> getLeaderboardValue("dodgeball_${format}_rating", spot - 1)
            else -> "null"
        }
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String {
        if (lastUpdate.toInt() == 0) {
            launch {
                refreshLeaderboards()
            }
            return "Loading..."
        }
        val args = params.split("_")
        if (args.isEmpty()) return "null"
        return when (args[0]) {
            "dodgeball" -> if (args.size <= 3) "null" else handleDodgeball(args)
            "next" -> {
                return millis().toMinutes(lastUpdate + minutes().toMillis(5) - System.currentTimeMillis()).toString()
            }

            else -> "null"
        }
    }
}