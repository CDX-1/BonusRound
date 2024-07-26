package net.bonusround.api

import net.kyori.adventure.title.Title
import org.bukkit.plugin.java.JavaPlugin

object BonusRoundAPI {
    lateinit var main: JavaPlugin

    object Lang {
        lateinit var matchFoundTitle: Title
    }
}