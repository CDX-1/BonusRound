package net.cdx.bonusround

import org.bukkit.Bukkit
import org.bukkit.event.*
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.NotNull
import java.util.function.BiConsumer
import java.util.function.Consumer

class EventListener<T : Event?>(@NotNull plugin: JavaPlugin?, @NotNull private val eventClass: Class<T>, @NotNull priority: EventPriority, @NotNull private val handler: BiConsumer<EventListener<T>, T>): org.bukkit.event.Listener {

    init {
        if (plugin != null) {
            @Suppress("UNCHECKED_CAST")
            Bukkit.getPluginManager().registerEvent(eventClass, this, priority, { _: Listener?, e: Event -> handleEvent(e as T) }, plugin)
        }
    }

    constructor(@NotNull eventClass: Class<T>, @NotNull priority: EventPriority, @NotNull handler: BiConsumer<EventListener<T>, T>) : this(Main.instance, eventClass, priority, handler)

    constructor(@NotNull plugin: JavaPlugin, @NotNull eventClass: Class<T>, @NotNull handler: BiConsumer<EventListener<T>, T>) : this(plugin, eventClass, EventPriority.NORMAL, handler)

    constructor(@NotNull eventClass: Class<T>, @NotNull handler: BiConsumer<EventListener<T>, T>) : this(Main.instance, eventClass, EventPriority.NORMAL, handler)

    constructor(@NotNull plugin: JavaPlugin?, @NotNull eventClass: Class<T>, priority: EventPriority, @NotNull handler: Consumer<T>) : this(plugin, eventClass, priority, BiConsumer<EventListener<T>, T> { _: EventListener<T>?, e: T -> handler.accept(e) })

    constructor(@NotNull eventClass: Class<T>, @NotNull priority: EventPriority, @NotNull handler: Consumer<T>) : this(Main.instance, eventClass, priority, handler)

    constructor(@NotNull plugin: JavaPlugin, @NotNull eventClass: Class<T>, @NotNull handler: Consumer<T>) : this(plugin, eventClass, EventPriority.NORMAL, handler)

    constructor(@NotNull eventClass: Class<T>, @NotNull handler: Consumer<T>) : this(Main.instance, eventClass, EventPriority.NORMAL, handler)

    @EventHandler
    fun handleEvent(@NotNull event: T) {
        if (eventClass.isAssignableFrom(event!!::class.java)) {
            handler.accept(this, event)
        }
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
    }
}