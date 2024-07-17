package net.cdx.bonusround.generic

import de.tr7zw.changeme.nbtapi.NBT
import net.cdx.bonusround.EventListener
import net.cdx.bonusround.Registrable
import net.cdx.bonusround.config.lang
import net.cdx.bonusround.utils.Formatter
import org.bukkit.event.player.PlayerDropItemEvent

class ItemEvents : Registrable {

    override fun register() {

        EventListener(PlayerDropItemEvent::class.java) { event ->
            NBT.get(event.itemDrop.itemStack) { nbt ->
                event.isCancelled = nbt.getOrDefault("droppable", true)
                event.player.sendMessage(Formatter(lang().general.undroppableItem).component())
            }
        }

    }

}