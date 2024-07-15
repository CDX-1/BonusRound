package net.cdx.bonusround.commands

import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.executors.CommandArguments
import net.cdx.bonusround.Command
import net.cdx.bonusround.Lang
import net.cdx.bonusround.format
import net.cdx.bonusround.games.api.QueueManager
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
    aliases = arrayOf("q", "match", "matchmaking", "mm"),
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, args ->
        player.sendMessage(format(Lang.Commands.Queue.noSubCommand))
        player.playSound(Sound.sound(Key.key("block.anvil.land"), Sound.Source.MASTER, 2f, 1f))
    }
}

class QueueDodgeballSubCommand : Command(
    name = "dodgeball",
    shortDescription = "Join the dodgeball queue",
    fullDescription = "Join the dodgeball matchmaking queue in the format of your choice",
    arguments = mutableListOf(
        MultiLiteralArgument("format", "1v1", "2v2")
    )
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, args ->
        if (QueueManager isInQueue player) {
            player.sendMessage(format(Lang.Commands.Queue.mustLeaveQueue))
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
        queue!!.addPlayer(player)

        player.sendMessage(format(Lang.Commands.Queue.joinedQueue, true, "Dodgeball $formatId"))
        player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 2f, 1f))
    }
}

class QueueLeaveSubCommand : Command(
    "leave",
    shortDescription = "Leave your current queue",
    fullDescription = "Remove yourself from the matchmaking of your current queue"
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, args ->
        if (!(QueueManager isInQueue player)) {
            player.sendMessage(format(Lang.Commands.Queue.notInQueue))
            player.playSound(Sound.sound(Key.key("block.anvil.land"), Sound.Source.MASTER, 2f, 1f))
            return@BiConsumer
        }
        val queue = QueueManager getQueueOf player
        if (queue != null) {
            queue removePlayer player
            player.sendMessage(format(Lang.Commands.Queue.leftQueue))
            player.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.MASTER, 2f, 1f))
        }
    }
}