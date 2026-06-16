package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.blur
import com.example.ui.theme.ThemeState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Repeat
import com.example.ui.theme.LocalStrings
import com.example.ui.theme.AppFontFamily
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ui.components.AestheticAlbumCover
import com.example.R
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.model.Song
import com.example.ui.components.AestheticAlbumCover
import com.example.ui.theme.DarkestCream
import com.example.ui.theme.DeepSoil
import com.example.ui.theme.LighterCream
import com.example.ui.theme.MutedSoil
import com.example.ui.theme.SandBrown
import com.example.ui.theme.WarmCreamBg
import com.example.viewmodel.MusicViewModel

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val showQueueSheet = state.showQueueSheet

    androidx.activity.compose.BackHandler(enabled = state.showLyrics || state.showQueueSheet) {
        if (state.showLyrics) {
            viewModel.toggleLyrics()
        } else if (state.showQueueSheet) {
            viewModel.toggleQueueSheet(false)
        }
    }

    val dimAlpha by animateFloatAsState(
        targetValue = if (showQueueSheet) 0.65f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "dimAlpha"
    )

    // Swipe down/up registers to pull up the queue
    val draggableState = rememberDraggableState { delta ->
        if (delta > 15f || delta < -15f) {
            viewModel.toggleQueueSheet(true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCreamBg)
    ) {
        val songs = state.songs
        val song = state.currentSong

        if (songs.isEmpty() || song == null) {
            // Elegant premium Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.5.dp, SandBrown, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.ui.components.MusicaLogo(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome to Musica",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = DeepSoil,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Scan your device to discover and play your offline audio files. No files are uploaded; your catalog remains completely private.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = AppFontFamily,
                    color = MutedSoil,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.scanDeviceSongs(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SandBrown, 
                        contentColor = if (ThemeState.isDark) Color(0xFF000000) else Color(0xFFECE5D9)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(52.dp)
                        .testTag("btn_scan_storage")
                ) {
                    Text(
                        text = "Scan Device Storage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily
                    )
                }
            }
        } else {
            // High efficiency non-scrollable viewport-optimized view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                // 1. Sleek top decorative capsule handle
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 12.dp)
                        .width(42.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(DeepSoil.copy(alpha = 0.25f))
                        .clickable { viewModel.toggleQueueSheet(true) }
                        .draggable(state = draggableState, orientation = Orientation.Vertical)
                        .testTag("drawer_handle")
                )

                // 2. Beautiful dynamic album artwork cover
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val artworkModifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight(0.85f)

                    // High-performance animated layered squared glow (dynamic neon lighting following the Theme variant)
                    val infiniteTransition = rememberInfiniteTransition(label = "squaredGlowBreathing")
                    val glowPulse by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "squaredGlowPulse"
                    )

                    val activeGlowColor = com.example.ui.theme.ActiveAccentColor

                    // Layer 3: Outer Layer (Very faint, wide diffusion for depth)
                    Box(
                        modifier = artworkModifier
                            .scale(1.22f)
                            .background(
                                color = activeGlowColor.copy(alpha = if (ThemeState.isDark) 0.12f * glowPulse else 0.08f * glowPulse),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .blur(56.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                    )

                    // Layer 2: Middle Layer (Medium spread, softer opacity, expands outward)
                    Box(
                        modifier = artworkModifier
                            .scale(1.10f)
                            .background(
                                color = activeGlowColor.copy(alpha = if (ThemeState.isDark) 0.22f * glowPulse else 0.15f * glowPulse),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .blur(32.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                    )

                    // Layer 1: Inner Layer (Strong intensity, close to cover edges, slightly sharper blur)
                    Box(
                        modifier = artworkModifier
                            .scale(1.02f)
                            .background(
                                color = activeGlowColor.copy(alpha = if (ThemeState.isDark) 0.45f * glowPulse else 0.30f * glowPulse),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .blur(14.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                    )

                    Box(
                        modifier = artworkModifier
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(32.dp),
                                clip = false,
                                ambientColor = if (ThemeState.isDark) Color.Black.copy(alpha = 0.5f) else DeepSoil.copy(alpha = 0.25f),
                                spotColor = if (ThemeState.isDark) Color.Black.copy(alpha = 0.7f) else DeepSoil.copy(alpha = 0.35f)
                            )
                            .clip(RoundedCornerShape(32.dp))
                    ) {
                        AestheticAlbumCover(
                            song = song,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Typographic song detail info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = song.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold, // Unified bold title
                        fontFamily = FontFamily.SansSerif, // Unified geometric look
                        color = DeepSoil,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("song_title")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium, // Matching subtitle
                        fontFamily = FontFamily.SansSerif,
                        color = MutedSoil,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("song_artist")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Compact Action Triggers (Heart, Chat/Lyrics, Share)
                Row(
                    modifier = Modifier.width(220.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLiked = state.likedSongIds.contains(state.currentSongIndex)

                    IconButton(
                        onClick = { viewModel.toggleFavorite(state.currentSongIndex) },
                        modifier = Modifier.size(48.dp).testTag("action_favorite")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite Song",
                            tint = if (isLiked) com.example.ui.theme.ActiveAccentColor else DeepSoil,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Lyrics button is now permanently active
                    IconButton(
                        onClick = { viewModel.toggleLyrics() },
                        modifier = Modifier.size(48.dp).testTag("action_lyrics")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = "Song Lyrics",
                            tint = DeepSoil,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Check out this song!")
                                putExtra(Intent.EXTRA_TEXT, "Listening to ${song.title} by ${song.artist} on Musica!")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                        },
                        modifier = Modifier.size(48.dp).testTag("action_share")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share Choice",
                            tint = DeepSoil,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Progress slider with total harmony color ActiveAccentColor
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    var sliderPosition by remember { mutableStateOf<Float?>(null) }
                    val currentPlayPosition = sliderPosition ?: state.progressSeconds.toFloat()
                    Slider(
                        value = currentPlayPosition,
                        onValueChange = { sliderPosition = it },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                viewModel.seekTo(it.toInt())
                            }
                            sliderPosition = null
                        },
                        valueRange = 0f..song.durationSeconds.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = com.example.ui.theme.ActiveAccentColor,
                            activeTrackColor = com.example.ui.theme.ActiveAccentColor,
                            inactiveTrackColor = DarkestCream.copy(alpha = 0.5f),
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .testTag("timeline_slider")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPlayPosition.toInt()),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif,
                            color = DeepSoil
                        )
                        Text(
                            text = formatTime(song.durationSeconds),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif,
                            color = DeepSoil
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Media Playback Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Icon
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = { viewModel.toggleShuffle() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("control_shuffle")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (state.isShuffled) com.example.ui.theme.ActiveAccentColor else DeepSoil.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (state.isShuffled) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 34.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.ActiveAccentColor)
                            )
                        }
                    }

                    // Previous Icon
                    IconButton(
                        onClick = { viewModel.previousSong() },
                        modifier = Modifier.size(48.dp).testTag("control_previous")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous Song",
                            tint = DeepSoil,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play/Pause with identical color background (SandBrown)
                    val playScale by animateFloatAsState(
                        targetValue = if (state.isPlaying) 1.05f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "playScale"
                    )
                    Box(
                        modifier = Modifier
                            .scale(playScale)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.ActiveAccentColor) // Identical active matching color!
                            .clickable { viewModel.togglePlayPause() }
                            .testTag("control_play_pause"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = LighterCream,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Next Icon
                    IconButton(
                        onClick = { viewModel.nextSong() },
                        modifier = Modifier.size(48.dp).testTag("control_next")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next Song",
                            tint = DeepSoil,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Repeat Icon
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = { viewModel.toggleRepeat() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("control_repeat")
                        ) {
                            Icon(
                                imageVector = if (state.isRepeated) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                                contentDescription = "Repeat",
                                tint = if (state.isRepeated) com.example.ui.theme.ActiveAccentColor else DeepSoil.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (state.isRepeated) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 34.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.ActiveAccentColor)
                            )
                        }
                    }
                }
            }
        }

        // Smooth dimming overlay that darkens the player content below the sliding drawer
        if (dimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = dimAlpha }
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.toggleQueueSheet(false) }
                    )
            )
        }

        // Sliding Playlist Drawer from Top
        AnimatedVisibility(
            visible = showQueueSheet,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(LighterCream)
                    .draggable(
                        state = rememberDraggableState { delta ->
                            if (delta < -10f) { // Swipe up to close
                                viewModel.toggleQueueSheet(false)
                            }
                        },
                        orientation = Orientation.Vertical
                    )
                    .clickable(enabled = false) {} // absorb click events
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.QueueMusic,
                                contentDescription = null,
                                tint = SandBrown,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Playlist Queue",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = DeepSoil
                            )
                        }

                        IconButton(onClick = { viewModel.toggleQueueSheet(false) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Playlist",
                                tint = DeepSoil
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                    ) {
                        itemsIndexed(songs) { idx, s ->
                            val activeState = state.currentSongIndex == idx
                            val queueRowBg = if (activeState) {
                                if (ThemeState.isDark) Color(0xFF2C2C2C) else Color(0xFFECE5D9)
                            } else Color.Transparent
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(queueRowBg)
                                    .clickable {
                                        viewModel.playSongAtIndex(idx)
                                        viewModel.toggleQueueSheet(false)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (ThemeState.isDark) Color(0xFF222222) else LighterCream)
                                ) {
                                    AestheticAlbumCover(
                                        song = s,
                                        modifier = Modifier.fillMaxSize(),
                                        isMini = true
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = if (activeState) com.example.ui.theme.ActiveAccentColor else DeepSoil
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = s.artist,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = MutedSoil
                                    )
                                }

                                if (activeState && state.isPlaying) {
                                    Text(
                                        text = "NOW PLAYING",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = SandBrown
                                    )
                                }
                            }
                        }
                    }

                    // Tactile closing handle placed ergonomically at the bottom edge
                    Box(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .width(50.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(DeepSoil.copy(alpha = 0.25f))
                            .draggable(
                                state = rememberDraggableState { delta ->
                                    if (delta < -10f || delta > 10f) {
                                        viewModel.toggleQueueSheet(false)
                                    }
                                },
                                orientation = Orientation.Vertical
                            )
                            .clickable { viewModel.toggleQueueSheet(false) }
                            .testTag("drawer_handle_close")
                    )
                }
            }
        }

        // 7. Dynamic sliding Lyrics Panel
        AnimatedVisibility(
            visible = state.showLyrics,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        ) {
            var isEditing by remember(song?.id) { mutableStateOf(false) }
            var lyricsInputText by remember(song?.id) { mutableStateOf(song?.lyrics ?: "") }

            val lyricsBg = if (ThemeState.isDark) {
                Color(0xFF141414)
            } else {
                when (ThemeState.themeVariant) {
                    com.example.ui.theme.AppThemeVariant.BLUE -> Color(0xFFE8F0FE)
                    com.example.ui.theme.AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFFFF0F3)
                    else -> Color(0xFFE4DEC6)
                }
            }
            val lyricsTextColor = if (ThemeState.isDark) Color(0xFFEEEEEE) else DeepSoil
            val lyricsTitleColor = if (ThemeState.isDark) Color(0xFFFFFFFF) else DeepSoil
            val inputBg = if (ThemeState.isDark) Color(0xFF222222) else DarkestCream

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)) // Darken the behind background significantly
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.toggleLyrics() }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                ) {
                    // Modern 70% intensity/opacity strong blur layer
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(lyricsBg.copy(alpha = 0.70f))
                            .blur(30.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = false) {}, // absorb touch events
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(lyricsTitleColor.copy(alpha = 0.2f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LocalStrings.lyricsTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = lyricsTitleColor
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isEditing && (song?.lyrics?.isNotEmpty() == true)) {
                                Button(
                                    onClick = { isEditing = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SandBrown, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 8.dp).height(36.dp)
                                ) {
                                    Text(
                                        text = LocalStrings.editBtn, 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = AppFontFamily
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.toggleLyrics() }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close Lyrics",
                                    tint = lyricsTitleColor
                                )
                            }
                        }
                    }

                    if (isEditing || (song?.lyrics?.trim()?.isEmpty() == true)) {
                        // Edit / empty proposal interface
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 28.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (song?.lyrics?.trim()?.isEmpty() == true) {
                                Text(
                                    text = LocalStrings.noLyricsFound,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = lyricsTextColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                            
                            OutlinedTextField(
                                value = lyricsInputText,
                                onValueChange = { lyricsInputText = it },
                                placeholder = { 
                                    Text(
                                        text = LocalStrings.writeLyricsPlaceholder, 
                                        color = lyricsTextColor.copy(alpha = 0.5f),
                                        fontFamily = AppFontFamily
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(bottom = 12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SandBrown,
                                    unfocusedBorderColor = DarkestCream,
                                    focusedContainerColor = inputBg,
                                    unfocusedContainerColor = inputBg,
                                    focusedTextColor = lyricsTextColor,
                                    unfocusedTextColor = lyricsTextColor
                                ),
                                singleLine = false,
                                shape = RoundedCornerShape(16.dp)
                            )
                            
                            Button(
                                onClick = {
                                    if (song != null) {
                                        viewModel.updateSongLyrics(song.id, lyricsInputText)
                                    }
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SandBrown, contentColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = LocalStrings.saveLyricsBtn, 
                                    fontWeight = FontWeight.Bold, 
                                    fontFamily = AppFontFamily, 
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Display lyrics interface
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 28.dp, end = 28.dp, bottom = 24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = song?.lyrics ?: "",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif,
                                color = lyricsTextColor,
                                lineHeight = 32.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}
