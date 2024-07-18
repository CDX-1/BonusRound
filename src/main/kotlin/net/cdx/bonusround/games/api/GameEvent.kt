package net.cdx.bonusround.games.api

data class GameEvent(val eventId: String, val parameters: HashMap<String, Any> = hashMapOf())