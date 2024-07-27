package net.bonusround.api.gui

import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.Pane
import net.bonusround.api.utils.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag

@Suppress("UNUSED", "MemberVisibilityCanBePrivate")
object GuiConstants {
    val WHITESPACE_BACKGROUND_1_ROW = OutlinePane(0, 0, 9, 1, Pane.Priority.LOWEST)
    val WHITESPACE_BACKGROUND_2_ROW = OutlinePane(0, 0, 9, 2, Pane.Priority.LOWEST)
    val WHITESPACE_BACKGROUND_3_ROW = OutlinePane(0, 0, 9, 3, Pane.Priority.LOWEST)
    val WHITESPACE_BACKGROUND_4_ROW = OutlinePane(0, 0, 9, 4, Pane.Priority.LOWEST)
    val WHITESPACE_BACKGROUND_5_ROW = OutlinePane(0, 0, 9, 5, Pane.Priority.LOWEST)
    val WHITESPACE_BACKGROUND_6_ROW = OutlinePane(0, 0, 9, 6, Pane.Priority.LOWEST)

    init {
        arrayOf(
            WHITESPACE_BACKGROUND_1_ROW,
            WHITESPACE_BACKGROUND_2_ROW,
            WHITESPACE_BACKGROUND_3_ROW,
            WHITESPACE_BACKGROUND_4_ROW,
            WHITESPACE_BACKGROUND_5_ROW,
            WHITESPACE_BACKGROUND_6_ROW
        ).forEach {
            it.addItem(
                ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .displayName(Component.text(""))
                    .flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                    .toGuiItem()
            )
            it.setRepeat(true)
        }
    }
}