package dev.jdtech.jellyfin.curated.api

fun AuthStatusDto.requiresUnlock(): Boolean = pinEnabled && !unlocked
