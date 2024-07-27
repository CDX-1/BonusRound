package net.bonusround.game.games

import com.github.shynixn.mccoroutine.bukkit.launch
import de.tr7zw.changeme.nbtapi.NBT
import io.papermc.paper.event.entity.EntityMoveEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.bonusround.api.game.*
import net.bonusround.api.game.GameEvent
import net.bonusround.api.utils.*
import net.bonusround.api.utils.EventListener
import net.bonusround.game.Main
import net.bonusround.game.configs.lang
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.SoundType
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.*
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.sin

private lateinit var dodgeballArenaProvider: ArenaProvider
private val inGame = HashMap<Player, Game>()
private val dodgeball = ItemBuilder(Material.BOW)
    .displayName(
        lang().games.dodgeball.dodgeballItemName.component(usePrefix = false)
    )
    .enchant(Enchantment.INFINITY, 1)
    .unbreakable(false, hide = true)
    .droppable(false)
    .flags(ItemFlag.HIDE_ENCHANTS)

private val dodgeballCharge = ItemBuilder(Material.ARROW)
    .displayName(Component.text(""))
    .droppable(false)

private val dashItem = ItemBuilder(Material.STICK)
    .displayName(
        lang().games.dodgeball.dodgeballDashItemName.component(usePrefix = false)
    )
    .unbreakable(true)
    .droppable(false)
    .customModelData(102)

private val doubleJumpItem = ItemBuilder(Material.STRING)
    .displayName(
        lang().games.dodgeball.dodgeballDoubleJumpItemName.component(usePrefix = false)
    )
    .unbreakable(true)
    .droppable(false)
    .customModelData(101)
private val dashAbility = PlayerAbility("dash", seconds().toMillis(5))
private val doubleJumpAbility = PlayerAbility("doubleJump", seconds().toMillis(2))

