package net.bonusround.game.discord

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import io.papermc.paper.event.player.AsyncChatEvent
import net.bonusround.api.utils.*
import net.bonusround.game.Main
import net.bonusround.game.configs.conf
import net.bonusround.game.configs.lang
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Instant

class DiscordChat : ListenerAdapter() {

    companion object {
        private var initialized = false
        private lateinit var webhook: WebhookClient

        fun shutdown() {
            webhook.send(
                WebhookMessageBuilder()
                    .setContent("**\uD83D\uDED1 Server stopped**")
                    .build()
            )
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.id != conf().discordChatChannel) return
        if (event.author.isBot) return
        Bukkit.getServer().onlinePlayers.forEach { player ->
            lang().general.discordMinecraftFormat.component(
                usePrefix = false,
                values = arrayOf(event.author.name, event.message.contentStripped)
            ).send()
        }
    }

    init {
        if (!initialized) {
            initialized = true
            webhook = WebhookClientBuilder(conf().discordChatWebhook)
                .setThreadFactory { job ->
                    val thread = Thread(job)
                    thread.name = "BonusRoundWebhook"
                    thread.isDaemon = true
                    return@setThreadFactory thread
                }
                .setWait(true)
                .build()

            webhook.send(
                WebhookMessageBuilder()
                    .setContent("**\uD83D\uDFE2 Server started**")
                    .build()
            )

            EventListener(AsyncChatEvent::class.java) { event ->
                webhook.send(
                    WebhookMessageBuilder()
                        .setUsername(event.player.name)
                        .setAvatarUrl("https://mc-heads.net/avatar/${event.player.name}.png/")
                        .setContent(
                            "**${event.player.name}:** ${
                                Formatter.plain().serialize(event.originalMessage()).replace("@", "\\@")
                            }"
                        )
                        .build()
                )
            }

            EventListener(PlayerJoinEvent::class.java) { event ->
                webhook.send(
                    WebhookMessageBuilder()
                        .setUsername(event.player.name)
                        .setAvatarUrl("https://mc-heads.net/avatar/${event.player.name}.png/")
                        .setContent("**${event.player.name} joined!**")
                        .build()
                )
            }

            EventListener(PlayerQuitEvent::class.java) { event ->
                webhook.send(
                    WebhookMessageBuilder()
                        .setUsername(event.player.name)
                        .setAvatarUrl("https://mc-heads.net/avatar/${event.player.name}.png/")
                        .setContent("**${event.player.name} quit!**")
                        .build()
                )
            }

            Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, {
                val freeMemory = Runtime.getRuntime().freeMemory()
                val totalMemory = Runtime.getRuntime().totalMemory()
                val usedMemory = totalMemory - freeMemory

                webhook.send(
                    WebhookEmbedBuilder()
                        .setAuthor(
                            WebhookEmbed.EmbedAuthor(
                                "Server Performance Report",
                                "https://i.ibb.co/SBZ4HtW/bonusround.png",
                                ""
                            )
                        )
                        .setColor(0xDB2B39)
                        .setDescription(
                            """
                            **RAM:** ${formatBytes(usedMemory)} / ${formatBytes(totalMemory)} (${
                                String.format(
                                    "%.2f",
                                    (usedMemory.toDouble() / totalMemory.toDouble()) * 100
                                )
                            }%)
                            **TPS:** ${String.format("%.2f", Bukkit.getServer().tps.first())}
                            **Players:** ${Bukkit.getServer().onlinePlayers.size}
                        """.trimIndent()
                        )
                        .setTimestamp(Instant.now())
                        .build()
                )
            }, 100, 20 * 60 * 10)
        }
    }

}