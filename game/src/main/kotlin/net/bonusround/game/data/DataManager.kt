package net.bonusround.game.data

import net.bonusround.game.Main
import net.bonusround.api.utils.Registrable
import net.bonusround.game.configs.conf
import org.jetbrains.exposed.sql.Database
import java.io.File

fun createTableName(tableName: String): String {
    return "BR_${tableName}"
}

class DataManager : Registrable {

    companion object {

        lateinit var database: Database

    }

    override fun register() {

        val databaseType = conf().databaseType
        if (databaseType.lowercase() == "sqlite") {
            val fileName = conf().sqliteFileName
            val path = File(Main.instance.dataFolder, "${fileName}.db")

            database = Database.connect("jdbc:sqlite:${path}", "org.sqlite.JDBC")
        } else if (databaseType.lowercase() == "mysql") {
            val host = conf().host
            val user = conf().user
            val pass = conf().pass

            database = Database.connect(
                "jdbc:mysql://${host}",
                driver = "com.mysql.cj.jdbc.Driver",
                user = user,
                password = pass
            )
        }

        DataContainerService.validateTables()

    }

}