package com.maxrave.simpmusic.extension

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.takeWhile
import kotlin.math.ceil

/*
 * Lite fork: a scrolling title that does not keep the GPU busy.
 *
 * Compose's basicMarquee redraws the whole window on every frame while it scrolls, and the call sites ask
 * it to repeat forever. On the target phone one frame of the full window costs 15 to 28 ms of GPU, so a
 * single scrolling title kept the GPU at 99 percent for as long as the screen was open.
 *
 * This one steps at about 12 frames per second, every scrolling title on screen steps on the SAME tick
 * (one redraw per tick, not one per title), and it stops after two passes. When nothing is scrolling the
 * ticker is not running and the UI draws nothing.
 */

private const val MARQUEE_TICK_MS = 83L
private const val MARQUEE_MAX_PASSES = 2

private object MarqueeTicker {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Runs only while at least one title is scrolling; every subscriber sees the same ticks. */
    val ticks: SharedFlow<Long> =
        flow {
            var n = 0L
            while (true) {
                delay(MARQUEE_TICK_MS)
                emit(n++)
            }
        }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)
}

@Suppress("UNUSED_PARAMETER")
fun Modifier.lowFpsMarquee(
    iterations: Int,
    repeatDelayMillis: Int,
    initialDelayMillis: Int,
    spacing: MarqueeSpacing,
    velocity: Dp,
): Modifier =
    composed {
        val density = LocalDensity.current
        var contentWidth by remember { mutableIntStateOf(0) }
        var viewWidth by remember { mutableIntStateOf(0) }
        // Read while drawing only, so a step redraws and does not recompose.
        var offsetPx by remember { mutableFloatStateOf(0f) }
        var cyclePx by remember { mutableIntStateOf(0) }

        LaunchedEffect(contentWidth, viewWidth, velocity, repeatDelayMillis, initialDelayMillis, iterations) {
            offsetPx = 0f
            if (viewWidth <= 0 || contentWidth <= viewWidth) {
                cyclePx = 0
                return@LaunchedEffect
            }
            val gap = with(spacing) { density.calculateSpacing(contentWidth, viewWidth) }
            val cycle = contentWidth + gap
            cyclePx = cycle
            val pxPerTick = with(density) { velocity.toPx() } * MARQUEE_TICK_MS / 1000f
            val passes = minOf(iterations, MARQUEE_MAX_PASSES)
            var waiting = ceil(initialDelayMillis / MARQUEE_TICK_MS.toFloat()).toInt()
            var moving = false
            var moved = 0f
            var pass = 0
            var finished = false
            MarqueeTicker.ticks.takeWhile { !finished }.collect {
                if (!moving) {
                    if (--waiting <= 0) {
                        moving = true
                        moved = 0f
                    }
                } else {
                    moved += pxPerTick
                    if (moved >= cycle) {
                        offsetPx = 0f
                        moving = false
                        pass++
                        if (pass >= passes) {
                            finished = true
                        } else {
                            waiting = ceil(repeatDelayMillis / MARQUEE_TICK_MS.toFloat()).toInt()
                        }
                    } else {
                        offsetPx = moved
                    }
                }
            }
        }

        this
            .clipToBounds()
            .layout { measurable, constraints ->
                // The child is measured as wide as it wants, as basicMarquee does, and clipped to the view.
                val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
                val width =
                    if (constraints.hasBoundedWidth) {
                        placeable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
                    } else {
                        placeable.width
                    }
                contentWidth = placeable.width
                viewWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
                layout(width, placeable.height) { placeable.place(0, 0) }
            }.drawWithContent {
                val offset = offsetPx
                if (offset <= 0f || cyclePx <= 0) {
                    drawContent()
                } else {
                    translate(left = -offset) { this@drawWithContent.drawContent() }
                    translate(left = cyclePx - offset) { this@drawWithContent.drawContent() }
                }
            }
    }
