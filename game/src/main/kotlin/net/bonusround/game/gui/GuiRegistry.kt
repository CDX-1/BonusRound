package net.bonusround.game.gui

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import net.bonusround.game.Main

class GuiRegistry {

    fun get(guiId: String): ChestGui {

        val stream = Main.instance.getResource("guis/$guiId.xml")
        var gui: ChestGui? = null
        if (stream != null) {
            gui = ChestGui.load(Main.instance, stream)
        } else {
            Main.logger.severe("Gui not found: $guiId")
        }

        return gui!! // Intentional NPE if invalid gui id is provided

    }

}