package dev.jdtech.jellyfin.presentation.curated

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.jdtech.jellyfin.curated.repository.CuratedRepositoryFactory
import dev.jdtech.jellyfin.curatedStartPositionMs
import dev.jdtech.jellyfin.settings.domain.AppPreferences
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import timber.log.Timber

@HiltViewModel
class CuratedPlayerViewModel
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val repositoryFactory: CuratedRepositoryFactory,
    private val okHttpClient: OkHttpClient,
    private val appPreferences: AppPreferences,
) : ViewModel(), Player.Listener {
    val player: ExoPlayer

    private val trackSelector = DefaultTrackSelector(context)
    private var initializedMovieId: String? = null

    var playWhenReady = true
    var playbackSpeed = 1f

    private val _uiState = MutableStateFlow(CuratedPlayerState())
    val uiState = _uiState.asStateFlow()

    private val eventsChannel = Channel<CuratedPlayerEvent>()
    val events = eventsChannel.receiveAsFlow()

    init {
        val audioAttributes =
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build()

        trackSelector.setParameters(
            trackSelector
                .buildUponParameters()
                .setTunnelingEnabled(true)
                .setPreferredAudioLanguage(
                    appPreferences.getValue(appPreferences.preferredAudioLanguage)
                )
                .setPreferredTextLanguage(
                    appPreferences.getValue(appPreferences.preferredSubtitleLanguage)
                )
        )

        val renderersFactory =
            DefaultRenderersFactory(context)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        player =
            ExoPlayer.Builder(context, renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setAudioAttributes(audioAttributes, true)
                .setTrackSelector(trackSelector)
                .setSeekBackIncrementMs(appPreferences.getValue(appPreferences.playerSeekBackInc))
                .setSeekForwardIncrementMs(
                    appPreferences.getValue(appPreferences.playerSeekForwardInc)
                )
                .build()
        player.addListener(this)
    }

    fun initialize(movieId: String, title: String?) {
        if (initializedMovieId == movieId) return
        initializedMovieId = movieId

        viewModelScope.launch {
            _uiState.value =
                CuratedPlayerState(currentTitle = title.orEmpty(), isLoading = true)
            try {
                val descriptor =
                    repositoryFactory.createForCurrentServer().getPlaybackDescriptor(movieId)
                val mediaTitle = title?.takeIf { it.isNotBlank() } ?: descriptor.fileName ?: movieId
                val mediaItem =
                    MediaItem.Builder()
                        .setMediaId(descriptor.movieId)
                        .setUri(descriptor.url)
                        .setMimeType(descriptor.mimeType)
                        .setMediaMetadata(MediaMetadata.Builder().setTitle(mediaTitle).build())
                        .build()

                _uiState.value =
                    CuratedPlayerState(
                        currentTitle = mediaTitle,
                        isLoading = false,
                        sessionId = descriptor.sessionId,
                    )
                player.setMediaItems(listOf(mediaItem), 0, descriptor.curatedStartPositionMs())
                player.prepare()
                player.play()
            } catch (e: Exception) {
                Timber.e(e)
                val message = e.localizedMessage ?: "Unable to start playback"
                _uiState.value =
                    CuratedPlayerState(
                        currentTitle = title.orEmpty(),
                        isLoading = false,
                        errorMessage = message,
                    )
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updatePlaybackProgress() {
        // Progress sync will be wired to /api/playback/progress/{movieId} in the next step.
    }

    fun selectSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        playbackSpeed = speed
    }

    fun switchToTrack(trackType: @C.TrackType Int, index: Int) {
        if (index == -1) {
            player.trackSelectionParameters =
                player.trackSelectionParameters
                    .buildUpon()
                    .clearOverridesOfType(trackType)
                    .setTrackTypeDisabled(trackType, true)
                    .build()
        } else {
            val group =
                player.currentTracks.groups.filter { it.type == trackType && it.isSupported }[
                    index
                ]
            player.trackSelectionParameters =
                player.trackSelectionParameters
                    .buildUpon()
                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, 0))
                    .setTrackTypeDisabled(trackType, false)
                    .build()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> _uiState.update { it.copy(fileLoaded = true) }
            Player.STATE_ENDED -> eventsChannel.trySend(CuratedPlayerEvent.NavigateBack)
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        eventsChannel.trySend(CuratedPlayerEvent.IsPlayingChanged(isPlaying))
    }

    override fun onCleared() {
        super.onCleared()
        player.removeListener(this)
        player.release()
    }
}

data class CuratedPlayerState(
    val currentTitle: String = "",
    val isLoading: Boolean = false,
    val fileLoaded: Boolean = false,
    val sessionId: String? = null,
    val errorMessage: String? = null,
)

sealed interface CuratedPlayerEvent {
    data object NavigateBack : CuratedPlayerEvent

    data class IsPlayingChanged(val isPlaying: Boolean) : CuratedPlayerEvent
}
