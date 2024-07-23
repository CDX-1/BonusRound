package net.bonusround.game

import com.github.retrooper.packetevents.PacketEvents
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import net.bonusround.api.BonusRoundAPI
import net.bonusround.game.commands.DiscordCommand
import net.bonusround.game.commands.HelpCommand
import net.bonusround.game.commands.QueueCommand
import net.bonusround.game.configs.Config
import net.bonusround.api.config.ConfigLoader
import net.bonusround.game.configs.Lang
import net.bonusround.game.configs.Overrides
import net.bonusround.game.data.DataManager
import net.bonusround.game.discord.Bot
import net.bonusround.game.discord.bot
import net.bonusround.game.discord.serverStopped
import net.bonusround.game.extensions.dataProvider
import net.bonusround.game.games.Dodgeball
import net.bonusround.api.game.QueueManager
import net.bonusround.api.utils.Formatter
import net.bonusround.game.configs.lang
import net.bonusround.game.generic.AppearanceEvents
import net.bonusround.game.generic.ItemEvents
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.util.function.Function
import java.util.logging.Logger

class Main : SuspendingJavaPlugin() {

    companion object {
        lateinit var instance: Main
        lateinit var logger: Logger

        lateinit var conf: Config
        lateinit var lang: Lang
        lateinit var overrides: Overrides

        val placeholderResolvers: HashMap<String, Function<Player, String>> = HashMap()
    }

    private lateinit var configLoader: ConfigLoader<Config>
    private lateinit var langLoader: ConfigLoader<Lang>
    private lateinit var overridesLoader: ConfigLoader<Overrides>

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings.reEncodeByDefault(false)
            .checkForUpdates(true)
        PacketEvents.getAPI().load()
    }

    override suspend fun onEnableAsync() {

        // INIT

        BonusRoundAPI.main = this

        instance = this
        Companion.logger = logger

        // PACKET EVENTS

        PacketEvents.getAPI().init()

        // LANG

        configLoader = ConfigLoader("config.conf", Config::class)
        conf = configLoader.load()
        langLoader = ConfigLoader("lang.conf", Lang::class)
        lang = langLoader.load()
        overridesLoader = ConfigLoader("overrides.conf", Overrides::class)
        overrides = overridesLoader.load()

        Formatter.prefix = lang().general.prefix

        // DATABASE

        DataManager().register()

        // DISCORD

        Bot().register()

        // LISTENERS

        AppearanceEvents().register()
        ItemEvents().register()

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
    }

    override suspend fun onDisableAsync() {
        CommandAPI.onDisable()
        configLoader.save()
        langLoader.save()
        overridesLoader.save()
        bot().serverStopped()
    }

    private val miniMessage = MiniMessage.miniMessage()
    private fun registerPlaceholders() {
        placeholderResolvers["queue_name"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "/queue"
            return@Function miniMessage.serialize(
                miniMessage.deserialize(queue.formattedName).color(TextColor.fromHexString("#DB2B39"))
            )
        }

        placeholderResolvers["queue_size"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "/queue"
            return@Function miniMessage.serialize(
                miniMessage.deserialize("${queue.size}").color(TextColor.fromHexString("#DB2B39"))
            )
        }

        placeholderResolvers["queue_needed"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "/queue"
            return@Function miniMessage.serialize(
                miniMessage.deserialize("${queue.meta.minPlayers}").color(TextColor.fromHexString("#DB2B39"))
            )
        }

        placeholderResolvers["top_rating"] = Function { player ->
            return@Function "0"
        }

        placeholderResolvers["bits"] = Function { player ->
            return@Function player.dataProvider.playerData?.bits?.toString() ?: "0"
        }
    }

}