package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Song
import com.example.model.Album
import androidx.compose.material.icons.filled.Album
import com.example.ui.components.AestheticAlbumCover
import com.example.ui.theme.DarkestCream
import com.example.ui.theme.DeepSoil
import com.example.ui.theme.LighterCream
import com.example.ui.theme.MutedSoil
import com.example.ui.theme.SandBrown
import com.example.ui.theme.ThemeState
import com.example.ui.theme.LocalStrings
import com.example.ui.theme.AppFontFamily
import com.example.viewmodel.MusicViewModel
import com.example.viewmodel.PlayerUiState

enum class SortBy {
    DATE, TITLE
}

enum class LibrarySubView {
    MAIN, FAVORITES, RECENT, ALBUMS
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onSongSelected: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var activeSubView by remember { mutableStateOf(LibrarySubView.MAIN) }
    var sortBy by remember { mutableStateOf(SortBy.DATE) }

    androidx.activity.compose.BackHandler(enabled = activeSubView != LibrarySubView.MAIN) {
        activeSubView = LibrarySubView.MAIN
    }

    when (activeSubView) {
        LibrarySubView.MAIN -> {
            LibraryMainView(
                viewModel = viewModel,
                state = state,
                sortBy = sortBy,
                onSortChange = { sortBy = it },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSubView = { activeSubView = it },
                onSongSelected = onSongSelected,
                modifier = modifier
            )
        }
        LibrarySubView.FAVORITES -> {
            val favoriteSongs = remember(state.likedSongIds, state.songs) {
                state.songs.filterIndexed { index, _ -> state.likedSongIds.contains(index) }
            }
            LibrarySubPage(
                title = "Favorites",
                songs = favoriteSongs,
                icon = Icons.Default.Favorite,
                iconColor = com.example.ui.theme.ActiveAccentColor,
                viewModel = viewModel,
                state = state,
                onBackPressed = { activeSubView = LibrarySubView.MAIN },
                onSongSelected = onSongSelected,
                modifier = modifier
            )
        }
        LibrarySubView.RECENT -> {
            val recentSongs = remember(state.recentlyPlayedIds, state.songs) {
                state.recentlyPlayedIds.mapNotNull { id -> state.songs.find { it.id == id } }
            }
            LibrarySubPage(
                title = "Recently Played",
                songs = recentSongs,
                icon = Icons.Default.Schedule,
                iconColor = DeepSoil,
                viewModel = viewModel,
                state = state,
                onBackPressed = { activeSubView = LibrarySubView.MAIN },
                onSongSelected = onSongSelected,
                modifier = modifier
            )
        }
        LibrarySubView.ALBUMS -> {
            AlbumsScreen(
                viewModel = viewModel,
                state = state,
                onBackPressed = { activeSubView = LibrarySubView.MAIN },
                onSongSelected = onSongSelected,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryMainView(
    viewModel: MusicViewModel,
    state: PlayerUiState,
    sortBy: SortBy,
    onSortChange: (SortBy) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSubView: (LibrarySubView) -> Unit,
    onSongSelected: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.importSongFromUri(context, uri)
        }
    }

    val filteredTracks = remember(state.filteredSongs, sortBy) {
        when (sortBy) {
            SortBy.TITLE -> state.filteredSongs.sortedBy { it.title.lowercase() }
            SortBy.DATE -> state.filteredSongs.sortedBy { it.id }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background((if (ThemeState.isDark) Color(0xFF141414) else LighterCream).copy(alpha = 0.70f))
                    .blur(20.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, SandBrown, RoundedCornerShape(10.dp))
                    ) {
                        com.example.ui.components.MusicaLogo(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = LocalStrings.libraryTitle,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = DeepSoil
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { filePickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream.copy(alpha = 0.6f),
                                CircleShape
                            )
                            .testTag("library_btn_add_music")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Import Music",
                            tint = DeepSoil,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream.copy(alpha = 0.6f),
                                CircleShape
                            )
                            .testTag("library_btn_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open Settings",
                            tint = DeepSoil,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("library_tracks_list")
        ) {
            // 1. Search input
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { 
                        Text(
                            text = LocalStrings.searchPlaceholder, 
                            color = MutedSoil.copy(alpha = 0.7f), 
                            fontSize = 14.sp,
                            fontFamily = AppFontFamily
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = MutedSoil,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (state.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = MutedSoil,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SandBrown,
                        unfocusedBorderColor = DarkestCream.copy(alpha = 0.5f),
                        focusedContainerColor = if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream.copy(alpha = 0.7f),
                        unfocusedContainerColor = if (ThemeState.isDark) Color(0xFF121212) else LighterCream.copy(alpha = 0.4f),
                        focusedTextColor = DeepSoil,
                        unfocusedTextColor = DeepSoil
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("library_search_input")
                )
            }

            // 2. Stats Carousel
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LibraryCategoryCard(
                            title = LocalStrings.favoritesLabel,
                            count = "${state.likedSongIds.size}${LocalStrings.songsCountSuffix}",
                            icon = Icons.Default.Favorite,
                            iconColor = com.example.ui.theme.ActiveAccentColor,
                            onClick = { onNavigateToSubView(LibrarySubView.FAVORITES) }
                        )
                    }
                    item {
                        LibraryCategoryCard(
                            title = LocalStrings.albumsLabel,
                            count = "${state.albums.size} ${if (LocalStrings.language == com.example.ui.theme.AppLanguage.PERSIAN) "آلبوم" else if (state.albums.size == 1) "Album" else "Albums"}",
                            icon = Icons.Default.Album,
                            iconColor = SandBrown,
                            onClick = { onNavigateToSubView(LibrarySubView.ALBUMS) }
                        )
                    }
                    item {
                        LibraryCategoryCard(
                            title = "Recent Tracks",
                            count = "${state.recentlyPlayedIds.size}${LocalStrings.songsCountSuffix}",
                            icon = Icons.Default.Schedule,
                            iconColor = DeepSoil,
                            onClick = { onNavigateToSubView(LibrarySubView.RECENT) }
                        )
                    }
                }
            }

            // 3. Section Title & Sort Action
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LocalStrings.offlineTracksTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = DeepSoil
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream)
                            .clickable {
                                onSortChange(if (sortBy == SortBy.TITLE) SortBy.DATE else SortBy.TITLE)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("btn_sort")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Icon",
                            tint = SandBrown,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (sortBy == SortBy.TITLE) LocalStrings.sortByTitleName else LocalStrings.sortByTitleDate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = AppFontFamily,
                            color = DeepSoil
                        )
                    }
                }
            }

            // 4. Offline track items or Empty state
            if (state.songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .background(
                                if (ThemeState.isDark) Color(0xFF121212) else LighterCream,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, SandBrown, RoundedCornerShape(16.dp))
                            ) {
                                com.example.ui.components.MusicaLogo(
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = LocalStrings.libraryEmptyTitle,
                                color = DeepSoil,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = LocalStrings.libraryEmptySubtitle,
                                color = MutedSoil,
                                fontSize = 13.sp,
                                fontFamily = AppFontFamily,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(filteredTracks, key = { _, s -> s.id }) { _, song ->
                    val actualIndex = state.songs.indexOfFirst { it.id == song.id }
                    if (actualIndex != -1) {
                        val isCurrent = state.currentSongIndex == actualIndex
                        
                        // Slide and scale animation on item appearance
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.94f, animationSpec = tween(280)),
                            exit = fadeOut(animationSpec = tween(200)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LibraryTrackItem(
                                song = song,
                                isCurrent = isCurrent,
                                isPlaying = isCurrent && state.isPlaying,
                                onClick = {
                                    viewModel.playSongAtIndex(actualIndex)
                                    onSongSelected()
                                },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // Margin at the bottom to clear mini player nicely
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibrarySubPage(
    title: String,
    songs: List<Song>,
    icon: ImageVector,
    iconColor: Color,
    viewModel: MusicViewModel,
    state: PlayerUiState,
    showPlayCounts: Boolean = false,
    onBackPressed: () -> Unit,
    onSongSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf(SortBy.DATE) }

    val filtered = remember(songs, query, sortBy) {
        val list = songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
        when (sortBy) {
            SortBy.TITLE -> list.sortedBy { it.title.lowercase() }
            SortBy.DATE -> list
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background((if (ThemeState.isDark) Color(0xFF141414) else LighterCream).copy(alpha = 0.70f))
                    .blur(20.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepSoil
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = DeepSoil
                )
            }
        }

        // Search Bar within sub page
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    text = "Search in $title...",
                    color = MutedSoil.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontFamily = AppFontFamily
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MutedSoil,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MutedSoil,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SandBrown,
                unfocusedBorderColor = DarkestCream.copy(alpha = 0.5f),
                focusedContainerColor = if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream.copy(alpha = 0.7f),
                unfocusedContainerColor = if (ThemeState.isDark) Color(0xFF121212) else LighterCream.copy(alpha = 0.4f),
                focusedTextColor = DeepSoil,
                unfocusedTextColor = DeepSoil
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filtered.size} tracks found",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AppFontFamily,
                color = MutedSoil
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream)
                    .clickable {
                        sortBy = if (sortBy == SortBy.TITLE) SortBy.DATE else SortBy.TITLE
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort Icon",
                    tint = SandBrown,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (sortBy == SortBy.TITLE) LocalStrings.sortByTitleName else LocalStrings.sortByTitleDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = AppFontFamily,
                    color = DeepSoil
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        if (ThemeState.isDark) Color(0xFF121212) else LighterCream,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tracks found",
                        color = DeepSoil,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(filtered, key = { _, s -> s.id }) { _, song ->
                    val actualIndex = state.songs.indexOfFirst { it.id == song.id }
                    if (actualIndex != -1) {
                        val isCurrent = state.currentSongIndex == actualIndex
                        
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.94f, animationSpec = tween(280)),
                            exit = fadeOut(animationSpec = tween(200)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val playCount = if (showPlayCounts) state.playCounts[song.id] ?: 0 else 0
                            LibraryTrackItem(
                                song = song,
                                isCurrent = isCurrent,
                                isPlaying = isCurrent && state.isPlaying,
                                onClick = {
                                    viewModel.playSongAtIndex(actualIndex)
                                    onSongSelected()
                                },
                                playCountBadge = if (showPlayCounts) "$playCount plays" else null,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryCategoryCard(
    title: String,
    count: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkestCream.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = DeepSoil
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = MutedSoil
            )
        }
    }
}

@Composable
fun LibraryTrackItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    playCountBadge: String? = null
) {
    val containerBgColor = if (ThemeState.isDark) {
        if (isCurrent) Color(0xFF2C2C2C) else Color(0xFF1E1E1E)
    } else {
        if (isCurrent) Color(0xFFD5C7B4) else LighterCream.copy(alpha = 0.5f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerBgColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Aesthetic Mini Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LighterCream)
        ) {
            AestheticAlbumCover(
                song = song,
                modifier = Modifier.fillMaxSize(),
                isMini = true
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = DeepSoil
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${song.album}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                color = if (isCurrent) DeepSoil.copy(alpha = 0.7f) else MutedSoil
            )
        }

        if (playCountBadge != null) {
            Text(
                text = playCountBadge,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = com.example.ui.theme.ActiveAccentColor,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        } else if (isPlaying) {
            Text(
                text = "Playing",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = com.example.ui.theme.ActiveAccentColor,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MutedSoil,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
