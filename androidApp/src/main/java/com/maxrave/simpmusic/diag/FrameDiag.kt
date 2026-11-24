package com.maxrave.simpmusic.diag

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.snapshots.Snapshot
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * TEMPORARY diagnostic (started with `am start --ez diag true`): counts writes to Compose state objects and logs
 * the busiest ones every few seconds with the stack of the first write, to find what keeps the UI redrawing.
 */
object FrameDiag {
    private val counts = ConcurrentHashMap<String, AtomicInteger>()
    private val firstStack = ConcurrentHashMap<String, String>()
    private var started = false

    fun start() {
        if (started) return
        started = true
        com.maxrave.simpmusic.diag.Recomp.enabled = true
        Snapshot.registerGlobalWriteObserver { state ->
            val key = state.javaClass.simpleName + "@" + Integer.toHexString(System.identityHashCode(state))
            counts.getOrPut(key) { AtomicInteger() }.incrementAndGet()
            if (firstStack.putIfAbsent(key, "") == null) {
                firstStack[key] =
                    Throwable()
                        .stackTrace
                        .drop(2)
                        .take(16)
                        .joinToString(" <- ") { it.className.substringAfterLast('.') + "." + it.methodName + ":" + it.lineNumber }
            }
        }
        val handler = Handler(Looper.getMainLooper())
        val dump =
            object : Runnable {
                override fun run() {
                    val top = counts.entries.sortedByDescending { it.value.get() }.take(8)
                    Log.w("FrameDiag", "---- state writes in the last 4s: ${counts.values.sumOf { it.get() }} total ----")
                    top.forEach { Log.w("FrameDiag", "${it.value.get()} x ${it.key}: ${firstStack[it.key]}") }
                    val rc = com.maxrave.simpmusic.diag.Recomp.counts
                    Log.w("FrameDiag", "recompositions in 4s: " + rc.entries.sortedByDescending { it.value }.joinToString(", ") { "${it.key}=${it.value}" })
                    rc.clear()
                    counts.clear()
                    handler.postDelayed(this, 4000)
                }
            }
        handler.postDelayed(dump, 4000)
    }
}
