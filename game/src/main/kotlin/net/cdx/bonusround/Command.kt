package net.cdx.bonusround

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object BonusRoundCommandList : MutableMap<String, Command> by hashMapOf()

abstract class Command(
    val name: String,
    val shortDescription: String? = null,
    val fullDescription: String? = null,
    val permission: String? = null,
    val arguments: MutableList<out Argument<*>> = mutableListOf(),
    val subcommands: MutableList<out Command> = mutableListOf(),
    val aliases: Array<out String> = arrayOf(),
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
            if (permission == "op") {
                command.withPermission(CommandPermission.OP)
            } else {
                command.withPermission(CommandPermission.fromString(permission))
            }
        } else {
            command.withPermission(CommandPermission.NONE)
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
        BonusRoundCommandList[name] = this
        aliases.forEach { alias ->
            BonusRoundCommandList[alias] = this
        }
        val command = getCommand()
        command.register(Main.instance)
    }

}