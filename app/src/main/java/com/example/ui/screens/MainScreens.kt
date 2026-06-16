package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.Playlist
import com.example.data.Track
import com.example.ui.MusicViewModel
import com.example.ui.Screen
import com.example.ui.components.AudioWaveformRadar
import com.example.ui.theme.*

// ==================== 1. HOME SCREEN ====================
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val tracks by viewModel.allTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val aiQuery by viewModel.aiRecommendationQuery.collectAsState()
    val aiResponse by viewModel.aiRecommendationResponse.collectAsState()
    val isLoadingAi by viewModel.isLoadingAiRec.collectAsState()

    val currentPlayingTrack by viewModel.audioController.currentTrack.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 170.dp)
    ) {
        // Welcome Header & Neon Glow Pitch
        item {
            Column {
                Text(
                    text = "READY TO RIFF",
                    style = MaterialTheme.typography.labelSmall,
                    color = RockSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Rock Music",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }

        // Spotlight Smart Recommendations Row (with real tracks)
        item {
            Text(
                text = "Spotlight Discovery",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RockPrimary)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tracks.take(3)) { track ->
                        DiscoveryCard(track = track, onClick = {
                            viewModel.playTrackList(tracks, tracks.indexOf(track))
                        })
                    }
                }
            }
        }

        // Mood Playlists (Horizontal Grid)
        item {
            Text(
                text = "Mood Playlists",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(playlists) { playlist ->
                    MoodPlaylistCard(playlist = playlist, onClick = {
                        viewModel.navigateTo(Screen.PlaylistDetails(playlist))
                    })
                }
            }
        }

        // AI ROCK DJ DISCOVERY INPUT
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RockSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(RockGlassPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = RockPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI Smart discovery DJ",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Describe your mood (e.g. 'driving on highway 101 at sunset') and let Gemini select custom rock suggestions.",
                        fontSize = 12.sp,
                        color = RockMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = aiQuery,
                            onValueChange = { viewModel.onAiQueryChanged(it) },
                            placeholder = { Text("How's your mood?", fontSize = 13.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                focusedIndicatorColor = RockSecondary,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_dj_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.loadAiRecommendations() },
                            colors = ButtonDefaults.buttonColors(containerColor = RockSecondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("ai_dj_submit_btn")
                        ) {
                            if (isLoadingAi) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text("Ask", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (aiResponse.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = aiResponse,
                                fontSize = 13.sp,
                                color = RockOnBackground,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Continue Listening / Recently Played Row List
        item {
            Text(
                text = "Recently Blast list",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(tracks) { track ->
            TrackListItem(
                track = track,
                isPlaying = currentPlayingTrack?.id == track.id,
                onClick = {
                    viewModel.playTrackList(tracks, tracks.indexOf(track))
                },
                onDownloadClick = {
                    viewModel.toggleDownload(track.id, !track.isDownloaded)
                }
            )
        }
    }
}

// ==================== 2. SEARCH SCREEN ====================
@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchedTracks.collectAsState()
    val spotifyState by viewModel.spotifyImportState.collectAsState()
    val spotifyImportCount by viewModel.importedTracksCount.collectAsState()

    val currentPlayingTrack by viewModel.audioController.currentTrack.collectAsState()

    var showSpotifyImportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 170.dp)
    ) {
        item {
            Text(
                text = "Discover Riffs",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }

        // Search Bar Input
        item {
            TextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = RockMuted) },
                placeholder = { Text("Search songs, artists, high tension riffs...", color = RockMuted) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = RockSurface,
                    unfocusedContainerColor = RockSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = RockPrimary,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field_input")
            )
        }

        // SPOTIFY WEB API OAUTH HIGHLIGHT CARD
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RockTransparentCard),
                modifier = Modifier
                    .fillModifierWithGlow()
                    .clickable { showSpotifyImportDialog = true }
                    .testTag("spotify_oauth_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1DB954), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDownload,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Import Spotify Playlist",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Log in via OAuth to sync real rock playlists.",
                            color = RockMuted,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = RockMuted
                    )
                }
            }
        }

        // Results Divider
        item {
            Text(
                text = if (query.isEmpty()) "All Rock Tracks" else "Search Matches",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        if (results.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp), contentAlignment = Alignment.Center
                ) {
                    Text(text = "No guitar tracks match selection.", color = RockMuted, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(results) { track ->
                TrackListItem(
                    track = track,
                    isPlaying = currentPlayingTrack?.id == track.id,
                    onClick = {
                        viewModel.playTrackList(results, results.indexOf(track))
                    },
                    onDownloadClick = {
                        viewModel.toggleDownload(track.id, !track.isDownloaded)
                    }
                )
            }
        }
    }

    // --- SPOTIFY WEB API IMPORT DIALOGUE ---
    if (showSpotifyImportDialog) {
        Dialog(onDismissRequest = {
            showSpotifyImportDialog = false
            viewModel.resetSpotifyState()
        }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = RockSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Spotify Web API import",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unify Spotify's premium metadata. OAuth logins map Spotify tracks onto our cloud streams smoothly.",
                        fontSize = 13.sp,
                        color = RockMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    when (spotifyState) {
                        null, "idle" -> {
                            Button(
                                onClick = { viewModel.triggerSpotifyImport() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("spotify_trigger_oauth_btn")
                            ) {
                                Text("Connect via Spotify OAuth", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        "authorizing" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF1DB954))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Authorizing credentials...", color = Color.White, fontSize = 13.sp)
                            }
                        }
                        "fetching" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF1DB954))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Mapping tracks into cloud library...", color = Color.White, fontSize = 13.sp)
                            }
                        }
                        "success" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenColor, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Import Successful!", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Imported $spotifyImportCount playlist tracks.", color = RockMuted, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showSpotifyImportDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = RockPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Enjoy Tracks")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val GreenColor = Color(0xFF4CAF50)

