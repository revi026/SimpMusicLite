package com.maxrave.simpmusic.ui.noblur

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/*
 * Lite fork: every blur is gone, to spare the low-end GPU of the target phone.
 *
 * Call sites keep their original shape (`rememberHazeState`, `hazeSource`, `hazeBlur`,
 * `Modifier.blur`) and only their imports point here, so merging upstream stays easy. Nothing in
 * this file records a layer or draws an effect: a "blurred" bar is simply its opaque tint, which is
 * what upstream already draws where blur is unavailable (Android 11 and below).
 */

/** Stand-in for haze's state: holds nothing, because nothing is sampled. */
class HazeState

@Composable
fun rememberHazeState(): HazeState = remember { HazeState() }

/** Upstream marks the content that bars blur. Here it marks nothing. */
@Suppress("UNUSED_PARAMETER")
fun Modifier.hazeSource(state: HazeState): Modifier = this

sealed interface HazeInput {
    class Sources(
        @Suppress("unused") val state: HazeState,
    ) : HazeInput
}

/** The flat replacement for a frosted bar: just the colour it falls back to. */
class BarStyle(
    val color: Color,
)

@Suppress("UNUSED_PARAMETER")
fun Modifier.hazeBlur(
    input: HazeInput,
    style: BarStyle,
): Modifier = this.background(style.color)

/** `Modifier.blur` that does nothing. Same signature as Compose's, so call sites are untouched. */
@Suppress("UNUSED_PARAMETER")
fun Modifier.blur(
    radius: Dp,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle,
): Modifier = this
