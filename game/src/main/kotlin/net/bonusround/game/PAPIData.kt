package net.bonusround.game

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.bonusround.game.data.containers.DodgeballRatingContainer
import net.bonusround.game.data.containers.PlayerDataContainer
import net.bonusround.game.extensions.dataProvider
import net.bonusround.game.games.Dodgeball
import org.bukkit.entity.Player
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class PAPIData : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "data"
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

    override fun onPlaceholderRequest(player: Player, params: String): String {
        val args = params.split("_")
        if (args.size <= 1) return "null"
        return when (args[0]) {
            "playerdata" -> {
                player.dataProvider.playerData?.let { data ->
                    val value = PlayerDataContainer::class.memberProperties.find { prop -> prop.name == args[1] }
                    value?.isAccessible = true
                    value?.get(data)?.toString() ?: "null"
                } ?: "null"
            }
            "dodgeball" -> {
                if (args.size <= 2) return "null"
                player.dataProvider.dodgeballRatings?.let { ratings ->
                    try {
                        ratings[Dodgeball.Format.valueOf(args[1])]?.let { rating ->
                            val value = DodgeballRatingContainer::class.memberProperties.find { prop -> prop.name == args[2] }
                            value?.isAccessible = true
                            value?.get(rating)?.toString() ?: "null"
                        }
                    } catch (error: IllegalArgumentException) {
                        "null"
                    }
                } ?: "null"
            }
            else -> "null"
        }
    }
}