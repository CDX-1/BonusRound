package net.bonusround.game

import com.github.retrooper.packetevents.PacketEvents
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.clip.placeholderapi.PlaceholderAPI
import net.bonusround.api.BonusRoundAPI
import net.bonusround.api.config.ConfigLoader
import net.bonusround.api.data.DataManager
import net.bonusround.api.game.QueueManager
import net.bonusround.api.gui.GuiService
import net.bonusround.api.utils.Formatter
import net.bonusround.game.commands.DiscordCommand
import net.bonusround.game.commands.HelpCommand
import net.bonusround.game.commands.QueueCommand
import net.bonusround.game.commands.StatsCommand
import net.bonusround.game.configs.Config
import net.bonusround.game.configs.Lang
import net.bonusround.game.configs.Overrides
import net.bonusround.game.configs.lang
import net.bonusround.game.data.containers.DodgeballRatingContainer
import net.bonusround.game.data.containers.PlayerDataContainer
import net.bonusround.game.data.tables.DodgeballRatingTable
import net.bonusround.game.data.tables.PlayerDataTable
import net.bonusround.game.discord.Bot
import net.bonusround.game.discord.bot
import net.bonusround.game.discord.serverStopped
import net.bonusround.game.extensions.dataProvider
import net.bonusround.game.games.Dodgeball
import net.bonusround.game.generic.AppearanceEvents
import net.bonusround.game.generic.ItemEvents
import net.bonusround.game.gui.StatsGui
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
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

        BonusRoundAPI.Lang.matchFoundTitle = lang.games.general.matchFoundTitle.toTitle(usePrefix = false)

        // DATABASE

        DataManager(
            conf.databaseType,
            conf.sqliteFileName,
            conf.host,
            conf.user,
            conf.pass
        )
            .registerTable(PlayerDataTable, PlayerDataContainer::class)
            .registerTable(DodgeballRatingTable, DodgeballRatingContainer::class)
            .register()

        // DISCORD

        Bot().register()

        // LISTENERS

        AppearanceEvents().register()
        ItemEvents().register()

        // GAMES

        Dodgeball().register()

        // GUIs

        GuiService.registerGui(StatsGui())

        // COMMANDS

        CommandAPI.onLoad(CommandAPIBukkitConfig(this))
        CommandAPI.onEnable()

        QueueCommand().register()
        DiscordCommand().register()
        HelpCommand().register()
        StatsCommand().register()

        // PLACEHOLDER API

        registerPlaceholders()
        PAPIExtension().register()
        DataPAPI().register()

        // OVERRIDES

        overrides.commandPermissionOverrides.forEach { commandPermissionOverride ->
            val command = server.commandMap.getCommand(commandPermissionOverride)
            command?.let {
                it.permission = "override"
            } ?: run {
                logger.info("Command: $commandPermissionOverride does not exist in command map!")
            }
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

        placeholderResolvers["queue_name_bold"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "/queue"
            return@Function Formatter.legacy().serialize(
                miniMessage.deserialize(queue.formattedName).color(TextColor.fromHexString("#DB2B39")).decorate(TextDecoration.BOLD)
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
            return@Function player.dataProvider.dodgeballRatings?.let { ratings ->
                ratings[Dodgeball.Format.`1v1`]?.wins?.toString() ?: "0"
            } ?: "0"
        }

        placeholderResolvers["bits"] = Function { player ->
            return@Function player.dataProvider.playerData?.bits?.toString() ?: "0"
        }

        placeholderResolvers["in_queue"] = Function { player ->
            return@Function ((QueueManager getQueueOf player) != null).toString()
        }

        placeholderResolvers["queue_total_games"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "n/a"
            try {
                val wins = PlaceholderAPI.setPlaceholders(player, "%data_${queue.id}_wins%").toInt()
                val losses = PlaceholderAPI.setPlaceholders(player, "%data_${queue.id}_losses%").toInt()
                return@Function (wins + losses).toString()
            } catch (error: NumberFormatException) {
                return@Function "0"
            }
        }

        placeholderResolvers["queue_rating"] = Function { player ->
            val queue = (QueueManager getQueueOf player) ?: return@Function "n/a"
            val rating = PlaceholderAPI.setPlaceholders(player, "%data_${queue.id}_rating%")
            if (rating == "%data_${queue.id}_rating%") return@Function "0" else rating
        }
    }

}