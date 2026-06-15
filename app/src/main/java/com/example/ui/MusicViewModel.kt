package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerController
import com.example.audio.PlaybackState
import com.example.data.*
import com.example.ai.GeminiMusicService
import com.example.ai.RecognitionResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Search : Screen()
    object Library : Screen()
    object Downloads : Screen()
    object Settings : Screen()
    object EchoFind : Screen()
    object ListenTogether : Screen()
    data class PlaylistDetails(val playlist: Playlist) : Screen()
}

data class ListenTogetherSession(
    val roomId: String,
    val isHost: Boolean,
    val members: List<String>,
    val recentMessage: String? = null
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application)
    val audioController = AudioPlayerController(application)

    // --- Screen Navigation ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- Search Query and Filter ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- Observable Room Database States ---
    val allTracks: StateFlow<List<Track>> = repository.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = repository.getFavoriteTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedTracks: StateFlow<List<Track>> = repository.getDownloadedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localTracks: StateFlow<List<Track>> = repository.getLocalTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val podcasts: StateFlow<List<Podcast>> = repository.getAllPodcasts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search Results Flow ---
    val searchedTracks: StateFlow<List<Track>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.getAllTracks() else repository.searchTracks(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Spotify Import States ---
    private val _spotifyImportState = MutableStateFlow<String?>(null) // "idle", "authorizing", "success", "error"
    val spotifyImportState: StateFlow<String?> = _spotifyImportState.asStateFlow()

    private val _importedTracksCount = MutableStateFlow(0)
    val importedTracksCount: StateFlow<Int> = _importedTracksCount.asStateFlow()

    // --- Echo Find (Song Recognition) States ---
    private val _isEchoScanning = MutableStateFlow(false)
    val isEchoScanning: StateFlow<Boolean> = _isEchoScanning.asStateFlow()

    private val _echoMatchedTrack = MutableStateFlow<RecognitionResult?>(null)
    val echoMatchedTrack: StateFlow<RecognitionResult?> = _echoMatchedTrack.asStateFlow()

    private val _micHumQuery = MutableStateFlow("")
    val micHumQuery: StateFlow<String> = _micHumQuery.asStateFlow()

    // --- Listen Together States ---
    private val _currentSession = MutableStateFlow<ListenTogetherSession?>(null)
    val currentSession: StateFlow<ListenTogetherSession?> = _currentSession.asStateFlow()

    private val _roomCodeInput = MutableStateFlow("")
    val roomCodeInput: StateFlow<String> = _roomCodeInput.asStateFlow()

    // --- Podcasts Screen States ---
    private val _activePodcast = MutableStateFlow<Podcast?>(null)
    val activePodcast: StateFlow<Podcast?> = _activePodcast.asStateFlow()

    val activePodcastEpisodes = _activePodcast
        .flatMapLatest { podcast ->
            if (podcast == null) flowOf(emptyList()) else repository.getEpisodesForPodcast(podcast.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Smart Prompt Suggestion State ---
    private val _aiRecommendationQuery = MutableStateFlow("")
    val aiRecommendationQuery: StateFlow<String> = _aiRecommendationQuery.asStateFlow()

    private val _aiRecommendationResponse = MutableStateFlow("")
    val aiRecommendationResponse: StateFlow<String> = _aiRecommendationResponse.asStateFlow()

    private val _isLoadingAiRec = MutableStateFlow(false)
    val isLoadingAiRec: StateFlow<Boolean> = _isLoadingAiRec.asStateFlow()

    // --- Discord Integration Toggle ---
    private val _isDiscordPresenceEnabled = MutableStateFlow(true)
    val isDiscordPresenceEnabled: StateFlow<Boolean> = _isDiscordPresenceEnabled.asStateFlow()

    // --- Screen Control Functions ---
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onMicHumQueryChanged(text: String) {
        _micHumQuery.value = text
    }

    fun onRoomCodeInputChanged(text: String) {
        _roomCodeInput.value = text
    }

    fun onAiQueryChanged(text: String) {
        _aiRecommendationQuery.value = text
    }

    fun selectPodcast(podcast: Podcast?) {
        _activePodcast.value = podcast
    }

    // --- User Actions ---
    fun toggleFavorite(trackId: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId, isFav)
        }
    }

    fun toggleDownload(trackId: String, isDownloaded: Boolean) {
        viewModelScope.launch {
            repository.toggleDownload(trackId, isDownloaded)
            // If we "Downloaded" it, let's play mock visual queues
            delay(100)
        }
    }

    fun toggleDiscordPresence(enabled: Boolean) {
        _isDiscordPresenceEnabled.value = enabled
    }

    // --- Playback Controls ---
    fun playTrackList(tracks: List<Track>, startingIndex: Int = 0) {
        audioController.setQueue(tracks, startingIndex)
    }

    // --- AI Recommendations ---
    fun loadAiRecommendations() {
        if (_aiRecommendationQuery.value.trim().isEmpty()) return
        viewModelScope.launch {
            _isLoadingAiRec.value = true
            _aiRecommendationResponse.value = ""
            val result = GeminiMusicService.getAiRockSuggestions(_aiRecommendationQuery.value)
            _aiRecommendationResponse.value = result
            _isLoadingAiRec.value = false
        }
    }

    // --- Echo Find Execution ---
    fun startEchoFindScan() {
        if (_isEchoScanning.value) return
        _isEchoScanning.value = true
        _echoMatchedTrack.value = null

        viewModelScope.launch {
            // Emulate mic capture FFT audio wave animation
            delay(4000) // Scan for 4 seconds
            
            // Deliver actual Gemini matches, incorporating typed lyrics or hummingbird hums
            val textToScan = _micHumQuery.value.ifBlank { "Heavy guitar classic bass solo" }
            val result = GeminiMusicService.recognizeTrackByLyrics(textToScan)
            _echoMatchedTrack.value = result
            _isEchoScanning.value = false
        }
    }

    // --- Spotify Playlists OAuth & Import Mocking ---
    fun triggerSpotifyImport() {
        _spotifyImportState.value = "authorizing"
        viewModelScope.launch {
            delay(2000) // Authorizing
            _spotifyImportState.value = "fetching"
            delay(1500) // Mapping imported Spotify tracks to local catalog
            
            // Map tracks in database as converted Spotify songs
            val songsToInsert = listOf(
                Track(
                    id = "spot_masterOfPuppets",
                    title = "Master of Puppets (Spotify Import)",
                    artist = "Metallica",
                    album = "Master of Puppets",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                    durationSeconds = 515,
                    albumArt = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                    isLocal = false
                ),
                Track(
                    id = "spot_stairwayToHeaven",
                    title = "Stairway to Heaven (Spotify Import)",
                    artist = "Led Zeppelin",
                    album = "Led Zeppelin IV",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                    durationSeconds = 482,
                    albumArt = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500&q=80",
                    isLocal = false
                )
            )
            
            repository.createPlaylist(
                Playlist(
                    id = "spotify_imported",
                    name = "My Spotify Import",
                    description = "Successfully mapped from Spotify Web API OAuth."
                )
            )
            
            songsToInsert.forEach {
                // Add to track system
                // First insert track
                delay(10)
            }
            _importedTracksCount.value = 2
            _spotifyImportState.value = "success"
        }
    }

    fun resetSpotifyState() {
        _spotifyImportState.value = "idle"
        _importedTracksCount.value = 0
    }

    // --- Listen Together Rooms Sync Logic ---
    fun createListenRoom() {
        val id = (100000..999999).random().toString()
        _currentSession.value = ListenTogetherSession(
            roomId = id,
            isHost = true,
            members = listOf("You (Host)")
        )
        // Set listening room feedback
        viewModelScope.launch {
            delay(2000)
            _currentSession.value = _currentSession.value?.copy(
                members = listOf("You (Host)", "RockBuddy_54", "GuitarHero_2"),
                recentMessage = "RockBuddy_54 joined the room!"
            )
        }
    }

    fun joinListenRoom() {
        val code = _roomCodeInput.value
        if (code.length < 4) return
        _currentSession.value = ListenTogetherSession(
            roomId = code,
            isHost = false,
            members = listOf("Host (SlashFan)", "You")
        )
        // Autoplay host's song "Born to Speed" in sync!
        viewModelScope.launch {
            val track = repository.getTrackById("born_to_speed")
            if (track != null) {
                audioController.playTrack(track)
            }
            delay(2000)
            _currentSession.value = _currentSession.value?.copy(
                recentMessage = "Host paused playback."
            )
            audioController.togglePlayPause() // Pause in sync with host
        }
    }

    fun leaveListenRoom() {
        _currentSession.value = null
        _roomCodeInput.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        audioController.release()
    }
}
