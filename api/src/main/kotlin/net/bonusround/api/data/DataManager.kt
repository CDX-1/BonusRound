package net.bonusround.api.data

import net.bonusround.api.BonusRoundAPI
import net.bonusround.api.utils.EventListener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import java.io.File
import kotlin.reflect.KClass

fun createTableName(tableName: String): String {
    return "BR_${tableName}"
}

class DataManager(
    private val databaseType: String,
    private val sqliteFileName: String,
    private val host: String,
    private val username: String,
    private val password: String,
) {

    companion object {
        lateinit var database: Database
    }

    fun register() {
        if (databaseType.lowercase() == "sqlite") {
            val path = File(BonusRoundAPI.main.dataFolder, "${sqliteFileName}.db")
            database = Database.connect("jdbc:sqlite:${path}?busy_timeout=30000", "org.sqlite.JDBC", databaseConfig = DatabaseConfig {
                defaultMaxAttempts = 30
                defaultMinRetryDelay = 50
            })
        } else if (databaseType.lowercase() == "mysql") {
            database = Database.connect(
                "jdbc:mysql://${host}",
                driver = "com.mysql.cj.jdbc.Driver",
                user = username,
                password = password
            )
        }

        DataContainerService.validateTables()

        val playerTables = ArrayList<PlayerTable>()

        DataContainerService.tables.forEach { table ->
            if (table is PlayerTable) {
                playerTables.add(table)
            }
        }

        EventListener(PlayerJoinEvent::class.java) { event ->
            playerTables.forEach { it.onJoin(event.player) }
        }

        EventListener(PlayerQuitEvent::class.java) { event ->
            playerTables.forEach { it.onQuit(event.player) }
        }
    }

    fun <ID : Comparable<ID>, E : Entity<ID>, T : IdTable<ID>, C : DataContainer<ID, E, T>> registerTable(
        table: T,
        container: KClass<C>,
    ): DataManager {
        DataContainerService.addTable(table, container)
        return this
    }

}