package dev.curated.app.settings.domain.models

data class Preference<out T>(val backendName: String, val defaultValue: T)
