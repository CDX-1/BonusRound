package net.cdx.bonusround.games

import net.cdx.bonusround.Registrable
import net.cdx.bonusround.games.api.QueueBuilder
import net.cdx.bonusround.games.api.QueueManager
import net.cdx.bonusround.games.api.QueueMeta

class Dodgeball : Registrable {
    override fun register() {

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
                .setGameInit { game ->

                }
                .build()
        )

    }
}