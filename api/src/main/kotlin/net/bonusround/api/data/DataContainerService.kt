package net.bonusround.api.data

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object DataContainerService {
    private val containers = ConcurrentHashMap<KClass<*>, ConcurrentHashMap<Any, Any>>()
    val tables = ArrayList<Table>()

    @Suppress("UNCHECKED_CAST")
    fun <ID : Comparable<ID>, E : Entity<ID>, T : Table, C : DataContainer<ID, E, T>, K : Any> getContainers(
        clazz: KClass<out C>,
        @Suppress("UNUSED_PARAMETER") id: KClass<ID>,
        @Suppress("UNUSED_PARAMETER") key: KClass<K>,
    ): ConcurrentHashMap<K, C>? {
        return containers[clazz] as? ConcurrentHashMap<K, C>
    }

    @Suppress("UNCHECKED_CAST")
    fun <ID : Comparable<ID>, E : Entity<ID>, T : Table, C : DataContainer<ID, E, T>, K : Any, K2 : Any> getContainersMap(
        clazz: KClass<out C>,
        @Suppress("UNUSED_PARAMETER") id: KClass<ID>,
        @Suppress("UNUSED_PARAMETER") key: KClass<K>,
        @Suppress("UNUSED_PARAMETER") key2: KClass<K2>,
    ): ConcurrentHashMap<K, ConcurrentHashMap<K2, C>>? {
        return containers[clazz] as? ConcurrentHashMap<K, ConcurrentHashMap<K2, C>>
    }

    fun validateTables() {
        runBlocking {
            transaction {
                tables.forEach { table ->
                    SchemaUtils.createMissingTablesAndColumns(table)
                }
            }
        }
    }

    fun <ID : Comparable<ID>, E : Entity<ID>, T : Table, C : DataContainer<ID, E, T>> addTable(
        table: Table,
        container: KClass<out C>,
    ) {
        tables.add(table)
        containers[container] = ConcurrentHashMap()
    }
}