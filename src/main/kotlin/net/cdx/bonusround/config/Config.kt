package net.cdx.bonusround.config

import net.cdx.bonusround.Main
import org.spongepowered.configurate.objectmapping.ConfigSerializable

fun conf(): Config {
    return Main.conf
}

@ConfigSerializable
class Config {

    var discordToken: String = ""
    var discordChatChannel: String = ""
    var discordChatWebhook: String = ""

}