package net.bonusround.game.games

import de.tr7zw.changeme.nbtapi.NBT
import io.papermc.paper.event.entity.EntityMoveEvent
import kotlinx.coroutines.delay
import net.bonusround.api.game.*
import net.bonusround.api.game.GameEvent
import net.bonusround.api.utils.*
import net.bonusround.game.Main
import net.bonusround.game.configs.lang
import net.bonusround.game.extensions.dataProvider
import net.bonusround.game.extensions.safeSub
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
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin

@Suppress("UNCHECKED_CAST")
class Dodgeball : BonusRoundGame("dodgeball", QueueMeta("1v1", 2, 2)) {
    private val arenaProvider = ArenaService.createProvider()
        .addArena("test", Location(Bukkit.getWorld("arenas"), 0.5, 65.0, 0.5))

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

    override fun onGameInit(game: Game) {
        suspendingAsync {
            game.properties["hasEnded"] = false
            game.properties["countdowns"] = ArrayList<BukkitTask>()

            game.broadcast(lang().games.general.searchingForArena.component())
            val arena = reserveArena(arenaProvider).await()

            if (arena == null) {
                game.broadcast(lang().games.general.arenaSearchTimeout.component())
                game.release(cancelJob = true)
                return@suspendingAsync
            }

            arena.reserve()
            game.properties["arena"] = arena
            game.broadcast(
                lang().games.general.arenaFound.component(values = arrayOf(arena.id))
            )

            sync {
                val red = game.players[0]
                val blue = game.players[1]

                red.teleport(Location(arena.origin.world, arena.origin.x, arena.origin.y, arena.origin.z + 16, 180f, 0f))
                blue.teleport(Location(arena.origin.world, arena.origin.x, arena.origin.y, arena.origin.z - 16))

                game.players.forEach { player ->
                    player.gameMode = GameMode.ADVENTURE
                    player.inventory.clear()
                    dodgeball.give(player)
                    dodgeballCharge.atSlot(9, player)
                    dashItem.atSlot(8, player)
                    doubleJumpItem.atSlot(7, player)
                    (game.properties["countdowns"] as ArrayList<BukkitTask>).add(experienceBarCountdown(player, 90))
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
                    if (game.properties["hasEnded"] as Boolean) return@delayed
                    game.broadcast(lang().games.general.gameTimeout.component())
                    game.callEvent(GameEvent("end"))
                }
            }
        }
    }

    override fun register() {
        disableHunger()
        autoAddToGameList(true)

        registerGameEvent("player_hit") { game, event ->
            val attacker = event.parameters["attacker"] as Player? ?: return@registerGameEvent
            val hit = event.parameters["hit"] as Player? ?: return@registerGameEvent

            hit.playHurtAnimation(0f)
            hit.world.strikeLightningEffect(hit.location)

            val cloneLoc = hit.location.clone()

            attacker.sendMessage(
                lang().games.dodgeball.attackerHit.component(values = arrayOf(hit.name))
            )

            hit.sendMessage(
                lang().games.dodgeball.victimHit.component(values = arrayOf(attacker.name))
            )

            attacker.dataProvider.dodgeballRatings?.get(Format.`1v1`)?.let { _ ->
                attacker.dataProvider.dodgeballRatings!![Format.`1v1`]!!.wins += 1
                attacker.dataProvider.dodgeballRatings!![Format.`1v1`]!!.rating += 3
            }
            var oldHitRating = 0
            hit.dataProvider.dodgeballRatings?.get(Format.`1v1`)?.let { _ ->
                hit.dataProvider.dodgeballRatings!![Format.`1v1`]!!.losses += 1
                oldHitRating = hit.dataProvider.dodgeballRatings!![Format.`1v1`]!!.rating
                hit.dataProvider.dodgeballRatings!![Format.`1v1`]!!.rating = oldHitRating safeSub 3
            }
            delayedAsync(5000) {
                if (attacker.isOnline) {
                    lang().games.general.ratingGain.toTitle(
                        usePrefix = false,
                        values = arrayOf("Dodgeball 1v1", "3")
                    ) send attacker
                    attacker.playSound(
                        Sound.sound(
                            Key.key("entity.player.levelup"),
                            Sound.Source.MASTER,
                            1f,
                            1f
                        )
                    )
                }
                if (hit.isOnline) {
                    if (oldHitRating - 3 < 0) {
                        lang().games.general.ratingUnchanged.toTitle(
                            usePrefix = false,
                            values = arrayOf("Dodgeball 1v1")
                        ) send hit
                    } else {
                        lang().games.general.ratingLoss.toTitle(
                            usePrefix = false,
                            values = arrayOf("Dodgeball 1v1", "3")
                        ) send hit
                    }
                    hit.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.MASTER, 1f, 1f))
                }
            }

