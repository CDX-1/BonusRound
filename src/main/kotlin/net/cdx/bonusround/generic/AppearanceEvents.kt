package net.cdx.bonusround.generic

import net.cdx.bonusround.*
import net.cdx.bonusround.config.lang
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*

class AppearanceEvents : Registrable {

    override fun register() {

        // CHAT EVENTS

        EventListener(PlayerJoinEvent::class.java) { event ->
            if (event.player.hasPlayedBefore()) {
                event.joinMessage(Formatter(lang().general.join)
                    .usePrefix(false)
                    .usePAPI(true, event.player)
                    .component())
            } else {
                event.joinMessage(Formatter(lang().general.joinUnique)
                    .usePrefix(false)
                    .usePAPI(true, event.player)
                    .component())
            }
        }

        EventListener(PlayerQuitEvent::class.java) { event ->
            event.quitMessage(Formatter(lang().general.quit)
                .usePrefix(false)
                .usePAPI(true, event.player)
                .component())
        }

        // LOBBY EVENTS

        EventListener(PlayerChangedWorldEvent::class.java) { event ->
            val player = event.player
            if (player.world.name != "world") return@EventListener
            player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
            player.foodLevel = 20
        }

        EventListener(FoodLevelChangeEvent::class.java) { event ->
            if (event.entity.world.name == "world") event.isCancelled = true
        }

        // SIMPLER COMMANDS

        EventListener(PlayerCommandSendEvent::class.java) { event ->
            if (event.player.isOp) return@EventListener
            event.commands.removeIf { commandName ->
                if (BonusRoundCommandList.contains(commandName)) return@removeIf false
                val command = Bukkit.getServer().commandMap.getCommand(commandName) ?: return@removeIf false
                if (command.permission == null) {
                    true
                } else {
                    !event.player.hasPermission(command.permission!!)
                }
            }
        }

        EventListener(PlayerCommandPreprocessEvent::class.java) { event ->
            val command = Bukkit.getServer().commandMap.getCommand(event.message.split(" ").first.removePrefix("/"))
            if (command == null) {
                event.isCancelled = true
                event.player.sendMessage(
                    Formatter(lang().general.unknownCommand).component()
                )
                return@EventListener
            }
            if (BonusRoundCommandList.contains(command.name)) {
                if (command.permission == null) {
                    return@EventListener
                }
                if (!event.player.hasPermission(BonusRoundCommandList[command.name]!!.permission!!)) {
                    event.isCancelled = true
                    event.player.sendMessage(
                        Formatter(lang().general.unknownCommand).component()
                    )
                }
            } else {
                if (command.permission == null) {
                    return@EventListener
                }
                if (!event.player.hasPermission(command.permission!!)) {
                    event.isCancelled = true
                    event.player.sendMessage(
                        Formatter(lang().general.unknownCommand).component()
                    )
                }
            }
        }

    }

}