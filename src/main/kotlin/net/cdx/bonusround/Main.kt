package net.cdx.bonusround

import com.github.retrooper.packetevents.PacketEvents
import com.github.shynixn.mccoroutine.bukkit.MCCoroutine
import com.github.shynixn.mccoroutine.bukkit.impl.MCCoroutineImpl
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import net.cdx.bonusround.commands.DiscordCommand
import net.cdx.bonusround.commands.HelpCommand
import net.cdx.bonusround.commands.QueueCommand
import net.cdx.bonusround.config.ConfigLoader
import net.cdx.bonusround.config.Lang
import net.cdx.bonusround.config.Overrides
import net.cdx.bonusround.games.Dodgeball
import net.cdx.bonusround.games.api.QueueManager
import net.cdx.bonusround.generic.AppearanceEvents
import net.cdx.bonusround.gui.GuiRegistry
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function
import java.util.logging.Logger

class Main : JavaPlugin() {

    companion object {
        lateinit var instance: Main
        lateinit var logger: Logger
        lateinit var guiRegistry: GuiRegistry

        lateinit var lang: Lang
        lateinit var overrides: Overrides

        val placeholderResolvers: HashMap<String, Function<Player, String>> = HashMap()
    }

    private lateinit var langLoader: ConfigLoader<Lang>
    private lateinit var overridesLoader: ConfigLoader<Overrides>

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings.reEncodeByDefault(false)
            .checkForUpdates(true)
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {

        // INIT

        instance = this
        Companion.logger = logger
        guiRegistry = GuiRegistry()

        // PACKET EVENTS

        PacketEvents.getAPI().init()

        // LANG

        langLoader = ConfigLoader("lang.conf", Lang::class)
        lang = langLoader.load()
        overridesLoader = ConfigLoader("overrides.conf", Overrides::class)
        overrides = overridesLoader.load()

        // LISTENERS

        AppearanceEvents().register()

        // GAMES

        Dodgeball().register()

        // COMMANDS

        CommandAPI.onLoad(CommandAPIBukkitConfig(this))
        CommandAPI.onEnable()

        QueueCommand().register()
        DiscordCommand().register()
        HelpCommand().register()

        // PLACEHOLDER API

        registerPlaceholders()
        PAPIExtension().register()

        // OVERRIDES

        overrides.commandPermissionOverrides.forEach { commandPermissionOverride ->
            val command = server.commandMap.getCommand(commandPermissionOverride)
            command?.permission = "override"
        }

        // removing this breaks mccoroutine, no idea why
        println(MCCoroutine::class.java.classLoader)
        println(MCCoroutineImpl::class.java.classLoader)
    }

    override fun onDisable() {
        CommandAPI.onDisable()
        langLoader.save()
        overridesLoader.save()
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

//private class TestListener : Listener {
//
//    @EventHandler
//    fun onBowShoot(event: EntityShootBowEvent) {
//        event.isCancelled = true
//        val entity = event.entity
//        val force = event.force
//        val item = ItemStack(Material.PLAYER_HEAD)
//        val stand = entity.world.spawnEntity(entity.eyeLocation.subtract(Vector(0, 3, 0)), EntityType.ARMOR_STAND) as ArmorStand
//        stand.isVisible = false
//        stand.isSmall = true
//        stand.setBasePlate(false)
//        stand.isInvulnerable = true
//        stand.equipment.helmet = item
//        stand.setNoPhysics(true)
//        stand.teleport(entity.eyeLocation.subtract(Vector(0, 1, 0)))
//        stand.velocity = entity.eyeLocation.direction
//            .normalize()
//            .multiply(force /*/ 1.5*/)
//        Scheduler.scheduleSyncDelayedTask(Main.instance, {
//            stand.equipment.helmet = item
//        }, 2L)
//    }
//
//    @EventHandler
//    fun onEntityMove(event: EntityMoveEvent) {
//        if (event.entityType != EntityType.ARMOR_STAND) return
//        val belowBlockType = event.entity.world.getBlockAt(event.entity.location.subtract(Vector(0.toDouble(), 0.25, 0.toDouble()))).type
//        if (belowBlockType == Material.AIR || belowBlockType == Material.BARRIER || belowBlockType == Material.LIGHT) return
//        event.entity.world.spawnParticle(Particle.EXPLOSION, event.entity.location, 3)
//        event.entity.world.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.MASTER, 1f, 1f))
//        // event.entity.setNoPhysics(true)
//        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main::class.java), Runnable {
//            event.entity.remove()
//        }, (20 * 2).toLong())
//    }
//
//    @EventHandler
//    fun onPlayerJoin(event: PlayerJoinEvent) {
//        event.player.allowFlight = true
//        event.player.foodLevel = 29
//    }
//
//    @EventHandler
//    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
//        event.isCancelled = true
//    }
//
//    @EventHandler
//    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
//        event.isCancelled = true
//        event.player.velocity = Vector(0, 1, 0)
//        event.player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
//    }
//
//    @EventHandler
//    fun onOffHandSwap(event: PlayerSwapHandItemsEvent) {
//        event.isCancelled = true
//        val player = event.player
//        player.velocity = player.eyeLocation.direction.multiply(Vector(2, 0, 2)).add(Vector(0, 1, 0))
//        player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
//    }
//
//}