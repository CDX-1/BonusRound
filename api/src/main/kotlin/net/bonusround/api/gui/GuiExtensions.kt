package net.bonusround.api.gui

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import net.bonusround.api.utils.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun ChestGui.pane(pane: Pane): ChestGui {
    this.addPane(pane)
    return this
}

fun ChestGui.isInterface(isInterface: Boolean): ChestGui {
    if (isInterface) {
        this.setOnGlobalClick { event -> event.isCancelled = true }
    }
    return this
}

fun ItemStack.toGuiItem(): GuiItem {
    return GuiItem(this)
}

fun ItemBuilder.toGuiItem(): GuiItem {
    return GuiItem(this.item())
}

fun ItemStack.toStaticPane(x: Int, y: Int, length: Int, height: Int, slotX: Int, slotY: Int): StaticPane {
    val pane = StaticPane(x, y, length, height)
    pane.addItem(this.toGuiItem(), slotX, slotY)
    return pane
}

fun ItemBuilder.toStaticPane(x: Int, y: Int, length: Int, height: Int, slotX: Int, slotY: Int): StaticPane {
    return this.item().toStaticPane(x, y, length, height, slotX, slotY)
}

fun Player.openGui(id: String) {
    GuiService.get(id)?.open(this)
}