package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.data.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Loading : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}

class AudioPlayerController(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _trackDuration = MutableStateFlow(0L)
    val trackDuration: StateFlow<Long> = _trackDuration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    // Queue Management
    private var playlistQueue = mutableListOf<Track>()
    private var currentQueueIndex = -1

    // Coroutine Jobs
    private var progressJob: Job? = null
    private val controllerScope = CoroutineScope(Dispatchers.Main + Job())

    // Sleep Timer
    private val _sleepTimerMinutesLeft = MutableStateFlow<Int?>(null)
    val sleepTimerMinutesLeft: StateFlow<Int?> = _sleepTimerMinutesLeft.asStateFlow()
    private var sleepTimerHandler = Handler(Looper.getMainLooper())
    private var sleepTimerRunnable: Runnable? = null

    init {
        // Initialize MediaPlayer
        resetPlayer()
    }

    private fun resetPlayer() {
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {}
        
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener {
                _playbackState.value = PlaybackState.Playing
                _trackDuration.value = duration.toLong()
                
                // Apply speed if supported
                applySpeed(playbackSpeed.value)
                
                start()
                startProgressTicker()
            }
            setOnCompletionListener {
                playNext()
            }
            setOnErrorListener { _, what, extra ->
                _playbackState.value = PlaybackState.Error("Media source playback error code ($what, $extra)")
                false
            }
        }
    }

    fun setQueue(tracks: List<Track>, startingIndex: Int = 0) {
        playlistQueue.clear()
        playlistQueue.addAll(tracks)
        currentQueueIndex = if (startingIndex in tracks.indices) startingIndex else 0
        if (playlistQueue.isNotEmpty()) {
            playTrack(playlistQueue[currentQueueIndex])
        }
    }

    fun playTrack(track: Track) {
        _currentTrack.value = track
        _playbackState.value = PlaybackState.Loading
        _currentPosition.value = 0L
        _trackDuration.value = 0L
        stopProgressTicker()

        try {
            resetPlayer()
            mediaPlayer?.apply {
                setDataSource(context, Uri.parse(track.url))
                prepareAsync()
            }
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.Error("Failed to load: ${e.localizedMessage}")
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        val currentState = _playbackState.value
        
        if (currentState is PlaybackState.Playing) {
            player.pause()
            _playbackState.value = PlaybackState.Paused
            stopProgressTicker()
        } else if (currentState is PlaybackState.Paused) {
            player.start()
            _playbackState.value = PlaybackState.Playing
            startProgressTicker()
        } else if (currentState is PlaybackState.Idle && _currentTrack.value != null) {
            _currentTrack.value?.let { playTrack(it) }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let {
            it.seekTo(positionMs.toInt())
            _currentPosition.value = positionMs
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applySpeed(speed)
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    try {
                        player.playbackParams = PlaybackParams().apply { this.speed = speed }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun playNext() {
        if (playlistQueue.isEmpty()) return
        currentQueueIndex = (currentQueueIndex + 1) % playlistQueue.size
        playTrack(playlistQueue[currentQueueIndex])
    }

    fun playPrevious() {
        if (playlistQueue.isEmpty()) return
        currentQueueIndex = if (currentQueueIndex - 1 < 0) playlistQueue.size - 1 else currentQueueIndex - 1
        playTrack(playlistQueue[currentQueueIndex])
    }

    // Progress Ticker
    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = controllerScope.launch {
            while (true) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _currentPosition.value = it.currentPosition.toLong()
                    }
                }
                delay(250) // Update progress 4 times a second
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    // Sleep Timer Action
    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        _sleepTimerMinutesLeft.value = minutes

        // Setup ticking runnable every minute
        val timerRunnable = object : Runnable {
            override fun run() {
                val current = _sleepTimerMinutesLeft.value
                if (current == null) return
                if (current <= 1) {
                    // Timer finished, pause and clear
                    pausePlayback()
                    _sleepTimerMinutesLeft.value = null
                } else {
                    _sleepTimerMinutesLeft.value = current - 1
                    sleepTimerHandler.postDelayed(this, 60_000)
                }
            }
        }
        sleepTimerRunnable = timerRunnable
        sleepTimerHandler.postDelayed(timerRunnable, 60_000)
    }

    fun cancelSleepTimer() {
        sleepTimerRunnable?.let { sleepTimerHandler.removeCallbacks(it) }
        sleepTimerRunnable = null
        _sleepTimerMinutesLeft.value = null
    }

    private fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.value = PlaybackState.Paused
                stopProgressTicker()
            }
        }
    }

    fun release() {
        stopProgressTicker()
        cancelSleepTimer()
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
