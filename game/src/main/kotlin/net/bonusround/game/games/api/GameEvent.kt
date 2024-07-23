package net.bonusround.game.games.api

data class GameEvent(val eventId: String, val parameters: HashMap<String, Any> = hashMapOf())