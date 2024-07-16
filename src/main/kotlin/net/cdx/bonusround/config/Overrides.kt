package net.cdx.bonusround.config

import net.cdx.bonusround.Main
import org.spongepowered.configurate.objectmapping.ConfigSerializable

fun overrides(): Overrides {
    return Main.overrides
}

@ConfigSerializable
class Overrides {

    var commandPermissionOverrides: List<String> = ArrayList()

}