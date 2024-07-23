package net.cdx.bonusround.extensions

import net.cdx.bonusround.data.PlayerDataProvider
import org.bukkit.entity.Player

val Player.dataProvider
    get() = PlayerDataProvider.getPlayer(this)