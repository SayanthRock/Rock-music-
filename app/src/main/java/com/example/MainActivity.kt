package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.PlaybackState
import com.example.ui.MusicViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayoutContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainLayoutContainer(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentTrack by viewModel.audioController.currentTrack.collectAsState()
    val playbackState by viewModel.audioController.playbackState.collectAsState()

    val isPlaying = playbackState is PlaybackState.Playing

    // Expanded player view state
    var isPlayerExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(RockBackground),
        // Draw the bottom navigation bar
        bottomBar = {
            Column {
                // Persistent Floating Mini-Player (slides up above bottom bar when song loaded)
                AnimatedVisibility(
                    visible = currentTrack != null && !isPlayerExpanded,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    currentTrack?.let { track ->
                        MiniMusicPlayer(
                            title = track.title,
                            artist = track.artist,
                            albumArt = track.albumArt,
                            isPlaying = isPlaying,
                            onPlayPauseToggle = { viewModel.audioController.togglePlayPause() },
                            onNext = { viewModel.audioController.playNext() },
                            onExpandClick = { isPlayerExpanded = true }
                        )
                    }
                }

                // Standard Material Design 3 Bottom Navigation items
                NavigationBar(
                    containerColor = RockSurface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("app_bottom_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Home,
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RockSecondary,
                            indicatorColor = RockPrimary,
                            unselectedIconColor = RockMuted,
                            unselectedTextColor = RockMuted
                        ),
                        modifier = Modifier.testTag("nav_home_tab")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Search,
                        onClick = { viewModel.navigateTo(Screen.Search) },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RockSecondary,
                            indicatorColor = RockPrimary,
                            unselectedIconColor = RockMuted,
                            unselectedTextColor = RockMuted
                        ),
                        modifier = Modifier.testTag("nav_search_tab")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.EchoFind,
                        onClick = { viewModel.navigateTo(Screen.EchoFind) },
                        icon = { Icon(Icons.Filled.Mic, contentDescription = "Echo Find") },
                        label = { Text("Echo Find", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RockSecondary,
                            indicatorColor = RockPrimary,
                            unselectedIconColor = RockMuted,
                            unselectedTextColor = RockMuted
                        ),
                        modifier = Modifier.testTag("nav_echofind_tab")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.ListenTogether,
                        onClick = { viewModel.navigateTo(Screen.ListenTogether) },
                        icon = { Icon(Icons.Filled.RecordVoiceOver, contentDescription = "GroupSync") },
                        label = { Text("Sync Live", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RockSecondary,
                            indicatorColor = RockPrimary,
                            unselectedIconColor = RockMuted,
                            unselectedTextColor = RockMuted
                        ),
                        modifier = Modifier.testTag("nav_synclive_tab")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Library || currentScreen is Screen.Downloads || currentScreen is Screen.Settings,
                        onClick = { viewModel.navigateTo(Screen.Library) },
                        icon = { Icon(Icons.Filled.QueueMusic, contentDescription = "Library") },
                        label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RockSecondary,
                            indicatorColor = RockPrimary,
                            unselectedIconColor = RockMuted,
                            unselectedTextColor = RockMuted
                        ),
                        modifier = Modifier.testTag("nav_library_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Draw active routing screen frame
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "screen_routing"
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.Home -> HomeScreen(viewModel = viewModel)
                    is Screen.Search -> SearchScreen(viewModel = viewModel)
                    is Screen.EchoFind -> EchoFindScreen(viewModel = viewModel)
                    is Screen.ListenTogether -> ListenTogetherScreen(viewModel = viewModel)
                    is Screen.Library -> LibraryScreen(viewModel = viewModel)
                    is Screen.Downloads -> DownloadsScreen(viewModel = viewModel)
                    is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                    is Screen.PlaylistDetails -> PlaylistDetailScreen(
                        playlist = targetScreen.playlist,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Library) }
                    )
                }
            }

            // Quick Floating Hotkeys for Downloads and Settings
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Downloads) },
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .background(RockSurface, CircleShape)
                        .size(40.dp)
                        .testTag("top_navigation_downloads_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = "Downloads Panel",
                        tint = RockSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Settings) },
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .background(RockSurface, CircleShape)
                        .size(40.dp)
                        .testTag("top_navigation_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings Panel",
                        tint = RockPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Fullscreen Immersive sliding overlay Player Screen
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(400)),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(400))
            ) {
                PlayerScreen(
                    viewModel = viewModel,
                    onMinimize = { isPlayerExpanded = false }
                )
            }
        }
    }
}

// Custom Compact Minibar Player shown at bottom above persistent nav bar
@Composable
fun MiniMusicPlayer(
    title: String,
    artist: String,
    albumArt: String,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = RockSurface,
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onExpandClick() }
            .testTag("mini_player_surface")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = albumArt,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist,
                        color = RockSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Play / Pause Mini button
                IconButton(onClick = onPlayPauseToggle, modifier = Modifier.testTag("mini_player_play_pause_btn")) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play or Pause music",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Track skip forward button
                IconButton(onClick = onNext, modifier = Modifier.testTag("mini_player_next_btn")) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Skip next track",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
