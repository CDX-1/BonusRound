package net.cdx.bonusround.commands

import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.executors.CommandArguments
import net.cdx.bonusround.Command
import net.cdx.bonusround.utils.Animations
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.entity.Player

class Test : Command(
    name = "test",
    arguments = mutableListOf(
        IntegerArgument("radius")
    )
) {
    override var onPlayer: BiConsumer<Player, CommandArguments>? = BiConsumer { player, args ->
        Animations.createBlockWave(player.location, args.getOrDefault("radius", 6) as Int)
    }
}