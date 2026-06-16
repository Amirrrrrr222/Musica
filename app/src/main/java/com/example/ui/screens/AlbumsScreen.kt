package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Album
import com.example.model.Song
import com.example.ui.components.AestheticAlbumCover
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel
import com.example.viewmodel.PlayerUiState

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    viewModel: MusicViewModel,
    state: PlayerUiState,
    onBackPressed: () -> Unit,
    onSongSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubScreen by remember { mutableStateOf<AlbumSubScreen>(AlbumSubScreen.List) }
    var selectedAlbumForDetails by remember { mutableStateOf<Album?>(null) }
    
    // For creation / editing
    var editingAlbum by remember { mutableStateOf<Album?>(null) }

    // Intercept back actions
    BackHandler {
        when (activeSubScreen) {
            AlbumSubScreen.List -> onBackPressed()
            AlbumSubScreen.Details -> activeSubScreen = AlbumSubScreen.List
            AlbumSubScreen.Create, AlbumSubScreen.Edit -> {
                activeSubScreen = if (selectedAlbumForDetails != null) AlbumSubScreen.Details else AlbumSubScreen.List
            }
        }
    }

    Scaffold(
        containerColor = WarmCreamBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (activeSubScreen) {
                            AlbumSubScreen.List -> if (ThemeState.appLanguage == AppLanguage.PERSIAN) "آلبوم‌های سفارشی" else "Custom Albums"
                            AlbumSubScreen.Details -> selectedAlbumForDetails?.title ?: ""
                            AlbumSubScreen.Create -> if (ThemeState.appLanguage == AppLanguage.PERSIAN) "ایجاد آلبوم جدید" else "Create New Album"
                            AlbumSubScreen.Edit -> if (ThemeState.appLanguage == AppLanguage.PERSIAN) "ویرایش آلبوم" else "Edit Album"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = DeepSoil
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (activeSubScreen) {
                                AlbumSubScreen.List -> onBackPressed()
                                AlbumSubScreen.Details -> activeSubScreen = AlbumSubScreen.List
                                AlbumSubScreen.Create, AlbumSubScreen.Edit -> {
                                    activeSubScreen = if (selectedAlbumForDetails != null) AlbumSubScreen.Details else AlbumSubScreen.List
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepSoil
                        )
                    }
                },
                actions = {
                    if (activeSubScreen == AlbumSubScreen.Details) {
                        IconButton(
                            onClick = {
                                editingAlbum = selectedAlbumForDetails
                                activeSubScreen = AlbumSubScreen.Edit
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Album",
                                tint = DeepSoil
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmCreamBg,
                    titleContentColor = DeepSoil
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = activeSubScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { targetScreen ->
                when (targetScreen) {
                    AlbumSubScreen.List -> {
                        AlbumGrid(
                            albums = state.albums,
                            songs = state.songs,
                            onAlbumClick = { album ->
                                selectedAlbumForDetails = album
                                activeSubScreen = AlbumSubScreen.Details
                            },
                            onCreateClick = {
                                editingAlbum = null
                                activeSubScreen = AlbumSubScreen.Create
                            }
                        )
                    }
                    AlbumSubScreen.Details -> {
                        selectedAlbumForDetails?.let { album ->
                            // Resync details album with current state in case songs/cover changed
                            val currentAlbum = state.albums.find { it.id == album.id } ?: album
                            AlbumDetailsView(
                                album = currentAlbum,
                                songs = state.songs,
                                viewModel = viewModel,
                                onPlaySong = { onSongSelected() },
                                onDeleteClick = {
                                    viewModel.deleteAlbum(currentAlbum.id)
                                    activeSubScreen = AlbumSubScreen.List
                                }
                            )
                        }
                    }
                    AlbumSubScreen.Create -> {
                        AlbumFormView(
                            album = null,
                            allSongs = state.songs,
                            onSave = { title, coverUri, songIds ->
                                viewModel.createAlbum(title, coverUri, songIds)
                                activeSubScreen = AlbumSubScreen.List
                            }
                        )
                    }
                    AlbumSubScreen.Edit -> {
                        editingAlbum?.let { album ->
                            AlbumFormView(
                                album = album,
                                allSongs = state.songs,
                                onSave = { title, coverUri, songIds ->
                                    viewModel.updateAlbum(album.id, title, coverUri, songIds)
                                    // Resync detail view representation
                                    selectedAlbumForDetails = state.albums.find { it.id == album.id }?.copy(
                                        title = title,
                                        coverUri = coverUri,
                                        songIdsJson = Album.createSongIdsJson(songIds)
                                    ) ?: Album(album.id, title, coverUri, Album.createSongIdsJson(songIds))
                                    activeSubScreen = AlbumSubScreen.Details
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class AlbumSubScreen {
    List, Details, Create, Edit
}

@Composable
fun AlbumGrid(
    albums: List<Album>,
    songs: List<Song>,
    onAlbumClick: (Album) -> Unit,
    onCreateClick: () -> Unit
) {
    if (albums.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream)
                        .border(1.dp, SandBrown, RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = SandBrown,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "هیچ آلبومی ساخته نشده است" else "No custom albums created",
                    color = DeepSoil,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "آلبوم‌های سفارشی خود را بسازید و آهنگ‌های مورد علاقه خود را دسته‌بندی کنید." else "Organize and group your tracks into personalized custom albums.",
                    color = MutedSoil,
                    fontSize = 14.sp,
                    fontFamily = AppFontFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SandBrown,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("btn_empty_create_album")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "ایجاد اولین آلبوم" else "Create First Album",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = AppFontFamily
                    )
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.78f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onCreateClick)
                        .border(
                            width = 1.5.dp,
                            color = SandBrown.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .testTag("btn_grid_create_album"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ThemeState.isDark) Color(0xFF161616) else Color.Transparent
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SandBrown.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = SandBrown,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "ایجاد آلبوم جدید" else "Create New Album",
                            color = DeepSoil,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            items(albums, key = { it.id }) { album ->
                AlbumCardItem(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
            // Clearance spacing for floating players
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(112.dp))
            }
        }
    }
}

@Composable
fun AlbumCardItem(
    album: Album,
    onClick: () -> Unit
) {
    val songIds = remember(album.songIdsJson) { album.getSongIds() }
    val songCount = songIds.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("album_item_${album.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
            ) {
                if (!album.coverUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = Uri.parse(album.coverUri),
                        contentDescription = "${album.title} Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback artistic gradient representation styled uniquely for each album ID
                    val hash = album.id
                    val brushColors = when (hash % 4) {
                        0 -> listOf(Color(0xFFE9DCC9), Color(0xFFD4A373))
                        1 -> listOf(Color(0xFFEDE0D4), Color(0xFFC7C7B4))
                        2 -> listOf(Color(0xFFF1E3EF), Color(0xFFCEFBC0))
                        else -> listOf(Color(0xFF2C2C2C), Color(0xFFE0E0E0))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Brush.linearGradient(brushColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = DeepSoil.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }
            
            // Text area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = album.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = AppFontFamily,
                    color = DeepSoil,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "$songCount آهنگ" else "$songCount songs",
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    fontFamily = AppFontFamily,
                    color = MutedSoil
                )
            }
        }
    }
}

@Composable
fun AlbumDetailsView(
    album: Album,
    songs: List<Song>,
    viewModel: MusicViewModel,
    onPlaySong: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val albumSongs = remember(album.songIdsJson, songs) {
        val ids = album.getSongIds()
        ids.mapNotNull { id -> songs.find { it.id == id } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Hero header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Gray.copy(alpha = 0.1f))
                ) {
                    if (!album.coverUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = Uri.parse(album.coverUri),
                            contentDescription = album.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback gradient
                        val hash = album.id
                        val brushColors = when (hash % 4) {
                            0 -> listOf(Color(0xFFE9DCC9), Color(0xFFD4A373))
                            1 -> listOf(Color(0xFFEDE0D4), Color(0xFFC7C7B4))
                            2 -> listOf(Color(0xFFF1E3EF), Color(0xFFCEFBC0))
                            else -> listOf(Color(0xFF2C2C2C), Color(0xFFE0E0E0))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(androidx.compose.ui.graphics.Brush.linearGradient(brushColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = DeepSoil.copy(alpha = 0.4f),
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = album.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = DeepSoil,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "${albumSongs.size} آهنگ در این آلبوم" else "${albumSongs.size} tracks in this album",
                    fontSize = 14.sp,
                    fontFamily = AppFontFamily,
                    color = MutedSoil
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Play Album Button
                    if (albumSongs.isNotEmpty()) {
                        Button(
                            onClick = {
                                // Find index of first album song in entire queue list or load list
                                val firstSong = albumSongs.first()
                                val totalIndex = songs.indexOfFirst { it.id == firstSong.id }
                                if (totalIndex != -1) {
                                    viewModel.playSongAtIndex(totalIndex)
                                    onPlaySong()
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SandBrown,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("btn_play_album")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "پخش آلبوم" else "Play Album",
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily
                            )
                        }
                    }

                    // Delete Album Button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.08f))
                            .testTag("btn_delete_album")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Album",
                            tint = Color.Red
                        )
                    }
                }
            }
        }

        // List Header label
        item {
            Text(
                text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "لیست آهنگ‌ها" else "Song List",
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = DeepSoil,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (albumSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "آهنگی در این آلبوم وجود ندارد. دکمه ویرایش را از بالا بزنید." else "No songs added to this album. Tap Edit above to add.",
                        color = MutedSoil,
                        fontSize = 13.sp,
                        fontFamily = AppFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(albumSongs) { song ->
                var isCurrent = false
                val currentSong = viewModel.uiState.value.currentSong
                if (currentSong?.id == song.id) {
                    isCurrent = true
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCurrent) SandBrown.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable {
                            val idx = songs.indexOfFirst { it.id == song.id }
                            if (idx != -1) {
                                viewModel.playSongAtIndex(idx)
                                onPlaySong()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Gray.copy(alpha = 0.1f))
                    ) {
                        AestheticAlbumCover(song = song, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isCurrent) SandBrown else DeepSoil,
                            fontFamily = AppFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            fontSize = 12.sp,
                            color = MutedSoil,
                            fontFamily = AppFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bottom spaces
        item {
            Spacer(modifier = Modifier.height(112.dp))
        }
    }
}

@Composable
fun AlbumFormView(
    album: Album?,
    allSongs: List<Song>,
    onSave: (title: String, coverUri: String?, songIds: List<Int>) -> Unit
) {
    var title by remember { mutableStateOf(album?.title ?: "") }
    var coverUri by remember { mutableStateOf(album?.coverUri) }
    val initialSongIds = remember(album) { album?.getSongIds() ?: emptyList() }
    val selectedSongIds = remember { mutableStateListOf<Int>().apply { addAll(initialSongIds) } }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coverUri = uri.toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Cover selector Area
        item {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream)
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .border(2.dp, SandBrown, RoundedCornerShape(24.dp))
                    .testTag("album_cover_button"),
                contentAlignment = Alignment.Center
            ) {
                if (!coverUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = Uri.parse(coverUri),
                        contentDescription = "Selected Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Change label overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Change Cover",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Select Cover",
                            tint = SandBrown,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "انتخاب عکس کاور" else "Select Cover",
                            fontFamily = AppFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SandBrown
                        )
                    }
                }
            }
        }

        // 2. Title field
        item {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "نام آلبوم" else "Album Title",
                        fontFamily = AppFontFamily
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("album_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SandBrown,
                    unfocusedBorderColor = DarkestCream.copy(alpha = 0.5f),
                    focusedLabelColor = SandBrown,
                    unfocusedLabelColor = MutedSoil,
                    focusedTextColor = DeepSoil,
                    unfocusedTextColor = DeepSoil
                )
            )
        }

        // 3. Selection of Songs
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "انتخاب آهنگ‌ها" else "Select Songs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = AppFontFamily,
                    color = DeepSoil
                )
            }
        }

        if (allSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "آهنگی وجود ندارد. لطفا آهنگ بارگذاری کنید." else "No songs in library to add. Load songs first.",
                        color = MutedSoil,
                        fontFamily = AppFontFamily,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(allSongs) { song ->
                val isSelected = selectedSongIds.contains(song.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (isSelected) {
                                selectedSongIds.remove(song.id)
                            } else {
                                selectedSongIds.add(song.id)
                            }
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                selectedSongIds.add(song.id)
                            } else {
                                selectedSongIds.remove(song.id)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SandBrown,
                            uncheckedColor = MutedSoil
                        ),
                        modifier = Modifier.testTag("album_checkbox_${song.id}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.1f))
                    ) {
                        AestheticAlbumCover(song = song, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = AppFontFamily,
                            color = DeepSoil,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily,
                            color = MutedSoil,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Save Button
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, coverUri, selectedSongIds.toList())
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SandBrown,
                    contentColor = Color.White,
                    disabledContainerColor = DarkestCream,
                    disabledContentColor = MutedSoil
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_album")
            ) {
                Text(
                    text = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "ذخیره آلبوم" else "Save Album",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily
                )
            }
            Spacer(modifier = Modifier.height(112.dp))
        }
    }
}
