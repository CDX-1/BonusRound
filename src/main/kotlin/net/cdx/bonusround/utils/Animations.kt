package net.cdx.bonusround.utils

import net.cdx.bonusround.Main
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.*

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

        fun createBlockWave(origin: Location, radius: Int) {
            val random = Random()
            val fallingBlocks = mutableSetOf<FallingBlock>()
            val dontAddList = mutableSetOf(
                Material.AIR,
                Material.PLAYER_HEAD,
                Material.CREEPER_HEAD,
                Material.BEDROCK,
                Material.BARRIER
            )

            val surroundingBlocks = mutableListOf<Block>()
            for (x in -radius..radius) {
                for (y in -radius..radius) {
                    for (z in -radius..radius) {
                        val loc = origin.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                        if (loc != origin && !dontAddList.contains(loc.block.type)) {
                            surroundingBlocks.add(loc.block)
                        }
                    }
                }
            }

            object : BukkitRunnable() {
                override fun run() {
                    for (block in surroundingBlocks) {
                        val loc = block.location
                        val fallingBlock = loc.world.spawn(loc, FallingBlock::class.java) { falling ->
                            falling.blockData = block.blockData
                            falling.dropItem = false
                        }

                        val velocity = Vector(
                            (random.nextDouble() - 0.5) * 1.5,
                            random.nextDouble() * 1.5,
                            (random.nextDouble() - 0.5) * 1.5
                        )
                        fallingBlock.velocity = velocity
                        fallingBlocks.add(fallingBlock)
                    }
                }
            }.runTask(Main.instance)

            object : BukkitRunnable() {
                override fun run() {
                    fallingBlocks.forEach { it.remove() }
                }
            }.runTaskLater(Main.instance, 100L)
        }

    }
}