// Helper extension modifier adding gentle background accent
fun Modifier.fillModifierWithGlow(): Modifier = this.shadow(4.dp, RoundedCornerShape(16.dp))

// ==================== 3. LIBRARY SCREEN ====================
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val localTracks by viewModel.localTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val favorites by viewModel.favoriteTracks.collectAsState()
    val currentPlayingTrack by viewModel.audioController.currentTrack.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Playlists, 1 = Favorites, 2 = Local Device Files

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 170.dp)
    ) {
        item {
            Text(
                text = "Library Base",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }

        // Tab selection pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Playlists", "Favorites", "Local Audio").forEachIndexed { index, name ->
                    val selected = activeTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) RockSecondary else RockSurface)
                            .clickable { activeTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else RockMuted
                        )
                    }
                }
            }
        }

        when (activeTab) {
            0 -> {
                // Playlists List
                if (playlists.isEmpty()) {
                    item {
                        EmptyStateWidget(
                            icon = Icons.Filled.QueueMusic,
                            title = "No Playlists Found",
                            tip = "Explore mood playlists or connect Spotify to generate lists."
                        )
                    }
                } else {
                    items(playlists) { playlist ->
                        PlaylistRowCard(playlist = playlist, onClick = {
                            viewModel.navigateTo(Screen.PlaylistDetails(playlist))
                        })
                    }
                }
            }
            1 -> {
                // Favorites List
                if (favorites.isEmpty()) {
                    item {
                        EmptyStateWidget(
                            icon = Icons.Filled.FavoriteBorder,
                            title = "No Liked Tracks",
                            tip = "Click the heart on any player screen to register favorites."
                        )
                    }
                } else {
                    items(favorites) { track ->
                        TrackListItem(
                            track = track,
                            isPlaying = currentPlayingTrack?.id == track.id,
                            onClick = {
                                viewModel.playTrackList(favorites, favorites.indexOf(track))
                            },
                            onDownloadClick = {
                                viewModel.toggleDownload(track.id, !track.isDownloaded)
                            }
                        )
                    }
                }
            }
            2 -> {
                // Local Audio Scan
                item {
                    Button(
                        onClick = { /* Media Store Scanner simulated in viewmodel populate */ },
                        colors = ButtonDefaults.buttonColors(containerColor = RockPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_local_media_btn")
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan device music (MediaStore)", fontWeight = FontWeight.Bold)
                    }
                }
                if (localTracks.isEmpty()) {
                    item {
                        EmptyStateWidget(
                            icon = Icons.Filled.FolderOpen,
                            title = "No local tracks mapped",
                            tip = "Connect device storage or sync download folders to display file tracks."
                        )
                    }
                } else {
                    items(localTracks) { track ->
                        TrackListItem(
                            track = track,
                            isPlaying = currentPlayingTrack?.id == track.id,
                            onClick = {
                                viewModel.playTrackList(localTracks, localTracks.indexOf(track))
                            },
                            onDownloadClick = {
                                viewModel.toggleDownload(track.id, !track.isDownloaded)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==================== 4. DOWNLOADS SCREEN ====================
@Composable
fun DownloadsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloadedTracks.collectAsState()
    val currentPlayingTrack by viewModel.audioController.currentTrack.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            Column {
                Text(
                    text = "OFFLINE STORAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = RockSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }

        if (downloads.isEmpty()) {
            item {
                EmptyStateWidget(
                    icon = Icons.Filled.CloudOff,
                    title = "Offline Vault is empty",
                    tip = "Click the download button on any track item to save files for offline flight playlists."
                )
            }
        } else {
            items(downloads) { track ->
                TrackListItem(
                    track = track,
                    isPlaying = currentPlayingTrack?.id == track.id,
                    onClick = {
                        viewModel.playTrackList(downloads, downloads.indexOf(track))
                    },
                    onDownloadClick = {
                        viewModel.toggleDownload(track.id, !track.isDownloaded)
                    }
                )
            }
        }
    }
}

// ==================== 5. SETTINGS SCREEN ====================
@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val discordEnabled by viewModel.isDiscordPresenceEnabled.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 170.dp)
    ) {
        item {
            Text(
                text = "Engine Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RockSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DISCORD RICH PRESENCE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RockSecondary
                    )
                    Text(
                        text = "Sync active song tracks, artists, and live album art onto your Discord profile status.",
                        fontSize = 12.sp,
                        color = RockMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Status on Discord", color = Color.White, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = discordEnabled,
                            onCheckedChange = { viewModel.toggleDiscordPresence(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RockSecondary,
                                checkedTrackColor = RockPrimary
                            ),
                            modifier = Modifier.testTag("discord_switch_toggle")
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RockSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SYSTEM ARCHITECTURE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RockPrimary
                    )
                    SettingsInfoRow(label = "Platform", value = "Android JVM Client")
                    SettingsInfoRow(label = "Audio Core Engine", value = "Media3 ExoPlayer Wrapper")
                    SettingsInfoRow(label = "Local Persistence", value = "SQLite / Room Database")
                    SettingsInfoRow(label = "Integration Hub", value = "WebSocket sync server")
                }
            }
        }
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = RockMuted, fontSize = 13.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

// ==================== 6. ECHO FIND SCREEN ====================
@Composable
fun EchoFindScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val isScanning by viewModel.isEchoScanning.collectAsState()
    val matchResult by viewModel.echoMatchedTrack.collectAsState()
    val micQuery by viewModel.micHumQuery.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 170.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "SONG IDENTIFICATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = RockSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Echo Find",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }

        // Radar Animated Ring Scanner Component
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clickable { viewModel.startEchoFindScan() }
                    .testTag("echo_find_radar_btn"),
                contentAlignment = Alignment.Center
            ) {
                AudioWaveformRadar(
                    isScanning = isScanning,
                    modifier = Modifier.fillMaxSize()
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isScanning) Icons.Filled.MicNone else Icons.Filled.Mic,
                        contentDescription = "Microphone Scanner trigger",
                        tint = if (isScanning) RockSecondary else Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isScanning) "Listening..." else "Tap to Echo",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Optional Syllables vocal humming hint input
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RockSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vocal Hum Assistance (Optional)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Type vocal phrases/syllables or guitar humming description (e.g., 'heavy snare beat then duh duh boom boom') before clicking search to guide Gemini.",
                        fontSize = 11.sp,
                        color = RockMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = micQuery,
                        onValueChange = { viewModel.onMicHumQueryChanged(it) },
                        placeholder = { Text("E.g: Energetic guitar solo and deep baseline...", fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = RockPrimary,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mic_assistance_input")
                    )
                }
            }
        }

        // Match Result Block
        item {
            AnimatedVisibility(
                visible = matchResult != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                matchResult?.let { result ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RockSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(RockGlassSecondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WifiTethering,
                                        contentDescription = null,
                                        tint = RockSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "MATCH ACQUIRED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RockSecondary
                                    )
                                    Text(
                                        text = result.title,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "by ${result.artist}",
                                        fontSize = 14.sp,
                                        color = RockPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Divider(
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.padding(vertical = 14.dp)
                            )
                            Text(
                                text = "Album details: ${result.album} (${result.year})",
                                fontSize = 12.sp,
                                color = RockMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Song Trivia: ${result.trivia}",
                                fontSize = 13.sp,
                                color = RockOnBackground,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== 7. LISTEN TOGETHER SCREEN ====================
@Composable
fun ListenTogetherScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.currentSession.collectAsState()
    val roomCode by viewModel.roomCodeInput.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 170.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SOCIAL SYNC PLAYBACK",
                    style = MaterialTheme.typography.labelSmall,
                    color = RockSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Listen Together",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }

        if (session == null) {
            // Room options
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RockSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Host Playback Room",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Create a synchronized listener room. Any play, pause, or skip commands on your player will instantly propagate to connected users.",
                            fontSize = 12.sp,
                            color = RockMuted
                        )
                        Button(
                            onClick = { viewModel.createListenRoom() },
                            colors = ButtonDefaults.buttonColors(containerColor = RockPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_room_btn")
                        ) {
                            Text("Create Synced Room", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RockSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Join Playback Room",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Type the 6-digit room code generated by your host to sync audio playback, tracks, and reaction chat feeds.",
                            fontSize = 12.sp,
                            color = RockMuted
                        )
                        TextField(
                            value = roomCode,
                            onValueChange = { viewModel.onRoomCodeInputChanged(it) },
                            placeholder = { Text("E.g: 582914", color = RockMuted) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = RockSecondary,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("room_code_input")
                        )
                        Button(
                            onClick = { viewModel.joinListenRoom() },
                            colors = ButtonDefaults.buttonColors(containerColor = RockSecondary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("join_room_btn")
                        ) {
                            Text("Join Room Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            val active = session!!
            // Member list syncing room
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RockSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = if (active.isHost) "HOSTING ROOM" else "SYNC MEMBER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RockSecondary
                                )
                                Text(
                                    text = "Room ID: ${active.roomId}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { viewModel.leaveListenRoom() }) {
                                Icon(Icons.Filled.ExitToApp, contentDescription = "Exit Room", tint = RockPrimary)
                            }
                        }

                        Divider(
                            color = Color.White.copy(alpha = 0.10f),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Text(
                            text = "Connected Sync Members (${active.members.size})",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        active.members.forEach { name ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(GreenColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = name, color = RockOnBackground, fontSize = 13.sp)
                            }
                        }

                        active.recentMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RockGlassPrimary, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = RockPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 8. PLAYLIST DETAIL SCREEN ====================
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPlayingTrack by viewModel.audioController.currentTrack.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    
    // Simple filter tracks relating only to this playlist
    val tracks = remember(playlist.id, allTracks) {
        when (playlist.id) {
            "mood_high_octane" -> allTracks.filter { it.id == "born_to_speed" || it.id == "shadow_alley" }
            "mood_grunge" -> allTracks.filter { it.id == "electric_rain" }
            "mood_space_rock" -> allTracks.filter { it.id == "lunar_reactor" }
            else -> allTracks
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 170.dp)
    ) {
        item {
            IconButton(onClick = onBack, modifier = Modifier.testTag("playlist_back_btn")) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = playlist.coverArtUrl ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "ROCK GENRE PLAYLIST",
                        style = MaterialTheme.typography.labelSmall,
                        color = RockPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = playlist.description,
                        color = RockMuted,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Button(
                onClick = { if (tracks.isNotEmpty()) viewModel.playTrackList(tracks, 0) },
                colors = ButtonDefaults.buttonColors(containerColor = RockSecondary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playlist_play_cascade_btn")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stream Sync Session", fontWeight = FontWeight.Bold)
            }
        }

        if (tracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp), contentAlignment = Alignment.Center
                ) {
                    Text(text = "Empty Playlist", color = RockMuted)
                }
            }
        } else {
            items(tracks) { track ->
                TrackListItem(
                    track = track,
                    isPlaying = currentPlayingTrack?.id == track.id,
                    onClick = {
                        viewModel.playTrackList(tracks, tracks.indexOf(track))
                    },
                    onDownloadClick = {
                        viewModel.toggleDownload(track.id, !track.isDownloaded)
                    }
                )
            }
        }
    }
}

// ==================== SHARED LITTLE WIDGETS ====================

@Composable
fun DiscoveryCard(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RockSurface),
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = track.albumArt,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(136.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = track.title,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                fontSize = 11.sp,
                color = RockPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MoodPlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RockSurface),
        modifier = modifier
            .width(130.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = playlist.coverArtUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(106.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = playlist.name,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlaylistRowCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(RockSurface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = playlist.coverArtUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = playlist.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Text(text = playlist.description, color = RockMuted, fontSize = 11.sp, maxLines = 1)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = RockMuted)
    }
}

@Composable
fun TrackListItem(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.albumArt,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) RockSecondary else Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = RockMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (track.isLocal) {
            Icon(
                imageVector = Icons.Filled.SdCard,
                contentDescription = "Local storage song",
                tint = RockMuted,
                modifier = Modifier.size(16.dp).padding(horizontal = 4.dp)
            )
        }
        IconButton(onClick = onDownloadClick) {
            Icon(
                imageVector = if (track.isDownloaded) Icons.Filled.CloudDone else Icons.Outlined.CloudDownload,
                contentDescription = "Download track",
                tint = if (track.isDownloaded) RockSecondary else RockMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyStateWidget(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tip: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = RockMuted, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        Text(text = tip, color = RockMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
    }
}
