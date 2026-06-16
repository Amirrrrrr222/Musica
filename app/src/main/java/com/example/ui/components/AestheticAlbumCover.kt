package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Song
import androidx.compose.runtime.key
import com.example.ui.theme.DeepSoil

@Composable
fun AestheticAlbumCover(
    song: Song,
    modifier: Modifier = Modifier,
    isMini: Boolean = false
) {
    key(song.id) {
        if (!song.albumArtUri.isNullOrEmpty()) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = "${song.title} Album Art",
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        } else {
            // Formulate a beautiful organic gradient strictly based on the unique ID of the song
            val hash = song.id
            val gradColors = if (com.example.ui.theme.ThemeState.isDark) {
                when (hash % 4) {
                    0 -> listOf(Color(0xFF1E1E1E), Color(0xFF383838), Color(0xFF2B2B2B))
                    1 -> listOf(Color(0xFF121212), Color(0xFF262626), Color(0xFF3F3F3F))
                    2 -> listOf(Color(0xFF2C2C2C), Color(0xFF424242), Color(0xFF1C1C1C))
                    else -> listOf(Color(0xFF1A1A1A), Color(0xFF303030), Color(0xFF4A4A4A))
                }
            } else {
                when (hash % 4) {
                    0 -> listOf(Color(0xFFE9DCC9), Color(0xFFD4A373), Color(0xFFCEDFD9)) // Cappuccino/Emerald Cream
                    1 -> listOf(Color(0xFFEDE0D4), Color(0xFFC7C7B4), Color(0xFFE6BDF1)) // Lavender/Almond Cream
                    2 -> listOf(Color(0xFFF1E3EF), Color(0xFFCEFBC0), Color(0xFFD8C6FF)) // Pastel Mint/Orchid Cream
                    else -> listOf(Color(0xFFECECEC), Color(0xFFD3A9C9), Color(0xFFADB7D0)) // Steel-slate Cream
                }
            }

            Box(
                modifier = modifier
                    .background(Brush.linearGradient(gradColors)),
                contentAlignment = Alignment.Center
            ) {
                if (isMini) {
                    // Mini simple avatar square for list rows
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = song.title.take(1).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = DeepSoil
                        )
                    }
                } else {
                    // Generous graphic vinyl-modern layout for big album cards
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = song.title.take(1).uppercase(),
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = DeepSoil
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = song.album,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = DeepSoil.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
