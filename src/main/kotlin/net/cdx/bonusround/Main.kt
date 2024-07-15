package net.cdx.bonusround

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.papermc.paper.event.entity.EntityMoveEvent
import net.cdx.bonusround.commands.QueueCommand
import net.cdx.bonusround.games.Dodgeball
import net.cdx.bonusround.games.api.QueueManager
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerCommandSendEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.util.Vector
import java.io.File
import java.util.function.Function
import java.util.logging.Logger

class Main : JavaPlugin() {

    companion object {
        lateinit var instance: Main
        lateinit var logger: Logger
        lateinit var protocolManager: ProtocolManager
        lateinit var langFile: File
        lateinit var langYaml: YamlConfiguration

        val placeholderResolvers: HashMap<String, Function<Player, String>> = HashMap()
        val openCommandAliases = ArrayList<String>()
    }

    override fun onEnable() {
        instance = this
        Companion.logger = logger
        protocolManager = ProtocolLibrary.getProtocolManager()

        saveResource("lang.yml", false)
        langFile = File(dataFolder, "lang.yml")
        langYaml = YamlConfiguration.loadConfiguration(langFile)

        Dodgeball().register()

        CommandAPI.onLoad(CommandAPIBukkitConfig(this))
        CommandAPI.onEnable()

        QueueCommand().register()

        var commandsToRemove: Set<String> = setOf()
        EventListener(PlayerCommandSendEvent::class.java) { event ->
            if (!event.player.isOp) {
                if (commandsToRemove.isEmpty()) {
                    event.commands.forEach { command ->
                        if (openCommandAliases.contains(command)) {
                            return@forEach
                        }
                        commandsToRemove = commandsToRemove.plus(command)
                    }
                }
                event.commands.removeAll(commandsToRemove)
            }
        }

        registerPlaceholders()
        PAPIExtension().register()
    }

    override fun onDisable() {
        CommandAPI.onDisable()
        langYaml.save(langFile)
    }

    private val miniMessage = MiniMessage.miniMessage()
    private fun registerPlaceholders() {
        placeholderResolvers["queue_name"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "/queue"
            return@Function miniMessage.serialize(miniMessage.deserialize(queue.formattedName).color(TextColor.fromHexString("#DB2B39")))
        }

        placeholderResolvers["queue_size"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "/queue"
            return@Function miniMessage.serialize(miniMessage.deserialize("${queue.size}").color(TextColor.fromHexString("#DB2B39")))
        }

        placeholderResolvers["queue_needed"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "/queue"
            return@Function miniMessage.serialize(miniMessage.deserialize("${queue.meta.minPlayers}").color(TextColor.fromHexString("#DB2B39")))
        }

        placeholderResolvers["top_rating"] = Function { player ->
            return@Function "0"
        }
    }

}

object Scheduler : BukkitScheduler by Bukkit.getScheduler()

object Lang {
    object General {
        val prefix = Main.langYaml.get("general.prefix") as String
    }
    object Commands {
        object Queue {
            val noSubCommand = Main.langYaml.get("commands.queue.no-sub-command") as String
            val mustLeaveQueue = Main.langYaml.get("commands.queue.must-leave-queue") as String
            val joinedQueue = Main.langYaml.get("commands.queue.joined-queue") as String
            val notInQueue = Main.langYaml.get("commands.queue.not-in-queue") as String
            val leftQueue = Main.langYaml.get("commands.queue.left-queue") as String
        }
    }
    object Games {
        object General {
            object MatchFoundTitle {
                val header = Main.langYaml.get("games.general.match-found-title.header") as String
                val subtext = Main.langYaml.get("games.general.match-found-title.subtext") as String
            }
            val searchingForArena = Main.langYaml.get("games.general.searching-for-arena") as String
            val arenaFound = Main.langYaml.get("games.general.arena-found") as String
            val arenaSearchTimeout = Main.langYaml.get("games.general.arena-search-timeout") as String
            val arenaInstanceTimeout = Main.langYaml.get("games.general.arena-instance-timeout") as String
        }
    }
}

private class TestListener : Listener {

    @EventHandler
    fun onBowShoot(event: EntityShootBowEvent) {
        event.isCancelled = true
        val entity = event.entity
        val force = event.force
        val item = ItemStack(Material.PLAYER_HEAD)
        val stand = entity.world.spawnEntity(entity.eyeLocation.subtract(Vector(0, 3, 0)), EntityType.ARMOR_STAND) as ArmorStand
        stand.isVisible = false
        stand.isSmall = true
        stand.setBasePlate(false)
        stand.isInvulnerable = true
        stand.equipment.helmet = item
        stand.setNoPhysics(true)
        stand.teleport(entity.eyeLocation.subtract(Vector(0, 1, 0)))
        stand.velocity = entity.eyeLocation.direction
            .normalize()
            .multiply(force /*/ 1.5*/)
        Scheduler.scheduleSyncDelayedTask(Main.instance, {
            stand.equipment.helmet = item
        }, 2L)
    }

    @EventHandler
    fun onEntityMove(event: EntityMoveEvent) {
        if (event.entityType != EntityType.ARMOR_STAND) return
        val belowBlockType = event.entity.world.getBlockAt(event.entity.location.subtract(Vector(0.toDouble(), 0.25, 0.toDouble()))).type
        if (belowBlockType == Material.AIR || belowBlockType == Material.BARRIER || belowBlockType == Material.LIGHT) return
        event.entity.world.spawnParticle(Particle.EXPLOSION, event.entity.location, 3)
        event.entity.world.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.MASTER, 1f, 1f))
        // event.entity.setNoPhysics(true)
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main::class.java), Runnable {
            event.entity.remove()
        }, (20 * 2).toLong())
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.allowFlight = true
        event.player.foodLevel = 29
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        event.isCancelled = true
        event.player.velocity = Vector(0, 1, 0)
        event.player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
    }

    @EventHandler
    fun onOffHandSwap(event: PlayerSwapHandItemsEvent) {
        event.isCancelled = true
        val player = event.player
        player.velocity = player.eyeLocation.direction.multiply(Vector(2, 0, 2)).add(Vector(0, 1, 0))
        player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
    }

}