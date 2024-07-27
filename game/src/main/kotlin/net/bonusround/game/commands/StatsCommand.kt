package net.bonusround.game.commands

import dev.jorel.commandapi.executors.CommandArguments
import net.bonusround.api.commands.Command
import net.bonusround.api.gui.openGui
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.entity.Player

class StatsCommand : Command(
    name = "stats",
    shortDescription = "Check your game stats",
    permission = "commands.default.stats",
    aliases = arrayOf("stat", "viewstats")
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, _ ->
        player.openGui("stats")
    }
}