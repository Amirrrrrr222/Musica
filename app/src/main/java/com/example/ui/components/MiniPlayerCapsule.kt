package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.theme.AppFontFamily
import com.example.ui.theme.DarkestCream
import com.example.ui.theme.DeepSoil
import com.example.ui.theme.LighterCream
import com.example.ui.theme.MutedSoil
import com.example.ui.theme.ActiveAccentColor
import com.example.ui.theme.ThemeState

@Composable
fun MiniPlayerCapsule(
    song: Song,
    isPlaying: Boolean,
    progressSeconds: Int,
    onPlayPauseToggle: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (ThemeState.isDark) Color(0xFF141414) else LighterCream
    val borderCol = if (ThemeState.isDark) Color(0xFF242424) else DarkestCream.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, borderCol, CircleShape)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album artwork
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
            ) {
                AestheticAlbumCover(
                    song = song,
                    modifier = Modifier.fillMaxSize(),
                    isMini = true
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = DeepSoil,
                    maxLines = 1
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = AppFontFamily,
                    color = MutedSoil,
                    maxLines = 1
                )
            }

            // Media control buttons
            IconButton(
                onClick = onPrev,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = DeepSoil,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onPlayPauseToggle,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ActiveAccentColor)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (ThemeState.isDark) Color.Black else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next Track",
                    tint = DeepSoil,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Sleek playback progress indicator integrated at the bottom of the capsule
        val duration = if (song.durationSeconds > 0) song.durationSeconds else 1
        val progressFraction = (progressSeconds.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(borderCol.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction)
                    .background(ActiveAccentColor)
            )
        }
    }
}
