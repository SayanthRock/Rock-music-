package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.RockPrimary
import com.example.ui.theme.RockSecondary
import com.example.ui.theme.RockTertiary
import kotlin.math.cos
import kotlin.math.sin

// --- 1. Neon Glowing Equalizer Bars ---
@Composable
fun GlowEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 12,
    barWidth: Dp = 6.dp,
    maxHeight: Dp = 60.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "eq")
        
        for (i in 0 until barCount) {
            // High efficiency: vary frequencies and speeds for each bar
            val duration = remember(i) { (400..900).random() }
            val delay = remember(i) { (0..300).random() }
            
            val barHeightFraction by if (isPlaying) {
                infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, delayMillis = delay, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_$i"
                )
            } else {
                remember { mutableStateOf(0.15f) }
            }
            
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(maxHeight * barHeightFraction)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                RockSecondary,
                                RockPrimary,
                                RockTertiary
                            )
                        )
                    )
            )
        }
    }
}

// --- 2. 3D Spinning Album Vinyl Record ---
@Composable
fun SpinningVinyl(
    imageUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotationAngle by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = modifier
            .shadow(16.dp, CircleShape)
            .background(Color(0xFF07070B), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Draw the Vinyl grooves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f
            
            // Outer Ring border
            drawCircle(color = Color(0xFF1E1E28), radius = outerRadius, style = Stroke(width = 1.dp.toPx()))
            
            // Multiple audio groove lines
            for (r in 3..14) {
                drawCircle(
                    color = Color(0xFF14141E).copy(alpha = 0.4f),
                    radius = outerRadius * (r / 15f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Shininess Specular gloss highlight lines of traditional vinyl
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = center,
                end = Offset(center.x + outerRadius, center.y + outerRadius),
                strokeWidth = 35.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = center,
                end = Offset(center.x - outerRadius, center.y - outerRadius),
                strokeWidth = 35.dp.toPx()
            )
        }

        // Central Album Cover
        AsyncImage(
            model = imageUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
            contentDescription = "Album Vinyl",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize(0.42f)
                .rotate(rotationAngle)
                .clip(CircleShape)
        )

        // Center needle hole
        Box(
            modifier = Modifier
                .fillMaxSize(0.08f)
                .background(Color.Black, CircleShape)
                .border(2.dp, Color(0xFF323242), CircleShape)
        )
    }
}

// --- 3. Dynamic Audio Waveform Radar (Echo Find Screen) ---
@Composable
fun AudioWaveformRadar(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    // Wave pulses expanding outward
    val pulse1 by if (isScanning) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "p1"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val pulse2 by if (isScanning) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, delayMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "p2"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Audio frequency oscillations
    val waveHeight by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = SineWaveEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "osc"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.width / 2f

            // 1. Draw radar radial pulses
            if (isScanning) {
                drawCircle(
                    color = RockPrimary.copy(alpha = 1f - pulse1),
                    radius = maxRadius * pulse1,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = RockSecondary.copy(alpha = 1f - pulse2),
                    radius = maxRadius * pulse2,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // 2. Solid center scanning radar ring
            drawCircle(
                color = RockPrimary.copy(alpha = 0.15f),
                radius = maxRadius * 0.45f
            )
            drawCircle(
                color = RockPrimary,
                radius = maxRadius * 0.45f,
                style = Stroke(width = 4.dp.toPx())
            )

            // 3. Audio reactive sinewave inside center circle
            val waveWidth = maxRadius * 0.7f
            val points = 32
            val step = waveWidth / points
            val startX = center.x - (waveWidth / 2f)

            for (i in 0 until points - 1) {
                val x1 = startX + i * step
                // Calculate responsive sinusoidal curve matching current active scan state
                val angleFactor = (i.toFloat() / points.toFloat()) * Math.PI * 3f
                val hFactor = if (isScanning) waveHeight else 4f
                val y1 = center.y + sin(angleFactor + (pulse1 * 10f)).toFloat() * hFactor

                val x2 = startX + (i + 1) * step
                val y2 = center.y + sin(angleFactor + step + (pulse1 * 10f)).toFloat() * hFactor

                drawLine(
                    color = Color.White,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// Custom Easing class simulating continuous wave
private val SineWaveEasing = Easing { fraction ->
    val t = fraction * Math.PI * 2
    ((sin(t) + 1) / 2).toFloat()
}

// --- 4. Synchronized Lyrics LRC Karaoke Engine ---
data class LyricLine(val timeMs: Long, val text: String)

@Composable
fun SyncedLyricsView(
    lrcContent: String?,
    currentPositionMs: Long,
    modifier: Modifier = Modifier
) {
    val lyricLines = remember(lrcContent) {
        parseLrc(lrcContent)
    }

    if (lyricLines.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No synced lyrics available",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
        return;
    }

    // Find current active and candidate lyric indexes
    val activeIndex = remember(currentPositionMs, lyricLines) {
        var ret = -1
        for (i in lyricLines.indices) {
            if (currentPositionMs >= lyricLines[i].timeMs) {
                ret = i
            } else {
                break
            }
        }
        ret
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Render 3 lines: previous line (dim), active highlighted line, and next line (dim)
        // Previous Line
        val prevLine = if (activeIndex > 0) lyricLines[activeIndex - 1].text else ""
        Text(
            text = prevLine,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Active highlighted line (Karaoke style!)
        val activeLine = if (activeIndex in lyricLines.indices) lyricLines[activeIndex].text else "Instrumental solo..."
        Text(
            text = activeLine,
            color = RockSecondary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Next Line
        val nextLine = if (activeIndex + 1 in lyricLines.indices) lyricLines[activeIndex + 1].text else ""
        Text(
            text = nextLine,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// Simple helper parser for .LRC files [mm:ss.xx] Lyrics
private fun parseLrc(lrc: String?): List<LyricLine> {
    if (lrc.isNullOrBlank()) return emptyList()
    val lines = lrc.lines()
    val parsed = mutableListOf<LyricLine>()
    
    val regex = Regex("""\[(\d+):(\d+)\.(\d+)\](.*)""")
    
    for (line in lines) {
        val match = regex.find(line)
        if (match != null) {
            val min = match.groupValues[1].toLong()
            val sec = match.groupValues[2].toLong()
            val milli = match.groupValues[3].toLong()
            val lyricsText = match.groupValues[4].trim()
            
            val timeMs = (min * 60 * 1000) + (sec * 1000) + (milli * 10)
            parsed.add(LyricLine(timeMs, lyricsText))
        }
    }
    return parsed.sortedBy { it.timeMs }
}
