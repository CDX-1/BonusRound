package net.bonusround.game.commands

import dev.jorel.commandapi.executors.CommandArguments
import net.bonusround.api.commands.Command
import net.bonusround.api.utils.Formatter
import net.bonusround.game.configs.lang
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.entity.Player

class DiscordCommand : Command(
    name = "discord",
    shortDescription = "Interact with our Discord server",
    fullDescription = "Get a link to our Discord server or manage your account syncing",
    subcommands = mutableListOf(),
    permission = "commands.default.discord",
    aliases = arrayOf("d", "dis")
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, _ ->
        player.sendMessage(Formatter(lang().commands.discord.serverInvite).component())
    }
}