            createBlockWave(hit.location, 4)
            launch {
                delay(seconds().toMillis(1))
                repeat(40) {
                    delay(75)
                    cloneLoc.world.playSound(
                        Sound.sound(
                            Key.key("entity.generic.explode"),
                            Sound.Source.MASTER,
                            10f,
                            1f
                        ), cloneLoc.x, cloneLoc.y, cloneLoc.z
                    )
                    cloneLoc.world.spawnParticle(Particle.EXPLOSION, cloneLoc, 1, 4.0, 2.0, 4.0)
                }
            }

            game.callEvent(GameEvent("end"))
        }

        registerGameEvent("end") { game, _ ->
            (game.properties["countdowns"] as ArrayList<BukkitTask>).forEach { it.cancel() }
            (game.properties["countdowns"] as ArrayList<BukkitTask>).clear()
            game.players.forEach { player ->
                inGame.remove(player)
                player.allowFlight = false
                player.inventory.clear()
                player.level = 0
                player.exp = 0F
            }
            delayedAsync(seconds().toMillis(1)) {
                game.players.forEach { player ->
                    player.gameMode = GameMode.SPECTATOR
                }
            }

            game.properties["hasEnded"] = true
            game.broadcast(lang().games.general.gameOverTitle.toTitle())

            delayed(5000) {
                game.release(cancelJob = true)
            }
        }

        registerGameEvent("release") { game, _ ->
            (game.properties["countdowns"] as ArrayList<BukkitTask>).forEach { it.cancel() }
            inGame.clear()
            (game.properties["countdowns"] as ArrayList<BukkitTask>).clear()
            (game.properties["arena"] as? Arena)?.release()
            game.players.forEach { player ->
                player.allowFlight = false
                player.inventory.clear()
                player.gameMode = GameMode.ADVENTURE
            }
        }

        registerGameEvent("disconnect") { game, event ->
            val player = event.parameters["player"] as Player? ?: return@registerGameEvent
            game.release(cancelJob = true)
        }

        registerGamePlayerEntityListener("bow_shoot", EntityShootBowEvent::class) { event ->
            val player = event.entity as Player
            val bow = player.inventory.itemInMainHand
            if (bow.type != Material.BOW) return@registerGamePlayerEntityListener
            if (player.getCooldown(Material.BOW) > 0) return@registerGamePlayerEntityListener
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

        registerGameListener("dodgeball_move", EntityMoveEvent::class, disposeForNonPlayers = false) { event ->
            if (event.entityType != EntityType.ARMOR_STAND) return@registerGameListener
            val entity = event.entity as ArmorStand
            if (entity.world.name != "arenas") return@registerGameListener
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
            val uuid = NBT.getPersistentData<UUID>(entity) { nbt -> nbt.getUUID("owner") } ?: return@registerGameListener
            val nearby = entity.location.getNearbyPlayers(1.0)
            nearby.forEach { player ->
                if (player.uniqueId == uuid) return@forEach
                if (!inGame.containsKey(player)) return@forEach
                delayed(500) {
                    val owner = Bukkit.getPlayer(uuid) ?: return@delayed
                    if (!owner.isOnline) return@delayed
                    inGame[player]?.callEvent(
                        GameEvent(
                            "player_hit",
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
            if (belowBlockType == Material.AIR || belowBlockType == Material.BARRIER || belowBlockType == Material.LIGHT) return@registerGameListener
            event.entity.world.spawnParticle(Particle.EXPLOSION, event.entity.location, 3)
            event.entity.world.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.MASTER, 1f, 1f))
            delayed(seconds().toMillis(2)) {
                event.entity.remove()
            }
        }

        registerPlayerAbilityWithEvent(PlayerAbility("double_jump", seconds().toMillis(2)), PlayerToggleFlightEvent::class) { ability, player ->
            player.velocity = Vector(0, 1, 0)
            player.setCooldown(Material.STRING, ((ability.cooldown / 1000) * 20).toInt())
            Main.logger.info("cd ${ability.cooldown} ${player.getCooldown(Material.STRING)}")
            player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
        }

        registerPlayerAbilityWithEvent(PlayerAbility("dash", seconds().toMillis(5)), PlayerSwapHandItemsEvent::class) { ability, player ->
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
            player.setCooldown(Material.STICK, ((ability.cooldown / 1000) * 20).toInt())
            player.playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 2f, 1f))
        }

        super.register()
    }

    @Suppress("UNUSED")
    enum class Format(val format: String) {
        `1v1`("1v1"),
        `2v2`("2v2")
    }
}