package net.bonusround.game.generic

import io.papermc.paper.chat.ChatRenderer
import me.clip.placeholderapi.PlaceholderAPI
import net.bonusround.api.utils.component
import net.bonusround.game.configs.lang
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
        return lang().general.chatFormat.component(
            usePrefix = false,
            usePAPI = true,
            papiPlayer = source,
            values = arrayOf(
                convertHex(PlaceholderAPI.setPlaceholders(source, "%luckperms_prefix%")),
                source.name
            )
        ).append(message.color(TextColor.color(170, 170, 170)))
    }

}