package net.cdx.bonusround.games

import com.github.shynixn.mccoroutine.bukkit.launch
import de.tr7zw.changeme.nbtapi.NBT
import io.papermc.paper.event.entity.EntityMoveEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.cdx.bonusround.EventListener
import net.cdx.bonusround.Main
import net.cdx.bonusround.Registrable
import net.cdx.bonusround.config.lang
import net.cdx.bonusround.games.api.*
import net.cdx.bonusround.utils.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.UUID
import java.util.function.Consumer

private lateinit var dodgeballArenaProvider: ArenaProvider
private val inGame = HashMap<Player, Game>()
private val dodgeball = ItemBuilder(Material.BOW)
    .displayName(Formatter(lang().games.dodgeball.dodgeballItemName)
        .usePrefix(false)
        .component())
    .enchant(Enchantment.INFINITY, 1)
    .unbreakable(false, hide = true)
    .droppable(false)
    .flags(ItemFlag.HIDE_ENCHANTS)
private val dodgeballCharge = ItemBuilder(Material.ARROW)
    .displayName(Component.text(""))
    .droppable(false)
    .count(64)

private val dodgeball1v1 = Consumer<Game> { game ->
    suspendingAsync {

        val arenaSearch = CompletableDeferred<Arena?>()
        var arena: Arena? = null

        game.onPlayerLost { player ->
            inGame.remove(player)
            player.allowFlight = false
            player.inventory.clear()
            game.release(cancelJob = true)
            arena?.release()
        }

        game.onEvent { parameters ->
            val playerHit: Player = parameters["playerHit"] as Player? ?: return@onEvent
            game.players.forEach { player ->
                inGame.remove(player)
                player.allowFlight = false
                player.inventory.clear()
            }
            game.broadcast("${playerHit.name} got hit! game over.")
            game.release()
            arena?.release()
        }

        game.broadcast("Waiting for available arena...")

        Main.instance.launch {
            withContext(Dispatchers.IO) {
                arenaSearch.complete(ArenaService.awaitArena(dodgeballArenaProvider, seconds().toMillis(1), minutes().toMillis(2)))
            }
        }

        arena = arenaSearch.await()

        if (arena == null) {
            game.broadcast("Failed to find available arena in reasonable time")
            game.release(cancelJob = true)
            return@suspendingAsync
        }

        arena.reserve()
        game.broadcast("Arena found: ${arena.id}")

        sync {
            val red = game.players[0]
            val blue = game.players[1]

            red.teleport(Location(arena.origin.world, arena.origin.x, arena.origin.y, arena.origin.z + 16))
            blue.teleport(Location(arena.origin.world, arena.origin.x, arena.origin.y, arena.origin.z - 16))

            game.players.forEach { player ->
                player.gameMode = GameMode.ADVENTURE
                player.inventory.clear()
                player.allowFlight = true
                dodgeball.give(player)
                dodgeballCharge.atSlot(9, player)
            }

            inGame[red] = game
            inGame[blue] = game

            game.broadcast("Automatically releasing in 10 seconds for testing")
        }
    }
}

class Dodgeball : Registrable {
    override fun register() {

        // Arenas

        dodgeballArenaProvider = ArenaService.createProvider()
            .addArena("test", Location(Bukkit.getWorld("arenas"), 0.5, 65.0, 0.5))

        // Game Mechanics

        EventListener(EntityShootBowEvent::class.java) { event ->
            if (event.entity !is Player) return@EventListener
            val player = event.entity as Player
            if (!inGame.containsKey(player)) return@EventListener
            event.isCancelled = true
            val force = event.force
            val item = ItemStack(Material.PLAYER_HEAD)
            val stand = player.world.spawnEntity(player.eyeLocation.subtract(Vector(0, 3, 0)), EntityType.ARMOR_STAND) as ArmorStand
            NBT.modify(stand) { nbt ->
                nbt.setUUID("owner", player.uniqueId)
            }
            stand.isVisible = false
            stand.isSmall = true
            stand.setBasePlate(false)
            stand.isInvulnerable = true
            stand.equipment.helmet = item
            stand.setNoPhysics(true)
            stand.teleport(player.eyeLocation.subtract(Vector(0, 1, 0)))
            stand.velocity = player.eyeLocation.direction
                .normalize()
                .multiply(force)
            delay(100L) {
                stand.equipment.helmet = item
            }
        }

        EventListener(EntityMoveEvent::class.java) { event ->
            if (event.entityType != EntityType.ARMOR_STAND) return@EventListener
            val entity = event.entity as ArmorStand
            if (entity.world.name != "arenas") return@EventListener
            NBT.get(entity) { nbt ->
                val uuid = nbt.getUUID("owner") ?: return@get
                val nearby = entity.location.getNearbyPlayers(1.0)
                nearby.forEach { player ->
                    if (player.uniqueId == uuid) return@forEach
                    if (!inGame.containsKey(player)) return@forEach
                    delay(500) {
                        inGame[player]?.callEvent(hashMapOf(Pair("playerHit", player)))
                    }
                }
                val belowBlockType = event.entity.world.getBlockAt(event.entity.location.subtract(Vector(0.toDouble(), 0.25, 0.toDouble()))).type
                if (belowBlockType == Material.AIR || belowBlockType == Material.BARRIER || belowBlockType == Material.LIGHT) return@get
                event.entity.world.spawnParticle(Particle.EXPLOSION, event.entity.location, 3)
                event.entity.world.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.MASTER, 1f, 1f))
                delay(seconds().toMillis(2)) {
                    event.entity.remove()
                }
            }
        }

        EventListener(PlayerToggleFlightEvent::class.java) { event ->
            val player = event.player
            if (!inGame.containsKey(player)) return@EventListener
            event.isCancelled = true
            player.velocity = Vector(0, 1, 0)
            player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
        }

        EventListener(PlayerSwapHandItemsEvent::class.java) { event ->
            val player = event.player
            if (!inGame.containsKey(player)) return@EventListener
            event.isCancelled = true
            player.velocity = player.eyeLocation.direction.multiply(Vector(2, 0, 2)).add(Vector(0, 1, 0))
            player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
        }

        // 1v1s

        QueueManager.registerQueue(
            QueueBuilder(
                "dodgeball_1v1",
                "Dodgeball 1v1"
            )
                .setMeta(QueueMeta(
                    2,
                    2
                ))
                .setGameInit(dodgeball1v1)
                .build()
        )

    }
}