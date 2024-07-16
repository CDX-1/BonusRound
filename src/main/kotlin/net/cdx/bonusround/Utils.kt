package net.cdx.bonusround

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.concurrent.TimeUnit

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