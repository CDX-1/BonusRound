package net.cdx.bonusround.utils

import net.cdx.bonusround.Main
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class Animations {
    companion object {

        fun experienceBarCountdown(player: Player, time: Int): BukkitTask {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + time * 1000

            val task = object : BukkitRunnable() {
                override fun run() {
                    val currentTime = System.currentTimeMillis()
                    val timeLeftMillis = endTime - currentTime

                    if (timeLeftMillis <= 0) {
                        player.level = 0
                        player.exp = 0f
                        cancel()
                    } else {
                        val timeLeftSeconds = (timeLeftMillis / 1000).toInt()
                        val exp = (timeLeftMillis % 1000) / 1000.0f

                        player.level = timeLeftSeconds
                        player.exp = exp
                    }
                }
            }

            return task.runTaskTimer(Main.instance, 0L, 1L)
        }

    }
}