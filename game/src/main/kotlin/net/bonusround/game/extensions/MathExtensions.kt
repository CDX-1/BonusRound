package net.bonusround.game.extensions

infix fun Int.safeSub(int: Int): Int {
    return (this - int).coerceAtLeast(0)
}