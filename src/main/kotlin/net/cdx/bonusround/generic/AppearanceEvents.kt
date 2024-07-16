package net.cdx.bonusround.generic

import net.cdx.bonusround.EventListener
import net.cdx.bonusround.Formatter
import net.cdx.bonusround.Registrable
import net.cdx.bonusround.config.lang
import org.bukkit.attribute.Attribute
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class AppearanceEvents : Registrable {

    override fun register() {

        // CHAT EVENTS

        EventListener(PlayerJoinEvent::class.java) { event ->
            if (event.player.hasPlayedBefore()) {
                event.joinMessage(Formatter(lang().general.join)
                    .usePrefix(false)
                    .usePAPI(true, event.player)
                    .component())
            } else {
                event.joinMessage(Formatter(lang().general.joinUnique)
                    .usePrefix(false)
                    .usePAPI(true, event.player)
                    .component())
            }
        }

        EventListener(PlayerQuitEvent::class.java) { event ->
            event.quitMessage(Formatter(lang().general.quit)
                .usePrefix(false)
                .usePAPI(true, event.player)
                .component())
        }

        // LOBBY EVENTS

        EventListener(PlayerChangedWorldEvent::class.java) { event ->
            val player = event.player
            if (player.world.name != "world") return@EventListener
            player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
            player.foodLevel = 20
        }

        EventListener(FoodLevelChangeEvent::class.java) { event ->
            if (event.entity.world.name == "world") event.isCancelled = true
        }

    }

}