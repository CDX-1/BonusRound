package net.cdx.bonusround.commands

import dev.jorel.commandapi.executors.CommandArguments
import net.cdx.bonusround.Command
import net.cdx.bonusround.Main
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.entity.Player

class HelpCommand : Command(
    name = "help",
    shortDescription = "Learn more about the server",
    fullDescription = "Get access to an extensive guide covering the different games, commands and mechanics",
    permission = "commands.default.help"
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, args ->
        val gui = Main.guiRegistry.get("help")

        gui.setOnClose { event ->
            event.player.sendMessage("closed help menu")
        }

        gui.show(player)
    }
}