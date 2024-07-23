@file:Suppress("UNUSED")

package net.bonusround.api.utils

import java.util.concurrent.TimeUnit

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

private const val TICKS_PER_SECOND = 20

fun TimeUnit.toTicks(duration: Long): Long {
    return when (this) {
        TimeUnit.NANOSECONDS -> (duration * TICKS_PER_SECOND / 1_000_000_000.0).toLong()
        TimeUnit.MICROSECONDS -> (duration * TICKS_PER_SECOND / 1_000_000.0).toLong()
        TimeUnit.MILLISECONDS -> (duration * TICKS_PER_SECOND / 1_000.0).toLong()
        TimeUnit.SECONDS -> duration * TICKS_PER_SECOND
        TimeUnit.MINUTES -> duration * TICKS_PER_SECOND * 60
        TimeUnit.HOURS -> duration * TICKS_PER_SECOND * 3600
        TimeUnit.DAYS -> duration * TICKS_PER_SECOND * 86400
        else -> throw IllegalArgumentException("Unsupported TimeUnit: $this")
    }
}

fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024
    val mb = kb / 1024
    val gb = mb / 1024
    return when {
        gb > 0 -> "$gb GB"
        mb > 0 -> "$mb MB"
        kb > 0 -> "$kb KB"
        else -> "$bytes B"
    }
}