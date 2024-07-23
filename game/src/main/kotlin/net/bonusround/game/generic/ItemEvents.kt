package net.bonusround.game.generic

import de.tr7zw.changeme.nbtapi.NBT
import net.bonusround.game.EventListener
import net.bonusround.game.utils.Registrable
import net.bonusround.game.config.lang
import net.bonusround.game.utils.Formatter
import org.bukkit.event.player.PlayerDropItemEvent

class ItemEvents : Registrable {

    override fun register() {

        EventListener(PlayerDropItemEvent::class.java) { event ->
            NBT.get(event.itemDrop.itemStack) { nbt ->
                event.isCancelled = !nbt.getOrDefault("droppable", true)
                if (event.isCancelled) {
                    event.player.sendMessage(Formatter(lang().general.undroppableItem).component())
                }
            }
        }

    }

}