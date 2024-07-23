package net.bonusround.game.configs

import net.bonusround.game.Main
import org.spongepowered.configurate.objectmapping.ConfigSerializable

fun conf(): Config {
    return Main.conf
}

@ConfigSerializable
class Config {

    var databaseType: String = "sqlite"
    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var sqliteFileName: String = "data"

    var discordToken: String = ""
    var discordChatChannel: String = ""
    var discordChatWebhook: String = ""

}