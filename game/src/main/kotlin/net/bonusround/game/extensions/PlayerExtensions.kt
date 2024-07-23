package net.bonusround.game.extensions

import net.bonusround.game.data.PlayerDataProvider
import org.bukkit.entity.Player

val Player.dataProvider
    get() = PlayerDataProvider.getPlayer(this)