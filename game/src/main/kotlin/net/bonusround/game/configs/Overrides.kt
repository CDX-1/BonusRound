package net.bonusround.game.configs

import net.bonusround.game.Main
import org.spongepowered.configurate.objectmapping.ConfigSerializable

fun overrides(): Overrides {
    return Main.overrides
}

@ConfigSerializable
class Overrides {

    var commandPermissionOverrides: List<String> = ArrayList()

}