@file:Suppress("UNUSED")

package net.bonusround.game.utils

import kotlin.math.abs
import kotlin.random.Random

fun chance(chance: Double, task: () -> Unit) {
    if (Random.nextDouble() < chance / 100) task()
}

fun <T> weightedChance(vararg items: Pair<Double, T>): T {
    val weights = DoubleArray(items.size)
    var totalWeight = 0.0
    items.indices.forEach { i ->
        totalWeight += items[i].first
        weights[i] = totalWeight
    }

    val random = Random.nextDouble(totalWeight)

    val index = weights.binarySearch(random)
    val itemIndex = if (index >= 0) index else abs(index) - 1

    return items[itemIndex].second
}