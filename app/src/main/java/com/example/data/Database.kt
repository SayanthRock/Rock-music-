package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1")
    fun getFavoriteTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1")
    fun getDownloadedTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isLocal = 1")
    fun getLocalTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: String): Track?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Update
    suspend fun updateTrack(track: Track)

    @Query("UPDATE tracks SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)

    @Query("UPDATE tracks SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean)

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchTracks(query: String): Flow<List<Track>>
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrack)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)

    @Query("""
        SELECT t.* FROM tracks t 
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId 
        WHERE pt.playlistId = :playlistId
    """)
    fun getTracksForPlaylist(playlistId: String): Flow<List<Track>>
}

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcasts")
    fun getAllPodcasts(): Flow<List<Podcast>>

    @Query("SELECT * FROM podcast_episodes WHERE podcastId = :podcastId ORDER BY publishDate DESC")
    fun getEpisodesForPodcast(podcastId: String): Flow<List<PodcastEpisode>>

    @Query("SELECT * FROM podcast_episodes WHERE isDownloaded = 1")
    fun getDownloadedEpisodes(): Flow<List<PodcastEpisode>>

    @Query("SELECT * FROM podcast_episodes WHERE id = :episodeId")
    suspend fun getEpisodeById(episodeId: String): PodcastEpisode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcasts(podcasts: List<Podcast>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<PodcastEpisode>)

    @Update
    suspend fun updateEpisode(episode: PodcastEpisode)
}

@Database(
    entities = [Track::class, Playlist::class, PlaylistTrack::class, Podcast::class, PodcastEpisode::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun podcastDao(): PodcastDao
}
