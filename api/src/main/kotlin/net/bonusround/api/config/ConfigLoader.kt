package net.bonusround.api.config

import net.bonusround.api.BonusRoundAPI
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import kotlin.io.path.Path
import kotlin.reflect.KClass

class ConfigLoader<T : Any>(path: String, private val type: KClass<T>) {

    private val loader = HoconConfigurationLoader.builder()
        .path(Path(BonusRoundAPI.main.dataFolder.path, path))
        .build()

    private val node = loader.load()
    private val config: T = node.get(type.java) as T

    fun load(): T {
        return config
    }

    fun save() {
        node.set(type.java, config)
        loader.save(node)
    }

}