package net.bonusround.api.data

import net.bonusround.api.BonusRoundAPI
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
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

            database = Database.connect("jdbc:sqlite:${path}", "org.sqlite.JDBC")
        } else if (databaseType.lowercase() == "mysql") {
            database = Database.connect(
                "jdbc:mysql://${host}",
                driver = "com.mysql.cj.jdbc.Driver",
                user = username,
                password = password
            )
        }

        DataContainerService.validateTables()
    }

    fun <ID : Comparable<ID>, E : Entity<ID>, T : IdTable<ID>, C : DataContainer<ID, E, T>> registerTable(
        table: T,
        container: KClass<C>,
    ): DataManager {
        DataContainerService.addTable(table, container)
        return this
    }

}