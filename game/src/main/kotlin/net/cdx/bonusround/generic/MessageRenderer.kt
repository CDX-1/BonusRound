package net.cdx.bonusround.generic

import io.papermc.paper.chat.ChatRenderer
import me.clip.placeholderapi.PlaceholderAPI
import net.cdx.bonusround.config.lang
import net.cdx.bonusround.utils.Formatter
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

private fun convertHex(input: String): String {
    return Regex("&(#?[a-fA-F0-9]{6})").replace(input) { matchResult ->
        "<color:${matchResult.groupValues[1]}>"
    }
}

class MessageRenderer : ChatRenderer {
    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        return Formatter(lang().general.chatFormat)
            .usePrefix(false)
            .usePAPI(true, source)
            .placeholders(convertHex(PlaceholderAPI.setPlaceholders(source, "%luckperms_prefix%")))
            .component()
            .append(message.color(TextColor.color(170, 170, 170)))
    }

}