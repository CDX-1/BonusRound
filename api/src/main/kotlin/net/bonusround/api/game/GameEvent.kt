package net.bonusround.api.game

data class GameEvent(val eventId: String, val parameters: HashMap<String, Any> = hashMapOf())