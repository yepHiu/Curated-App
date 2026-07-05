package dev.curated.app.curated.api

fun AuthStatusDto.requiresUnlock(): Boolean = pinEnabled && !unlocked
