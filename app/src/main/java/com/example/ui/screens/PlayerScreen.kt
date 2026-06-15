package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.audio.PlaybackState
import com.example.ui.MusicViewModel
import com.example.ui.components.GlowEqualizerBars
import com.example.ui.components.SpinningVinyl
import com.example.ui.components.SyncedLyricsView
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.audioController.currentTrack.collectAsState()
    val playbackState by viewModel.audioController.playbackState.collectAsState()
    val currentPosition by viewModel.audioController.currentPosition.collectAsState()
    val duration by viewModel.audioController.trackDuration.collectAsState()
    val speed by viewModel.audioController.playbackSpeed.collectAsState()
    val sleepTimerMins by viewModel.audioController.sleepTimerMinutesLeft.collectAsState()

    val isPlaying = playbackState is PlaybackState.Playing

    // --- Interactive Shifting Ambient Background Gradient (BPM style) ---
    val infiniteTransition = rememberInfiniteTransition(label = "player_bg")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    // Dynamic color shifting based on track title length
    val color1 by animateColorAsState(
        targetValue = if (currentTrack?.title?.length ?: 0 > 12) RockPrimary.copy(alpha = 0.25f) else RockTertiary.copy(alpha = 0.25f),
        animationSpec = tween(1500), label = "color1"
    )
    val color2 by animateColorAsState(
        targetValue = if (currentTrack?.title?.length ?: 0 > 12) RockSecondary.copy(alpha = 0.2f) else RockPrimary.copy(alpha = 0.2f),
        animationSpec = tween(1500), label = "color2"
    )

    // State Dialog Toggles
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Swipe back-and-forth track gesture skip triggers
    var dragOffsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06070B))
            // Swipe gesture for skipping tracks
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragOffsetX > 150f) {
                            viewModel.audioController.playPrevious()
                        } else if (dragOffsetX < -150f) {
                            viewModel.audioController.playNext()
                        }
                        dragOffsetX = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                    }
                )
            }
    ) {
        // Ambient blurring shifting glow background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color1, color2, Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(gradientShift % size.width, size.height / 2f),
                        radius = 800f
                    )
                )
        )

        if (currentTrack == null) {
            // Empty / Select Player State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = RockPrimary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select a Rock Anthem",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Browse Home or Search to blast the riffs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RockMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val track = currentTrack!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.testTag("minimize_player_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Minimize Screen",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLAYING FROM THE GRIT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = RockSecondary
                        )
                        Text(
                            text = track.album,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp,
                            color = RockMuted
                        )
                    }

                    var isFavorite by remember(track.id) { mutableStateOf(track.isFavorite) }
                    IconButton(
                        onClick = {
                            isFavorite = !isFavorite
                            viewModel.toggleFavorite(track.id, isFavorite)
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite track",
                            tint = if (isFavorite) RockPrimary else Color.White
                        )
                    }
                }

                // Mid Immersive Section - Spinning CD/Vinyl OR Synced Lyrics
                var showLyricsMode by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showLyricsMode) {
                        // IMMERSIVE SYNCD KARAOKE LYRICS
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showLyricsMode = false },
                            contentAlignment = Alignment.Center
                        ) {
                            SyncedLyricsView(
                                lrcContent = track.lyricsLrc,
                                currentPositionMs = currentPosition
                            )
                        }
                    } else {
                        // PREMIUM SPINNING CD / VINYL COVER
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable { showLyricsMode = true }
                        ) {
                            SpinningVinyl(
                                imageUrl = track.albumArt,
                                isPlaying = isPlaying,
                                modifier = Modifier
                                    .size(260.dp)
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // Small floating tag prompting lyrics toggle
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = RockTransparentCard,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = null,
                                        tint = RockSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tap to show Lyrics",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Neon equalizes visualizer overlay at bottom of midsection
                GlowEqualizerBars(
                    isPlaying = isPlaying,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Track Title and Artist Credits
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = track.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = RockSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PROGRESS TIMELINE SLIDER
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = { viewModel.audioController.seekTo(it.toLong()) },
                        valueRange = 0f..(if (duration > 0) duration.toFloat() else 100f),
                        colors = SliderDefaults.colors(
                            thumbColor = RockSecondary,
                            activeTrackColor = RockPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = RockMuted
                        )
                        Text(
                            text = formatTime(duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = RockMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // MEDIA INTERACTION BUTTON PANEL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Sleep Timer option
                    IconButton(onClick = { showTimerDialog = true }) {
                        Icon(
                            imageVector = if (sleepTimerMins != null) Icons.Filled.Timer else Icons.Outlined.Timer,
                            contentDescription = "Sleep timer setup",
                            tint = if (sleepTimerMins != null) RockSecondary else Color.White
                        )
                    }

                    // 2. Backward track
                    IconButton(
                        onClick = { viewModel.audioController.playPrevious() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 3. Highlighted neon play/pause container
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(RockPrimary, RockSecondary)
                                )
                            )
                            .clickable { viewModel.audioController.togglePlayPause() }
                            .testTag("player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play or Pause track",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // 4. Forward track
                    IconButton(
                        onClick = { viewModel.audioController.playNext() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 5. Playback Speed Selector Option
                    IconButton(onClick = { showSpeedDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = "Select playback speed",
                            tint = if (speed != 1.0f) RockSecondary else Color.White
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS FOR SPEED AND SLEEP TIMER ---
    if (showSpeedDialog) {
        Dialog(onDismissRequest = { showSpeedDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = RockSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Playback Speed Control",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { actualSpeed ->
                        TextButton(
                            onClick = {
                                viewModel.audioController.setSpeed(actualSpeed)
                                showSpeedDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${actualSpeed}x ${if (actualSpeed == 1.0f) "(Standard)" else ""}",
                                color = if (speed == actualSpeed) RockSecondary else Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTimerDialog) {
        Dialog(onDismissRequest = { showTimerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = RockSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Set Sleep Timer",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sleepTimerMins != null) {
                        Text(
                            text = "Active clock: $sleepTimerMins min remaining",
                            color = RockSecondary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf(5, 15, 30, 45, 60).forEach { mins ->
                        TextButton(
                            onClick = {
                                viewModel.audioController.startSleepTimer(mins)
                                showTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "$mins Minutes", color = Color.White)
                        }
                    }
                    if (sleepTimerMins != null) {
                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                        TextButton(
                            onClick = {
                                viewModel.audioController.cancelSleepTimer()
                                showTimerDialog = false
                            }
                        ) {
                            Text(text = "CANCEL SLEEP TIMER", color = RockPrimary)
                        }
                    }
                }
            }
        }
    }
}

// Format MS to simple minutes and seconds text
fun formatTime(timeMs: Long): String {
    val totalSecs = timeMs / 1000
    val minutes = totalSecs / 60
    val seconds = totalSecs % 60
    return String.format("%02d:%02d", minutes, seconds)
}
