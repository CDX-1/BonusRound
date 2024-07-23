package net.bonusround.game.discord

import net.bonusround.api.utils.Registrable
import net.bonusround.game.configs.conf
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent

fun bot(): JDA {
    return Bot.bot
}

fun JDA.serverStopped() {
    DiscordChat.shutdown()
    this.shutdown()
}

class Bot : Registrable {

    companion object {
        lateinit var bot: JDA
    }

    override fun register() {

        bot = JDABuilder.createLight(conf().discordToken)
            .setEnabledIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
            .addEventListeners(DiscordChat())
            .build()

        bot.presence.setPresence(Activity.playing("Bonus Round"), false)
    }

}