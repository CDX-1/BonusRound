package net.cdx.bonusround

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import me.clip.placeholderapi.PlaceholderAPI
import net.cdx.bonusround.config.lang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

// FORMATTING

private val miniMessageSerializer = MiniMessage.miniMessage()
private val legacySerializer = LegacyComponentSerializer.legacyAmpersand()
private val gsonSerializer = GsonComponentSerializer.gson()

@Suppress("MemberVisibilityCanBePrivate")
class Formatter(private var message: String) {

    private var usePrefix = true
    private var usePAPI = true
    private var papiPlayer: Player? = null
    private val placeholders = ArrayList<String>()

    fun usePrefix(value: Boolean): Formatter {
        usePrefix = value
        return this
    }

    fun usePAPI(value: Boolean, player: Player): Formatter {
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
            "${lang().general.prefix} $processedText"
        } else {
            processedText
        }

        return finalText
    }

    fun component(): Component {
        return miniMessageSerializer.deserialize(raw())
    }

    fun legacy(): String {
        return legacySerializer.serialize(component())
    }

    fun gson(): String {
        return gsonSerializer.serialize(component())
    }

}

// TIME CONVERSIONS

fun millis(): TimeUnit {
    return TimeUnit.MILLISECONDS
}

fun seconds(): TimeUnit {
    return TimeUnit.SECONDS
}

fun minutes(): TimeUnit {
    return TimeUnit.MINUTES
}

fun hours(): TimeUnit {
    return TimeUnit.HOURS
}

fun days(): TimeUnit {
    return TimeUnit.DAYS
}

// QUICK TASKS

fun sync(task: () -> Unit): Job {
    return Main.instance.launch {
        task()
    }
}

fun suspendingSync(task: suspend () -> Unit): Job {
    return Main.instance.launch {
        task()
    }
}

fun async(task: () -> Unit): Job {
    return Main.instance.launch {
        withContext(Dispatchers.IO) {
            task()
        }
    }
}

fun suspendingAsync(task: suspend () -> Unit): Job {
    return Main.instance.launch {
        withContext(Dispatchers.IO) {
            task()
        }
    }
}

fun delay(delayTime: Long, task: () -> Unit): Job {
    return Main.instance.launch {
        kotlinx.coroutines.delay(delayTime)
        task()
    }
}

fun delayAsync(delayTime: Long, task: () -> Unit): Job {
    return Main.instance.launch {
        withContext(Dispatchers.IO) {
            kotlinx.coroutines.delay(delayTime)
            task()
        }
    }
}