package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * The backdrop a glass surface samples from.
 *
 * Lite fork: nothing samples it any more (see [layerBackdrop] and `liquidGlass`), the type only
 * survives so the many call sites keep compiling unchanged.
 */
typealias PlatformBackdrop = LayerBackdrop

/**
 * Upstream marks a composable as the source layer that glass surfaces refract. Lite fork: a no-op,
 * so no screen is recorded into an extra offscreen layer every frame.
 */
@Suppress("UNUSED_PARAMETER")
fun Modifier.layerBackdrop(backdrop: PlatformBackdrop): Modifier = this

@Composable
fun rememberBackdrop(color: Color): PlatformBackdrop =
    rememberLayerBackdrop {
        drawRect(color)
        drawContent()
    }

/**
 * The backdrop App records behind its screens, for glass drawn outside them. Null where no
 * screen provides it, which draws those sheets flat.
 */
val LocalAppBackdrop = staticCompositionLocalOf<PlatformBackdrop?> { null }

/**
 * The app window's height in px, read where App composes, so SheetSurface compares a sheet against
 * the app window rather than whatever its own layer reports. 0 where not provided.
 */
val LocalAppWindowHeight = compositionLocalOf { 0 }
