package com.maxrave.simpmusic.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ThumbnailDownscaleTest {
    private val cover = "https://lh3.googleusercontent.com/abc=w544-h544-l90-rj"

    @Test
    fun smallViewAsksForASmallerSquare() {
        assertEquals("https://lh3.googleusercontent.com/abc=w128-h128-l90-rj", downscaledThumbnailUrl(cover, 72, 72))
        assertEquals("https://lh3.googleusercontent.com/abc=w384-h384-l90-rj", downscaledThumbnailUrl(cover, 320, 320))
    }

    @Test
    fun neverEnlargesAndLeavesNearlyFullSizeAlone() {
        assertNull(downscaledThumbnailUrl(cover, 640, 640))
        assertNull(downscaledThumbnailUrl("https://lh3.googleusercontent.com/abc=w226-h226-l90-rj", 300, 300))
    }

    @Test
    fun wideBannerKeepsItsAspectRatio() {
        assertEquals(
            "https://lh3.googleusercontent.com/abc=w768-h432-l90-rj",
            downscaledThumbnailUrl("https://lh3.googleusercontent.com/abc=w1280-h720-l90-rj", 720, 400),
        )
    }

    @Test
    fun squareSuffixIsResizedToo() {
        assertEquals(
            "https://yt3.ggpht.com/xyz=s256-c-k-c0x00ffffff-no-rj",
            downscaledThumbnailUrl("https://yt3.ggpht.com/xyz=s900-c-k-c0x00ffffff-no-rj", 200, 200),
        )
    }

    @Test
    fun unknownSizeOrOtherHostsAreUntouched() {
        assertNull(downscaledThumbnailUrl(cover, 0, 0))
        assertNull(downscaledThumbnailUrl("https://i.ytimg.com/vi/ID/maxresdefault.jpg", 300, 300))
    }
}
