package net.cdx.bonusround

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class Command(
    private val name: String,
    private val shortDescription: String? = null,
    private val fullDescription: String? = null,
    private val permission: String? = null,
    private val arguments: MutableList<out Argument<*>> = mutableListOf(),
    private val subcommands: MutableList<out Command> = mutableListOf(),
    private val aliases: Array<out String> = arrayOf()
) {

    open var onPlayer: BiConsumer<Player, CommandArguments>? = null
    open var onConsole: BiConsumer<CommandSender, CommandArguments>? = null

    private fun getCommand(): CommandAPICommand {
        val command = CommandAPICommand(name)
            .withAliases(*aliases)
        if (shortDescription != null) {
            command.withShortDescription(shortDescription)
        }
        if (fullDescription != null) {
            command.withFullDescription(fullDescription)
        }
        if (permission != null) {
            command.withPermission(permission)
        }
        if (arguments.isNotEmpty()) {
            command.withArguments(arguments)
        }
        if (subcommands.isNotEmpty()) {
            subcommands.forEach {
                command.withSubcommand(it.getCommand())
            }
        }

        command.executes(CommandExecutor { sender, args ->
            if (sender is Player) {
                onPlayer?.accept(sender, args)
            } else {
                onConsole?.accept(sender, args)
            }
        })

        return command
    }

    fun register() {
        val command = getCommand()
        command.register(Main.instance)
        if (command.permission == null) {
            Main.openCommandAliases.add(command.name)
            Main.openCommandAliases.addAll(command.aliases)
        }
    }

}