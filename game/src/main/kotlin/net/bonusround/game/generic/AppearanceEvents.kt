package net.bonusround.game.generic

import io.papermc.paper.event.player.AsyncChatEvent
import net.bonusround.api.commands.BonusRoundCommandList
import net.bonusround.api.utils.EventListener
import net.bonusround.api.utils.Registrable
import net.bonusround.api.utils.component
import net.bonusround.api.utils.sendComponent
import net.bonusround.game.configs.lang
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*
import org.bukkit.scoreboard.Team

class AppearanceEvents : Registrable {

    override fun register() {

        // CHAT EVENTS

        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        val noCollisionTeam = scoreboard.getTeam("noCollisions") ?: scoreboard.registerNewTeam("noCollisions")
        noCollisionTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)

        fun Player.setCollisions(value: Boolean) {
            if (!value) {
                noCollisionTeam.addPlayer(this)
            } else {
                noCollisionTeam.removePlayer(this)
            }
        }

        EventListener(PlayerJoinEvent::class.java) { event ->
            val player = event.player

            if (player.hasPlayedBefore()) {
                event.joinMessage(
                    lang().general.join.component(usePrefix = false, usePAPI = true, papiPlayer = player)
                )
            } else {
                event.joinMessage(
                    lang().general.joinUnique.component(usePrefix = false, usePAPI = true, papiPlayer = player)
                )
            }

            player.teleport(Location(Bukkit.getWorld("world"), 0.5, 65.0, 0.5))
            player.gameMode = GameMode.ADVENTURE
            player.inventory.clear()
            player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
            player.playSound(Sound.sound(Key.key("entity.firework_rocket.twinkle"), Sound.Source.MASTER, 2f, 1f))
            player.setCollisions(false)
        }

        EventListener(PlayerQuitEvent::class.java) { event ->
            event.quitMessage(
                lang().general.quit.component(usePrefix = false, usePAPI = true, papiPlayer = event.player)
            )
            event.player.setCollisions(true)
        }

        // LOBBY EVENTS

        EventListener(PlayerChangedWorldEvent::class.java) { event ->
            val player = event.player
            if (player.world.name != "world") {
                event.player.setCollisions(true)
                return@EventListener
            }
            event.player.setCollisions(false)
            player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
            player.foodLevel = 20
        }

        EventListener(FoodLevelChangeEvent::class.java) { event ->
            if (event.entity.world.name == "world") event.isCancelled = true
        }

        EventListener(PlayerMoveEvent::class.java) { event ->
            if (event.player.world.name != "world") return@EventListener
            if (50 < event.player.location.y) return@EventListener
            event.player.teleport(Location(Bukkit.getWorld("world"), 0.5, 65.0, 0.5))
            event.player.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.MASTER, 2f, 1f))
        }

        val renderer = MessageRenderer()
        EventListener(AsyncChatEvent::class.java) { event ->
            event.renderer(renderer)
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
            val commandName = event.message.split(" ").first().removePrefix("/")
            if (commandName.contains(":")) {
                event.isCancelled = true
                lang().general.unknownCommand sendComponent event.player
                return@EventListener
            }
            val command = Bukkit.getServer().commandMap.getCommand(commandName)
            if (command == null) {
                event.isCancelled = true
                lang().general.unknownCommand sendComponent event.player
                return@EventListener
            }
            if (BonusRoundCommandList.contains(command.name)) {
                if (!event.player.hasPermission(BonusRoundCommandList[command.name]!!.permission!!)) {
                    event.isCancelled = true
                    lang().general.unknownCommand sendComponent event.player
                }
            } else {
                if (command.permission == null) {
                    return@EventListener
                }
                if (!event.player.hasPermission(command.permission!!)) {
                    event.isCancelled = true
                    lang().general.unknownCommand sendComponent event.player
                }
            }
        }

    }

}