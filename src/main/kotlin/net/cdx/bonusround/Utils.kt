package net.cdx.bonusround

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

private val miniMessage = MiniMessage.miniMessage()
fun format(text: String, usePrefix: Boolean = true, vararg placeholders: String): Component {
    var message = text
    var count = 0
    placeholders.forEach {
        count++
        message = message.replace("%$count", it)
    }
    return if (usePrefix) {
        miniMessage.deserialize("${Lang.General.prefix} $message")
    } else {
        miniMessage.deserialize(message)
    }
}

fun sync(task: suspend () -> Unit): Job {
    return Main.instance.launch {
        task()
    }
}

fun async(task: suspend () -> Unit): Job {
    return Main.instance.launch {
        withContext(Dispatchers.IO) {
            task()
        }
    }
}