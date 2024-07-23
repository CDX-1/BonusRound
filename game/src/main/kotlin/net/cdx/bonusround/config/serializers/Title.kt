package net.cdx.bonusround.config.serializers

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

}