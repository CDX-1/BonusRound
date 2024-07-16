package net.cdx.bonusround.games.api

import org.bukkit.Location

class Arena(val id: String, val origin: Location) {

    private var isOpen: Boolean = true

    fun release() {
        isOpen = true
    }

    fun reserve() {
        isOpen = false
    }

    fun isReservable(): Boolean {
        return isOpen
    }

}