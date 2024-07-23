@file:Suppress("UNUSED")

package net.bonusround.api.utils

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.*
import net.bonusround.api.BonusRoundAPI
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun sync(task: () -> Unit): Job {
    return BonusRoundAPI.main.launch {
        task()
    }
}

fun suspendingSync(task: suspend () -> Unit): Job {
    return BonusRoundAPI.main.launch {
        task()
    }
}

fun async(task: () -> Unit): Job {
    return BonusRoundAPI.main.launch {
        withContext(Dispatchers.IO) {
            task()
        }
    }
}

fun suspendingAsync(task: suspend () -> Unit): Job {
    return BonusRoundAPI.main.launch {
        withContext(Dispatchers.IO) {
            task()
        }
    }
}

fun delayed(delayTime: Long, task: () -> Unit): Job {
    return BonusRoundAPI.main.launch {
        delay(delayTime)
        task()
    }
}

fun delayedAsync(delayTime: Long, task: () -> Unit): Job {
    return BonusRoundAPI.main.launch {
        withContext(Dispatchers.IO) {
            delay(delayTime)
            task()
        }
    }
}

suspend fun <T> asyncTransaction(block: suspend () -> T): T =
    withContext(Dispatchers.IO) {
        newSuspendedTransaction { block() }
    }

fun launch(task: suspend CoroutineScope.() -> Unit) {
    BonusRoundAPI.main.launch {
        task()
    }
}