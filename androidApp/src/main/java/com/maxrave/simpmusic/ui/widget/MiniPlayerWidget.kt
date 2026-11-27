package com.maxrave.simpmusic.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kmpalette.rememberPaletteState
import com.maxrave.common.Config
import com.maxrave.simpmusic.MainActivity
import com.maxrave.simpmusic.R
import com.maxrave.simpmusic.extension.getColorFromPalette
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

/** The cover is drawn at 72.dp at most, so it never needs more pixels than this. */
private const val ART_DECODE_PX = 256

/** Below this width the cover gives its room to the title. */
private val MIN_WIDTH_WITH_ART = 220.dp

/** The only parts of the now-playing data this widget draws. Lyrics and the rest must not redraw it. */
private data class MiniTrack(
    val title: String,
    val artist: String,
    val thumbUrl: String?,
)

/**
 * A 4x1 player bar: cover, title and artist, previous / play / next.
 */
class MiniPlayerWidget :
    GlanceAppWidget(),
    KoinComponent {
    // Exact for the same reason as the other widgets: lay out against the size actually granted.
    override val sizeMode: SizeMode = SizeMode.Exact

    private val sharedViewModel by inject<SharedViewModel>()
    private val serviceScope by inject<CoroutineScope>(named(Config.SERVICE_SCOPE))

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        watchPlayback(context.applicationContext)

        val trackFlow =
            sharedViewModel.nowPlayingScreenData
                .map { MiniTrack(it.nowPlayingTitle, it.artistName, it.thumbnailURL) }
                .distinctUntilChanged()
        val playingFlow = sharedViewModel.controllerState.map { it.isPlaying }.distinctUntilChanged()
        val initialData = sharedViewModel.nowPlayingScreenData.value
        val initialTrack = MiniTrack(initialData.nowPlayingTitle, initialData.artistName, initialData.thumbnailURL)
        val initialPlaying = sharedViewModel.controllerState.value.isPlaying

        provideContent {
            GlanceTheme {
                val track by trackFlow.collectAsState(initialTrack)
                val isPlaying by playingFlow.collectAsState(initialPlaying)

                val paletteState = rememberPaletteState()
                var artwork by remember { mutableStateOf<Bitmap?>(null) }
                var bgColor by remember { mutableStateOf(Color.Black) }

                LaunchedEffect(track.thumbUrl) {
                    artwork = context.loadBitmap(track.thumbUrl, track.thumbUrl + "BIGGER", ART_DECODE_PX)
                }

                LaunchedEffect(artwork) {
                    artwork?.asImageBitmap()?.let { paletteState.generate(it) }
                }

                LaunchedEffect(Unit) {
                    snapshotFlow { paletteState.palette }
                        .distinctUntilChanged()
                        .collectLatest { bgColor = it.getColorFromPalette() }
                }

                MiniPlayerBar(context, track, isPlaying, artwork, bgColor)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun MiniPlayerBar(
        context: Context,
        track: MiniTrack,
        isPlaying: Boolean,
        artwork: Bitmap?,
        bgColor: Color,
    ) {
        val size = LocalSize.current
        // A one-cell row is about 40.dp on some launchers and over 90.dp on others.
        val artSide: Dp = (size.height - 16.dp).coerceIn(32.dp, 72.dp)
        val discSide: Dp = (size.height - 8.dp).coerceIn(32.dp, 40.dp)

        Row(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(bgColor))
                    .cornerRadius(android.R.dimen.system_app_widget_background_radius)
                    .padding(horizontal = 10.dp)
                    .clickable(actionStartActivity<MainActivity>(), rippleOverride = R.drawable.no_ripple),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (size.width >= MIN_WIDTH_WITH_ART) {
                Image(
                    provider = artwork?.let { ImageProvider(it) } ?: ImageProvider(R.drawable.holder),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier.size(artSide).cornerRadius(10.dp),
                )
                Spacer(GlanceModifier.width(10.dp))
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = track.title.ifBlank { context.getString(R.string.app_name) },
                    maxLines = 1,
                    style =
                        TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )
                if (track.artist.isNotBlank()) {
                    Text(
                        text = track.artist,
                        maxLines = 1,
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp),
                    )
                }
            }
            Spacer(GlanceModifier.width(6.dp))
            // As in TurntableWidget: the launcher mirrors this row on a right-to-left system and
            // Glance cannot pin it, so the two skips are swapped there to keep previous on the left.
            val systemRtl = Resources.getSystem().configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
            val previous: @Composable () -> Unit = {
                SkipButton(R.drawable.rounded_skip_previous_24, "Previous", UIEvent.Previous)
            }
            val next: @Composable () -> Unit = {
                SkipButton(R.drawable.rounded_skip_next_24, "Next", UIEvent.Next)
            }
            if (systemRtl) next() else previous()
            Box(
                modifier =
                    GlanceModifier
                        .size(discSide)
                        .background(ImageProvider(R.drawable.widget_play_disc))
                        .clickable(rippleOverride = R.drawable.no_ripple) {
                            sharedViewModel.onUIEvent(UIEvent.PlayPause)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider =
                        ImageProvider(
                            if (isPlaying) R.drawable.rounded_pause_24 else R.drawable.rounded_play_arrow_24,
                        ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                    modifier = GlanceModifier.size(24.dp),
                )
            }
            if (systemRtl) previous() else next()
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun SkipButton(
        icon: Int,
        description: String,
        event: UIEvent,
    ) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = description,
            colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
            modifier =
                GlanceModifier
                    .size(36.dp)
                    .padding(5.dp)
                    .clickable(rippleOverride = R.drawable.no_ripple) {
                        sharedViewModel.onUIEvent(event)
                    },
        )
    }

    /**
     * Wakes the widget when something it draws changes while no Glance session is running.
     * One watcher per process: [provideGlance] runs again for every new session, and starting a
     * collector each time would pile them up.
     */
    private fun watchPlayback(appContext: Context) {
        synchronized(MiniPlayerWidget) {
            if (watcher?.isActive == true) return
            watcher =
                serviceScope.launch {
                    combine(
                        sharedViewModel.controllerState.map { it.isPlaying }.distinctUntilChanged(),
                        sharedViewModel.nowPlayingScreenData
                            .map { MiniTrack(it.nowPlayingTitle, it.artistName, it.thumbnailURL) }
                            .distinctUntilChanged(),
                    ) { playing, track -> playing to track }
                        .collectLatest { MiniPlayerWidget().updateAll(appContext) }
                }
        }
    }

    companion object {
        private var watcher: Job? = null
    }
}
