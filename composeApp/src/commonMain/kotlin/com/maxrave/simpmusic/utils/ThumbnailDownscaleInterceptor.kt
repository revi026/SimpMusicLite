package com.maxrave.simpmusic.utils

import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.size.pxOrElse
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Lite fork: ask the image CDN for the size the picture is drawn at, not the biggest one it has.
 *
 * YouTube Music cover art lives on Google's image CDN (`lh3.googleusercontent.com/...=w544-h544-l90-rj`,
 * `yt3.ggpht.com/...=s900-c-k`), which resizes on request. Call sites take the LAST (largest) entry of
 * each thumbnail list, so a 36dp avatar or a 160dp card downloads, decodes and caches the full-size
 * image and then throws most of its pixels away. On a phone with a weak CPU and little RAM that is
 * a lot of wasted work per row while scrolling.
 *
 * Runs BEFORE [NetworkFirstInterceptor], so that one sees the rewritten URL and the rewritten disk
 * key, and entries of different sizes never collide in the disk cache.
 */
class ThumbnailDownscaleInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        val url = request.data.toString()
        val viewWidth = chain.size.width.pxOrElse { 0 }
        val viewHeight = chain.size.height.pxOrElse { 0 }
        val smaller = downscaledThumbnailUrl(url, viewWidth, viewHeight) ?: return chain.proceed()

        val oldKey = request.diskCacheKey ?: url
        val newKey = if (oldKey.contains(url)) oldKey.replace(url, smaller) else "$oldKey#$smaller"
        return chain
            .withRequest(
                request
                    .newBuilder()
                    .data(smaller)
                    .diskCacheKey(newKey)
                    .build(),
            ).proceed()
    }
}

private val sizedWidthHeight = Regex("""=w(\d+)-h(\d+)""")
private val sizedSquare = Regex("""=s(\d+)""")

/** Sizes the CDN is asked for. Few and coarse, so one cover seen on three screens is not three downloads. */
private val sizeSteps = intArrayOf(128, 256, 384, 512, 768, 1024)

/**
 * The same picture at a smaller size, or null when [url] is not a resizable CDN link, the view size
 * is unknown, or the original is already no bigger than needed. Never enlarges.
 *
 * [viewWidth] and [viewHeight] are in pixels; 0 means unknown. The image is scaled to COVER the view
 * (Coil's default for cropped images), so the larger of the two ratios decides.
 */
internal fun downscaledThumbnailUrl(
    url: String,
    viewWidth: Int,
    viewHeight: Int,
): String? {
    if (viewWidth <= 0 && viewHeight <= 0) return null
    if (!url.contains("googleusercontent.com") && !url.contains("ggpht.com")) return null

    val box = sizedWidthHeight.find(url)
    val square = if (box == null) sizedSquare.find(url) else null
    val originalWidth: Int
    val originalHeight: Int
    when {
        box != null -> {
            originalWidth = box.groupValues[1].toIntOrNull() ?: return null
            originalHeight = box.groupValues[2].toIntOrNull() ?: return null
        }

        square != null -> {
            originalWidth = square.groupValues[1].toIntOrNull() ?: return null
            originalHeight = originalWidth
        }

        else -> {
            return null
        }
    }
    if (originalWidth <= 0 || originalHeight <= 0) return null

    val cover =
        max(
            if (viewWidth > 0) viewWidth.toDouble() / originalWidth else 0.0,
            if (viewHeight > 0) viewHeight.toDouble() / originalHeight else 0.0,
        )
    if (cover >= 0.9) return null
    val neededWidth = ceil(originalWidth * cover).toInt()
    val newWidth = sizeSteps.firstOrNull { it >= neededWidth } ?: return null
    if (newWidth >= originalWidth) return null

    return if (box != null) {
        val newHeight = (originalHeight.toDouble() * newWidth / originalWidth).roundToInt().coerceAtLeast(1)
        url.replaceRange(box.range, "=w$newWidth-h$newHeight")
    } else {
        url.replaceRange(square!!.range, "=s$newWidth")
    }
}
