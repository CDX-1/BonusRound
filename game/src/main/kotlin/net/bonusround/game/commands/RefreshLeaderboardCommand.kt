package net.bonusround.game.commands

import dev.jorel.commandapi.executors.CommandArguments
import net.bonusround.api.commands.Command
import net.bonusround.api.utils.launch
import net.bonusround.api.utils.sendComponent
import net.bonusround.game.PAPILeaderboard
import net.bonusround.game.configs.lang
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RefreshLeaderboardCommand : Command(
    name = "refreshleaderboard",
    shortDescription = "Refresh server leaderboards",
    permission = "commands.admin.refreshlb",
    aliases = arrayOf("refreshlb", "rlb")
) {

    private suspend fun refreshLeaderboards() {
        PAPILeaderboard.refreshLeaderboards()
    }

    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, _ ->
        launch {
            refreshLeaderboards()
            lang().commands.refreshLeaderboard.refreshed sendComponent player
        }
    }
    override var onConsole: BiConsumer<CommandSender, CommandArguments>? = BiConsumer { sender, _ ->
        launch {
            refreshLeaderboards()
            lang().commands.refreshLeaderboard.refreshed sendComponent sender
        }
    }
}