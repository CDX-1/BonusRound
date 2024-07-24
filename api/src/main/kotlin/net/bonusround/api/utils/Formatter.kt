@file:Suppress("UNUSED", "MemberVisibilityCanBePrivate")

package net.bonusround.api.utils

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private val miniMessageSerializer = MiniMessage.miniMessage()
private val legacySerializer = LegacyComponentSerializer.legacyAmpersand()
private val gsonSerializer = GsonComponentSerializer.gson()
private val plainSerializer = PlainTextComponentSerializer.plainText()

// FORMATTER UTILITY

class Formatter(private var message: String = "") : Cloneable {

    companion object {
        lateinit var prefix: String

        fun minimessage(): MiniMessage {
            return miniMessageSerializer
        }

        fun legacy(): LegacyComponentSerializer {
            return legacySerializer
        }

        fun gson(): GsonComponentSerializer {
            return gsonSerializer
        }

        fun plain(): PlainTextComponentSerializer {
            return plainSerializer
        }

        fun title(formatTemplate: Formatter, title: net.bonusround.api.config.serializers.Title): Title {
            val formatter1 = formatTemplate.clone()
            val formatter2 = formatTemplate.clone()
            formatter1.message = title.header
            formatter2.message = title.subtext
            return Title.title(formatter1.component(), formatter2.component())
        }
    }

    private var usePrefix = true
    private var usePAPI = true
    private var papiPlayer: Player? = null
    private val placeholders = ArrayList<String>()

    fun usePrefix(value: Boolean): Formatter {
        usePrefix = value
        return this
    }

    fun usePAPI(value: Boolean, player: Player?): Formatter {
        usePAPI = value
        papiPlayer = player
        return this
    }

    fun placeholders(vararg values: String): Formatter {
        placeholders.addAll(values)
        return this
    }


    fun raw(): String {
        val processedText = placeholders.foldIndexed(
            if (usePAPI && papiPlayer != null) {
                PlaceholderAPI.setPlaceholders(papiPlayer, message)
            } else {
                message
            }
        ) { index, acc, placeholder ->
            acc.replace("%$index", placeholder)
        }

        val finalText = if (usePrefix) {
            "$prefix $processedText"
        } else {
            processedText
        }

        return finalText
    }

    fun component(): Component {
        return miniMessageSerializer.deserialize(raw())
    }

    fun componentToLegacy(): String {
        return legacySerializer.serialize(component())
    }

    fun legacyToComponent(): Component {
        return legacySerializer.deserialize(raw())
    }

    fun componentToGson(): String {
        return gsonSerializer.serialize(component())
    }

    fun gsonToComponent(): Component {
        return gsonSerializer.deserialize(raw())
    }

    override fun clone(): Formatter {
        return Formatter(message)
            .usePrefix(this.usePrefix)
            .usePAPI(this.usePAPI, this.papiPlayer)
            .placeholders(*this.placeholders.toTypedArray())
    }

}

// FORMATTER EXTENSION FUNCTIONS

fun String.component(
    usePrefix: Boolean = true,
    usePAPI: Boolean = false,
    papiPlayer: Player? = null,
    vararg values: String,
): Component {
    val formatter = Formatter(this)
        .usePrefix(usePrefix)
        .placeholders(*values)

    if (usePAPI && papiPlayer != null) {
        formatter
            .usePAPI(true, papiPlayer)
    }

    return formatter.component()
}

fun String.fromLegacy(
    usePrefix: Boolean = true,
    usePAPI: Boolean = false,
    papiPlayer: Player? = null,
    vararg values: String,
): Component {
    val formatter = Formatter(this)
        .usePrefix(usePrefix)
        .placeholders(*values)

    if (usePAPI && papiPlayer != null) {
        formatter
            .usePAPI(true, papiPlayer)
    }

    return formatter.legacyToComponent()
}

fun String.fromGson(
    usePrefix: Boolean = true,
    usePAPI: Boolean = false,
    papiPlayer: Player? = null,
    vararg values: String,
): Component {
    val formatter = Formatter(this)
        .usePrefix(usePrefix)
        .placeholders(*values)

    if (usePAPI && papiPlayer != null) {
        formatter
            .usePAPI(true, papiPlayer)
    }

    return formatter.gsonToComponent()
}

fun Component.toMiniMessage(): String {
    return miniMessageSerializer.serialize(this)
}

fun Component.toLegacy(): String {
    return legacySerializer.serialize(this)
}

fun Component.toGson(): String {
    return gsonSerializer.serialize(this)
}

fun Component.toPlain(): String {
    return plainSerializer.serialize(this)
}

infix fun String.send(player: Player) {
    player.sendMessage(this)
}

fun String.send(vararg players: Player) {
    players.forEach { player ->
        player.sendMessage(this)
    }
}

fun String.send(player: Player? = null) {
    player?.let {
        player.sendMessage(this)
    } ?: run {
        this.send(*Bukkit.getServer().onlinePlayers.toTypedArray())
    }
}

infix fun String.sendComponent(player: Player) {
    this.component().send(player)
}

fun String.sendComponent(vararg players: Player) {
    this.component().send(*players)
}

fun String.sendComponent(player: Player? = null) {
    this.component().send(player)
}

infix fun Component.send(player: Player) {
    player.sendMessage(this)
}

fun Component.send(vararg players: Player) {
    players.forEach { player ->
        player.sendMessage(this)
    }
}

fun Component.send(player: Player? = null) {
    player?.let {
        player.sendMessage(this)
    } ?: run {
        this.send(*Bukkit.getServer().onlinePlayers.toTypedArray())
    }
}