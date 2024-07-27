package net.bonusround.game.gui

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import net.bonusround.api.gui.*
import net.bonusround.api.utils.ItemBuilder
import net.bonusround.api.utils.component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

class StatsGui : DynamicGui("stats") {
    override fun createInventory(player: Player): ChestGui {
        return ChestGui(3, ComponentHolder.of("<dark_gray>Stats".component(usePrefix = false)))
            .isInterface(true)
            .pane(GuiConstants.WHITESPACE_BACKGROUND_3_ROW)
            .pane(
                ItemBuilder(Material.PLAYER_HEAD)
                    .displayName("<red><b>Dodgeball".component(usePrefix = false).decoration(TextDecoration.ITALIC, false))
                    .flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                    .toStaticPane(4, 1, 1, 1, 0, 0)
            )

    }
}