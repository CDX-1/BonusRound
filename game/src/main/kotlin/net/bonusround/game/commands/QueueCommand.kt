package net.bonusround.game.commands

import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.executors.CommandArguments
import net.bonusround.api.commands.Command
import net.bonusround.api.game.QueueManager
import net.bonusround.api.utils.component
import net.bonusround.api.utils.send
import net.bonusround.api.utils.sendComponent
import net.bonusround.game.configs.lang
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.entity.Player

class QueueCommand : Command(
    name = "queue",
    shortDescription = "Join the game queue",
    fullDescription = "Join the the matchmaking queue for the game and format of your choice",
    subcommands = mutableListOf(
        QueueDodgeballSubCommand(),
        QueueLeaveSubCommand()
    ),
    permission = "commands.default.queue",
    aliases = arrayOf("q", "match", "matchmaking", "mm"),
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, _ ->
        lang().commands.queue.noSubCommands sendComponent player
        player.playSound(Sound.sound(Key.key("block.anvil.land"), Sound.Source.MASTER, 2f, 1f))
    }
}

class QueueDodgeballSubCommand : Command(
    name = "dodgeball",
    shortDescription = "Join the dodgeball queue",
    fullDescription = "Join the dodgeball matchmaking queue in the format of your choice",
    arguments = mutableListOf(
        MultiLiteralArgument("format", "1v1", "2v2")
    ),
    permission = "commands.default.queue"
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, args ->
        if (QueueManager isInQueue player) {
            lang().commands.queue.mustLeaveQueue sendComponent player
            player.playSound(Sound.sound(Key.key("block.anvil.land"), Sound.Source.MASTER, 2f, 1f))
            return@BiConsumer
        }
        val formatId: String = args.get("format") as String

        // TODO Implement 2v2s
        if (formatId.equals("2v2", ignoreCase = true)) {
            player.sendMessage("That format is currently in development.")
            return@BiConsumer
        }

        val queue = QueueManager getQueue "dodgeball_1v1"
        val success = queue!!.addPlayer(player)

        if (success) {
            lang().commands.queue.joinedQueue.component(values = arrayOf("Dodgeball $formatId")) send player
            player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 2f, 1f))
        } else {
            lang().commands.queue.inGame sendComponent player
            player.playSound(Sound.sound(Key.key("block.anvil.land"), Sound.Source.MASTER, 2f, 1f))
        }
    }
}

class QueueLeaveSubCommand : Command(
    "leave",
    shortDescription = "Leave your current queue",
    fullDescription = "Remove yourself from the matchmaking of your current queue",
    permission = "commands.default.queue"
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, _ ->
        if (!(QueueManager isInQueue player)) {
            lang().commands.queue.notInQueue sendComponent player
            player.playSound(Sound.sound(Key.key("block.anvil.land"), Sound.Source.MASTER, 2f, 1f))
            return@BiConsumer
        }
        val queue = QueueManager getQueueOf player
        if (queue != null) {
            queue removePlayer player
            lang().commands.queue.leftQueue sendComponent player
            player.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.MASTER, 2f, 1f))
        }
    }
}