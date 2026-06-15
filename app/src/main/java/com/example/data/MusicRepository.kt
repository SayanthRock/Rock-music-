package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {
    private val database: MusicDatabase = Room.databaseBuilder(
        context.applicationContext,
        MusicDatabase::class.java,
        "rock_music_db"
    ).build()

    private val trackDao = database.trackDao()
    private val playlistDao = database.playlistDao()
    private val podcastDao = database.podcastDao()

    init {
        // Run seed in a background thread on creation
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialData()
        }
    }

    // --- Tracks ---
    fun getAllTracks(): Flow<List<Track>> = trackDao.getAllTracks()
    fun getFavoriteTracks(): Flow<List<Track>> = trackDao.getFavoriteTracks()
    fun getDownloadedTracks(): Flow<List<Track>> = trackDao.getDownloadedTracks()
    fun getLocalTracks(): Flow<List<Track>> = trackDao.getLocalTracks()
    fun searchTracks(query: String): Flow<List<Track>> = trackDao.searchTracks(query)
    suspend fun getTrackById(id: String): Track? = trackDao.getTrackById(id)
    suspend fun updateTrack(track: Track) = trackDao.updateTrack(track)
    suspend fun toggleFavorite(id: String, isFav: Boolean) = trackDao.updateFavorite(id, isFav)
    suspend fun toggleDownload(id: String, isDownloaded: Boolean) = trackDao.updateDownloadStatus(id, isDownloaded)

    // --- Playlists ---
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    fun getTracksForPlaylist(playlistId: String): Flow<List<Track>> = playlistDao.getTracksForPlaylist(playlistId)
    suspend fun createPlaylist(playlist: Playlist) = playlistDao.insertPlaylist(playlist)
    suspend fun deletePlaylist(playlistId: String) = playlistDao.deletePlaylist(playlistId)
    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        playlistDao.insertPlaylistTrack(PlaylistTrack(playlistId = playlistId, trackId = trackId))
    }
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    // --- Podcasts ---
    fun getAllPodcasts(): Flow<List<Podcast>> = podcastDao.getAllPodcasts()
    fun getEpisodesForPodcast(podcastId: String): Flow<List<PodcastEpisode>> = podcastDao.getEpisodesForPodcast(podcastId)
    fun getDownloadedEpisodes(): Flow<List<PodcastEpisode>> = podcastDao.getDownloadedEpisodes()
    suspend fun getEpisodeById(episodeId: String): PodcastEpisode? = podcastDao.getEpisodeById(episodeId)
    suspend fun updateEpisode(episode: PodcastEpisode) = podcastDao.updateEpisode(episode)

    private suspend fun seedInitialData() = withContext(Dispatchers.IO) {
        // 1. Seed Tracks if none exist
        val existingTracks = trackDao.getAllTracks().firstOrNull()
        if (existingTracks.isNullOrEmpty()) {
            val seeds = listOf(
                Track(
                    id = "born_to_speed",
                    title = "Born to Speed",
                    artist = "The Thunderbolts",
                    album = "High Voltage Grit",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    durationSeconds = 372,
                    albumArt = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                    isLocal = false,
                    lyricsLrc = """
                        [00:00.00] Born to Speed by The Thunderbolts
                        [00:03.00] Revving engines in a desert land
                        [00:07.00] Steel and steel we take a final stand
                        [00:11.00] Riding fast through the canyon gale
                        [00:15.00] Got a rocket on a rusty trail
                        [00:19.00] Heat is rising on the neon street
                        [00:23.00] Feel the friction of the metal beat
                        [00:27.00] Oh! Born to speed, tonight we fly!
                        [00:31.00] Under the cosmic crimson sky!
                        [00:35.00] Put your foot down, feel the flame!
                        [00:39.00] Rock is the fury, rock is our name!
                    """.trimIndent()
                ),
                Track(
                    id = "shadow_alley",
                    title = "Shadow Alley",
                    artist = "Slate & Steel",
                    album = "Heavy Echoes",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    durationSeconds = 423,
                    albumArt = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500&q=80",
                    isLocal = false,
                    lyricsLrc = """
                        [00:00.00] Shadow Alley by Slate & Steel
                        [00:04.00] Cold rain falling in the concrete trace
                        [00:08.00] Whispers screaming on a frozen face
                        [00:12.00] We are the ghosts of the neon light
                        [00:16.00] Searching for the truth of the blackest night
                        [00:20.00] Run with the shadows, breakout today
                        [00:24.00] Heavy echoes of the highway play
                        [00:28.00] Over the limits, tearing the lock
                        [00:32.00] This is the spirit, this is the Rock!
                    """.trimIndent()
                ),
                Track(
                    id = "lunar_reactor",
                    title = "Lunar Reactor",
                    artist = "Vapor Orbit",
                    album = "Cybernetic Space",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    durationSeconds = 302,
                    albumArt = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                    isLocal = true, // Set as local device sound
                    lyricsLrc = """
                        [00:00.00] Lunar Reactor by Vapor Orbit
                        [00:03.00] Nuclear fusion in a solar sphere
                        [00:07.00] Gravity pulls but we have no fear
                        [00:11.00] Floating far where the static rings
                        [00:15.00] Cybernetic guitars spreading atomic wings
                        [00:19.00] Reactor core is glowing bright
                        [00:23.00] Progressive riffs take flight tonight
                        [00:27.00] Pulsing synth and heavy distortion clash
                        [00:31.00] Watch the digital lunar structure crash!
                    """.trimIndent()
                ),
                Track(
                    id = "electric_rain",
                    title = "Electric Rain",
                    artist = "The Voltage Kings",
                    album = "Thunderous Storms",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    durationSeconds = 390,
                    albumArt = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80",
                    isLocal = false,
                    lyricsLrc = """
                        [00:00.00] Electric Rain by The Voltage Kings
                        [00:04.00] Lightning strikes the radio tower
                        [00:08.00] We've been waiting for the midnight hour
                        [00:12.00] Power sparks and chords collide
                        [00:16.00] Surfing on an electric tide
                        [00:20.00] Let it pour, let the lightning shock
                        [00:24.00] Electric rain will forever rock!
                        [00:28.00] Crank the volume let the monitors blow
                        [00:32.00] Ride the voltage wherever we go!
                    """.trimIndent()
                )
            )
            trackDao.insertTracks(seeds)
        }

        // 2. Seed Playlists if none exist
        val existingPlaylists = playlistDao.getAllPlaylists().firstOrNull()
        if (existingPlaylists.isNullOrEmpty()) {
            val systemPlaylists = listOf(
                Playlist(
                    id = "mood_high_octane",
                    name = "High-Octane Rock",
                    description = "Pure adrenaline classic rock riffs for maximum speed.",
                    coverArtUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=500&q=80",
                    isSystemCreated = true
                ),
                Playlist(
                    id = "mood_grunge",
                    name = "Seattle Grunge Session",
                    description = "90s mood-based grungy, distorted masterpieces.",
                    coverArtUrl = "https://images.unsplash.com/photo-1549417229-aa67d3263c09?w=500&q=80",
                    isSystemCreated = true
                ),
                Playlist(
                    id = "mood_space_rock",
                    name = "Cosmic Prog Voyage",
                    description = "Atmospheric, space-themed progressive instrumentals.",
                    coverArtUrl = "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?w=500&q=80",
                    isSystemCreated = true
                )
            )
            systemPlaylists.forEach { playlistDao.insertPlaylist(it) }

            // Connect tracks to playlists
            playlistDao.insertPlaylistTrack(PlaylistTrack(playlistId = "mood_high_octane", trackId = "born_to_speed"))
            playlistDao.insertPlaylistTrack(PlaylistTrack(playlistId = "mood_high_octane", trackId = "shadow_alley"))
            playlistDao.insertPlaylistTrack(PlaylistTrack(playlistId = "mood_grunge", trackId = "electric_rain"))
            playlistDao.insertPlaylistTrack(PlaylistTrack(playlistId = "mood_space_rock", trackId = "lunar_reactor"))
        }

        // 3. Seed Podcasts if none exist
        val existingPodcasts = podcastDao.getAllPodcasts().firstOrNull()
        if (existingPodcasts.isNullOrEmpty()) {
            val rockPodcast = Podcast(
                id = "rock_history_chronicles",
                title = "Rock History Chronicles",
                publisher = "Dr. Vinyl & The Rock Academy",
                description = "Deep dives into the origins, gear, scandals, and epic stories of rock across generations.",
                feedUrl = "https://feeds.simplecast.com/rock-history-chronicles",
                imageUrl = "https://images.unsplash.com/photo-1481886156534-97af88ccb8e8?w=500&q=80"
            )
            podcastDao.insertPodcasts(listOf(rockPodcast))

            val episodes = listOf(
                PodcastEpisode(
                    id = "ep_birth_heavy_metal",
                    podcastId = "rock_history_chronicles",
                    title = "The Birth of Heavy Metal: Sabbath, Zeppelin & Purple",
                    description = "How three British bands created a darker, heavier sound that revolutionized rock in the late 60s and 70s.",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                    durationSeconds = 612,
                    publishDate = "2026-06-10"
                ),
                PodcastEpisode(
                    id = "ep_psychedelic_riffs",
                    podcastId = "rock_history_chronicles",
                    title = "Psychedelic Riffs and the Summer of Love",
                    description = "Analyzing the hypnotic guitar techniques of Jimi Hendrix, Pink Floyd, and Jefferson Airplane.",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                    durationSeconds = 548,
                    publishDate = "2026-06-01"
                )
            )
            podcastDao.insertEpisodes(episodes)
        }
    }
}
