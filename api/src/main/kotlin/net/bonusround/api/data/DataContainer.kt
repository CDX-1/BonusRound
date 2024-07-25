package net.bonusround.api.data

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Table

abstract class DataContainer<ID : Comparable<ID>, E : Entity<ID>, T : Table> {
    abstract fun getTable(): T
}