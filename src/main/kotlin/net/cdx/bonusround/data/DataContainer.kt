package net.cdx.bonusround.data

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.IdTable

abstract class DataContainer<ID : Comparable<ID>, E : Entity<ID>, T : IdTable<ID>>() {

    abstract fun getTable(): T

}