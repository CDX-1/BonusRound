@file:Suppress("UNUSED")

package net.bonusround.game.utils

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import net.bonusround.game.Main
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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

suspend fun <T> asyncTransaction(block: suspend () -> T): T =
    withContext(Dispatchers.IO) {
        newSuspendedTransaction { block() }
    }