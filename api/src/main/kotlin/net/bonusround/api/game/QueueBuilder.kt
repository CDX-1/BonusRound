package net.bonusround.api.game

import org.bukkit.entity.Player
import java.util.function.Consumer
import java.util.function.Function

class QueueBuilder(private val id: String, private val formattedName: String) {

    private lateinit var meta: QueueMeta
    private lateinit var startGame: Consumer<Game>
    private var matchmaker: Function<ArrayList<Player>, ArrayList<Game>>? = null

    fun setMeta(meta: QueueMeta): QueueBuilder {
        this.meta = meta
        return this
    }

    fun setGameInit(gameInit: Consumer<Game>): QueueBuilder {
        this.startGame = gameInit
        return this
    }

    fun setMatchmaker(matchmaker: Function<ArrayList<Player>, ArrayList<Game>>?): QueueBuilder {
        this.matchmaker = matchmaker
        return this
    }

    fun build(): Queue {
        return Queue(id, formattedName, meta, startGame, matchmaker)
    }

}