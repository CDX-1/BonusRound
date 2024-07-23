package net.bonusround.game.generic

import de.tr7zw.changeme.nbtapi.NBT
import net.bonusround.api.utils.EventListener
import net.bonusround.api.utils.Registrable
import net.bonusround.api.utils.Formatter
import net.bonusround.game.configs.lang
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