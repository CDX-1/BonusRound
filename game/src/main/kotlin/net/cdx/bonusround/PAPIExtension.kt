package net.cdx.bonusround

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PAPIExtension : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "br"
    }

    override fun getAuthor(): String {
        return Main.instance.pluginMeta.authors.joinToString(", ")
    }

    override fun getVersion(): String {
        return Main.instance.pluginMeta.version
    }

    override fun canRegister(): Boolean {
        return true
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player, params: String): String {
        return Main.placeholderResolvers[params.lowercase()]?.apply(player) ?: "error"
    }
}