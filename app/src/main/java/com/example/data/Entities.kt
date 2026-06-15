package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "tracks")
@Serializable
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val url: String,
    val durationSeconds: Int,
    val albumArt: String, // Resource name or URL
    val isLocal: Boolean = false,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val lyricsLrc: String? = null // LRC format lyrics
)

@Entity(tableName = "playlists")
@Serializable
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val coverArtUrl: String? = null,
    val isSystemCreated: Boolean = false
)

@Entity(tableName = "playlist_tracks")
data class PlaylistTrack(
    @PrimaryKey(autoGenerate = true) val entryId: Int = 0,
    val playlistId: String,
    val trackId: String
)

@Entity(tableName = "podcasts")
@Serializable
data class Podcast(
    @PrimaryKey val id: String,
    val title: String,
    val publisher: String,
    val description: String,
    val feedUrl: String,
    val imageUrl: String
)

@Entity(tableName = "podcast_episodes")
@Serializable
data class PodcastEpisode(
    @PrimaryKey val id: String,
    val podcastId: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val publishDate: String,
    val isDownloaded: Boolean = false,
    val playbackProgressSeconds: Int = 0
)
