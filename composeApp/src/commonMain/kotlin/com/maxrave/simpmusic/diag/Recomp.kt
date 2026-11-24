package com.maxrave.simpmusic.diag

/** TEMPORARY diagnostic: counts how often a composable's scope recomposes. Off unless FrameDiag turns it on. */
object Recomp {
    var enabled = false
    val counts = mutableMapOf<String, Int>()

    fun mark(name: String) {
        if (enabled) counts[name] = (counts[name] ?: 0) + 1
    }
}
