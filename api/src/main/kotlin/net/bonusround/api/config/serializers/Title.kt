package net.bonusround.api.config.serializers

import net.bonusround.api.utils.component
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class Title {

    companion object {
        fun of(header: String, subtext: String): Title {
            val title = Title()
            title.header = header
            title.subtext = subtext
            return title
        }
    }

    var header: String = ""
    var subtext: String = ""

    fun toTitle(usePrefix: Boolean = false, usePAPI: Boolean = false, papiPlayer: Player? = null, vararg values: String): net.kyori.adventure.title.Title {
        return net.kyori.adventure.title.Title.title(header.component(usePrefix, usePAPI, papiPlayer, values = values), subtext.component(usePrefix, usePAPI, papiPlayer, values = values))
    }
}