private val dodgeball1v1 = Consumer<Game> { game ->
    suspendingAsync {
        val arenaSearch = CompletableDeferred<Arena?>()
        var arena: Arena? = null
        var hasEnded = false
        val countdowns = ArrayList<BukkitTask>()

        game.registerPlayerAbility(dashAbility) { player ->
            val currentVelocity = player.velocity
            val horizontalVelocity = Vector(currentVelocity.x, 0.0, currentVelocity.z)

            if (horizontalVelocity.length() > 0) {
                val direction = horizontalVelocity.normalize()
                val pushVector = direction.multiply(1.5)
                player.velocity = player.velocity.add(pushVector)
            } else {
                val yaw = Math.toRadians(player.location.yaw.toDouble())
                val lookDirection = Vector(-sin(yaw), 0.0, cos(yaw))
                val pushVector = lookDirection.multiply(1.5)
                player.velocity = player.velocity.add(pushVector)
            }
            player.setCooldown(Material.STICK, ((dashAbility.cooldown / 1000) * 20).toInt())
            player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
        }

        game.registerPlayerAbility(doubleJumpAbility) { player ->
            player.velocity = Vector(0, 1, 0)
            player.setCooldown(Material.STRING, ((doubleJumpAbility.cooldown / 1000) * 20).toInt())
            player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
        }

        game.onEvent { event ->
            when (event.eventId) {
                "playerHit" -> {
                    val attacker = event.parameters["attacker"] as Player? ?: return@onEvent
                    val hit = event.parameters["hit"] as Player? ?: return@onEvent

                    val cloneLoc = hit.location.clone()

                    attacker.sendMessage(
                        lang().games.dodgeball.attackerHit.component(values = arrayOf(hit.name))
                    )

                    hit.sendMessage(
                        lang().games.dodgeball.victimHit.component(values = arrayOf(attacker.name))
                    )

                    cloneLoc.world.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.MASTER, 1f, 1f), cloneLoc.x, cloneLoc.y, cloneLoc.z)
                    game.players.forEach { player ->
                        createBlockWave(hit.location, 4)
                        launch {
                            delay(seconds().toMillis(1))
                            repeat(40) {
                                delay(100)
                                player.playSound(
                                    Sound.sound(
                                        Key.key("entity.generic.explode"),
                                        Sound.Source.MASTER,
                                        1f,
                                        1f
                                    )
                                )
                                cloneLoc.world.spawnParticle(Particle.EXPLOSION, cloneLoc, 1, 0.0, 0.0, 0.0)
                            }
                        }
                    }

                    game.callEvent(GameEvent("end"))
                }

                "end" -> {
                    countdowns.forEach { it.cancel() }
                    countdowns.clear()
                    game.players.forEach { player ->
                        inGame.remove(player)
                        player.allowFlight = false
                        player.inventory.clear()
                        player.gameMode = GameMode.SPECTATOR
                        player.level = 0
                        player.exp = 0F
                    }

                    hasEnded = true
                    game.broadcast(lang().games.general.gameOverTitle.toTitle())

                    delayed(5000) {
                        game.release(cancelJob = true)
                    }
                }

                "release" -> {
                    countdowns.forEach { it.cancel() }
                    inGame.clear()
                    countdowns.clear()
                    arena?.release()
                    game.players.forEach { player ->
                        player.allowFlight = false
                        player.inventory.clear()
                        player.gameMode = GameMode.ADVENTURE
                    }
                }

                "disconnect" -> {
                    // val player = event.parameters["player"] as Player? ?: return@onEvent
                    game.release(cancelJob = true)
                }
            }
        }

        game.broadcast(lang().games.general.searchingForArena.component())

        Main.instance.launch {
            withContext(Dispatchers.IO) {
                arenaSearch.complete(
                    ArenaService.awaitArena(
                        dodgeballArenaProvider,
                        seconds().toMillis(1),
                        minutes().toMillis(2)
                    )
                )
            }
        }

        arena = arenaSearch.await()

        if (arena == null) {
            game.broadcast(lang().games.general.arenaSearchTimeout.component())
            game.release(cancelJob = true)
            return@suspendingAsync
        }

        arena.reserve()
        game.broadcast(
            lang().games.general.arenaFound.component(values = arrayOf(arena.id))
        )

        sync {
            val red = game.players[0]
            val blue = game.players[1]

            red.teleport(Location(arena.origin.world, arena.origin.x, arena.origin.y, arena.origin.z + 16, 180f,0f))
            blue.teleport(Location(arena.origin.world, arena.origin.x, arena.origin.y, arena.origin.z - 16))

            game.players.forEach { player ->
                player.gameMode = GameMode.ADVENTURE
                player.inventory.clear()
                dodgeball.give(player)
                dodgeballCharge.atSlot(9, player)
                dashItem.atSlot(8, player)
                doubleJumpItem.atSlot(7, player)
                countdowns.add(experienceBarCountdown(player, 90))
            }

            delayedAsync(500) {
                game.players.forEach { player ->
                    if (inGame.containsKey(player)) {
                        sync {
                            player.allowFlight = true
                        }
                    }
                }
            }

            inGame[red] = game
            inGame[blue] = game

            delayed(seconds().toMillis(90)) {
                if (hasEnded) return@delayed
                game.broadcast(lang().games.general.gameTimeout.component())
                game.callEvent(GameEvent("end"))
            }
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
            val bow = player.inventory.itemInMainHand
            if (bow.type != Material.BOW) return@EventListener
            if (player.getCooldown(Material.BOW) > 0) return@EventListener
            player.setCooldown(Material.BOW, 2 * 20)
            event.isCancelled = true
            val force = event.force
            val item = ItemStack(Material.PLAYER_HEAD)
            val stand = player.world.spawnEntity(
                player.eyeLocation.subtract(Vector(0, 3, 0)),
                EntityType.ARMOR_STAND
            ) as ArmorStand
            NBT.modifyPersistentData(stand) { nbt ->
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
            delayed(100L) {
                stand.equipment.helmet = item
            }
        }

        EventListener(EntityMoveEvent::class.java) { event ->
            if (event.entityType != EntityType.ARMOR_STAND) return@EventListener
            val entity = event.entity as ArmorStand
            if (entity.world.name != "arenas") return@EventListener
            entity.world.spawnParticle(Particle.FIREWORK, entity.location, 5, 0.0, 0.0, 0.0, 0.0)
            chance(20.0) {
                event.entity.world.playSound(
                    Sound.sound(
                        Key.key("entity.experience_orb.pickup"),
                        Sound.Source.MASTER,
                        1f,
                        1f
                    )
                )
            }
            val uuid = NBT.getPersistentData<UUID>(entity) { nbt -> nbt.getUUID("owner") } ?: return@EventListener
            val nearby = entity.location.getNearbyPlayers(1.0)
            nearby.forEach { player ->
                if (player.uniqueId == uuid) return@forEach
                if (!inGame.containsKey(player)) return@forEach
                delayed(500) {
                    val owner = Bukkit.getPlayer(uuid) ?: return@delayed
                    if (!owner.isOnline) return@delayed
                    inGame[player]?.callEvent(
                        GameEvent(
                            "playerHit",
                            hashMapOf(Pair("attacker", owner), Pair("hit", player))
                        )
                    )
                }
            }
            val belowBlockType = event.entity.world.getBlockAt(
                event.entity.location.subtract(
                    Vector(
                        0.toDouble(),
                        0.25,
                        0.toDouble()
                    )
                )
            ).type
            if (belowBlockType == Material.AIR || belowBlockType == Material.BARRIER || belowBlockType == Material.LIGHT) return@EventListener
            event.entity.world.spawnParticle(Particle.EXPLOSION, event.entity.location, 3)
            event.entity.world.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.MASTER, 1f, 1f))
            delayed(seconds().toMillis(2)) {
                event.entity.remove()
            }
        }

        EventListener(PlayerToggleFlightEvent::class.java) { event ->
            val player = event.player
            if (!inGame.containsKey(player)) return@EventListener
            event.isCancelled = true
            inGame[player]?.callAbility(doubleJumpAbility, event.player)
        }

        EventListener(PlayerSwapHandItemsEvent::class.java) { event ->
            val player = event.player
            if (!inGame.containsKey(player)) return@EventListener
            event.isCancelled = true
            inGame[player]?.callAbility(dashAbility, event.player)
        }

        EventListener(FoodLevelChangeEvent::class.java) { event ->
            if (event.entity !is Player) return@EventListener
            val player = event.entity as Player
            if (!inGame.containsKey(player)) return@EventListener
            event.isCancelled = true
        }

        // 1v1s

        QueueManager.registerQueue(
            QueueBuilder(
                "dodgeball_1v1",
                "Dodgeball 1v1"
            )
                .setMeta(
                    QueueMeta(
                        2,
                        2
                    )
                )
                .setGameInit(dodgeball1v1)
                .build()
        )

    }

    @Suppress("UNUSED")
    enum class Format(val format: String) {
        `1v1`("1v1"),
        `2v2`("2v2")
    }
}