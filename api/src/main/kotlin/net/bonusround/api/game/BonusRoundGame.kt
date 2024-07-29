package net.bonusround.api.game

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import net.bonusround.api.utils.*
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.function.BiConsumer
import java.util.function.Consumer
import kotlin.reflect.KClass

abstract class BonusRoundGame(val id: String, vararg queues: QueueMeta) : Registrable {
    private val queueMetas = ArrayList<QueueMeta>()
    private val gameListeners = HashMap<String, EventListener<out Event>>()
    private val abilities = HashMap<PlayerAbility, BiConsumer<PlayerAbility, Player>>()
    private val gameEvents = HashMap<String, BiConsumer<Game, GameEvent>>()
    private var autoAddToGameList = false
    val inGame = HashMap<Player, Game>()

    init {
        queueMetas.addAll(queues)
    }

    fun <T : Event> registerGameListener(id: String, clazz: KClass<T>, disposeForNonPlayers: Boolean = true, handler: Consumer<T>) {
        gameListeners[id] = EventListener(clazz.java) { event ->
            if (event is EntityEvent && disposeForNonPlayers) {
                if (!inGame.containsKey(event.entity)) return@EventListener
            }
            if (event is PlayerEvent && disposeForNonPlayers) {
                if (!inGame.containsKey(event.player)) return@EventListener
            }
            handler.accept(event)
        }
    }

    fun <T : PlayerEvent> registerGamePlayerListener(id: String, clazz: KClass<out T>, disposeForNonPlayers: Boolean = true, handler: Consumer<T>) {
        gameListeners[id] = EventListener(clazz.java) { event ->
            if (disposeForNonPlayers) {
                if (!inGame.containsKey(event.player)) return@EventListener
            }
            handler.accept(event)
        }
    }

    fun <T : EntityEvent> registerGamePlayerEntityListener(id: String, clazz: KClass<out T>, disposeForNonPlayers: Boolean = true, handler: Consumer<T>) {
        gameListeners[id] = EventListener(clazz.java) { event ->
            if (event.entity is Player && disposeForNonPlayers) {
                if (!inGame.containsKey(event.entity)) return@EventListener
            }
            handler.accept(event)
        }
    }

    fun registerPlayerAbility(ability: PlayerAbility, handler: BiConsumer<PlayerAbility, Player>): PlayerAbility {
        abilities[ability] = handler
        return ability
    }

    fun <T> registerPlayerAbilityWithEvent(ability: PlayerAbility, clazz: KClass<out T>, handler: BiConsumer<PlayerAbility, Player>): PlayerAbility where T : PlayerEvent, T : Cancellable {
        registerPlayerAbility(ability, handler)
        registerGameListener(ability.id, clazz) { event ->
            event.isCancelled = true
            callAbility(ability, event.player)
        }
        return ability
    }

    fun registerGameEvent(id: String, handler: BiConsumer<Game, GameEvent>) {
        gameEvents[id] = handler
    }

    fun callAbility(ability: PlayerAbility, player: Player) {
        inGame[player]?.callAbility(ability, player)
    }

    fun disableHunger() {
        registerGameListener("disable_hunger", FoodLevelChangeEvent::class) { event ->
            event.isCancelled = true
        }
    }

    fun autoAddToGameList(value: Boolean) {
        autoAddToGameList = value
    }

    fun reserveArena(arenaProvider: ArenaProvider): CompletableDeferred<Arena?> {
        val search = CompletableDeferred<Arena?>()
        launch {
            async {
                search.complete(
                    ArenaService.awaitArena(
                        arenaProvider,
                        seconds().toMillis(1),
                        minutes().toMillis(2)
                    )
                )
            }
        }
        return search
    }

    abstract fun onGameInit(game: Game)
    open fun onGameEvent(game: Game, event: GameEvent) {}

    override fun register() {
        registerGamePlayerListener("player_quit", PlayerQuitEvent::class) { event ->
            inGame.remove(event.player)
        }

        queueMetas.forEach { queueMeta ->
            QueueManager.registerQueue(
                QueueBuilder(
                    "${id}_${queueMeta.id}",
                    "${id.replaceFirstChar { it.uppercaseChar() }} ${queueMeta.id.replaceFirstChar { it.uppercaseChar() }}"
                )
                    .setMeta(
                        queueMeta
                    )
                    .setGameInit { game ->
                        if (autoAddToGameList) {
                            game.players.forEach { player ->
                                inGame[player] = game
                            }
                        }
                        game.onEvent { event ->
                            gameEvents[event.eventId]?.accept(game, event)
                            onGameEvent(game, event)
                        }
                        abilities.forEach { (ability, handler) ->
                            game.registerPlayerAbility(ability) { player ->
                                handler.accept(ability, player)
                            }
                        }
                        onGameInit(game)
                    }
                    .build()
            )
        }
    }
}