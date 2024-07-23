package net.cdx.bonusround.data

import kotlinx.coroutines.runBlocking
import net.cdx.bonusround.data.schemas.containers.PlayerDataContainer
import net.cdx.bonusround.data.schemas.tables.PlayerDataTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object DataContainerService {

    private val containers = ConcurrentHashMap<KClass<*>, ConcurrentHashMap<Any, Any>>()

    init {
        containers[PlayerDataContainer::class] = ConcurrentHashMap()
    }

    @Suppress("UNCHECKED_CAST")
    fun <ID : Comparable<ID>, C : Any> getContainers(clazz: KClass<C>, id: KClass<ID>): ConcurrentHashMap<ID, C>? {
        return containers[clazz] as? ConcurrentHashMap<ID, C>
    }

    fun validateTables() {
        runBlocking {
            transaction {
                SchemaUtils.createMissingTablesAndColumns(PlayerDataTable)
            }
        }
    }

}