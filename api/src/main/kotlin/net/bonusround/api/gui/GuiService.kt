package net.bonusround.api.gui

object GuiService {
    private val guis = HashMap<String, Gui>()

    fun registerGui(gui: Gui): GuiService {
        guis[gui.id] = gui
        return this
    }

    fun get(id: String): Gui? {
        return guis[id]
    }
}