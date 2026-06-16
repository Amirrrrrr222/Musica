package com.example.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.provider.MediaStore
import android.util.Log
import com.example.R
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object PlaybackManager {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var app: Application? = null
    private var repository: com.example.db.AlbumRepository? = null

    fun init(application: Application) {
        if (this.app != null) return
        this.app = application
        val db = com.example.db.AppDatabase.getDatabase(application)
        val repo = com.example.db.AlbumRepository(db.albumDao())
        this.repository = repo

        scope.launch {
            repo.allAlbums.collect { albumList ->
                _uiState.update { it.copy(albums = albumList) }
            }
        }
    }

    fun createAlbum(title: String, coverUri: String?, songIds: List<Int>) {
        scope.launch(Dispatchers.IO) {
            val album = com.example.model.Album(
                title = title,
                coverUri = coverUri,
                songIdsJson = com.example.model.Album.createSongIdsJson(songIds)
            )
            repository?.insert(album)
        }
    }

    fun updateAlbum(albumId: Int, newTitle: String, newCoverUri: String?, newSongIds: List<Int>) {
        scope.launch(Dispatchers.IO) {
            val album = com.example.model.Album(
                id = albumId,
                title = newTitle,
                coverUri = newCoverUri,
                songIdsJson = com.example.model.Album.createSongIdsJson(newSongIds)
            )
            repository?.update(album)
        }
    }

    fun deleteAlbum(albumId: Int) {
        scope.launch(Dispatchers.IO) {
            val album = repository?.getAlbumById(albumId)
            if (album != null) {
                repository?.delete(album)
            }
        }
    }

    private fun startService() {
        app?.let { context ->
            try {
                val intent = Intent(context, Class.forName("com.example.service.MusicService"))
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("PlaybackManager", "Could not start MusicService", e)
            }
        }
    }

    fun scanDeviceSongs(context: Context) {
        scope.launch {
            val songList = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val list = mutableListOf<Song>()
                val contentResolver: ContentResolver = context.contentResolver
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ALBUM_ID
                )

                try {
                    contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                        val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                        while (cursor.moveToNext()) {
                            val id = cursor.getInt(idCol)
                            val title = cursor.getString(titleCol) ?: "Unknown Title"
                            val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                            val album = cursor.getString(albumCol) ?: "Unknown Album"
                            val durationMs = cursor.getInt(durationCol)
                            val durationSec = durationMs / 1000
                            val albumId = cursor.getLong(albumIdCol)
                            val albumArtUri = "content://media/external/audio/albumart/$albumId"

                            list.add(
                                Song(
                                    id = id,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    albumArtRes = R.drawable.ic_album_art,
                                    durationSeconds = if (durationSec > 0) durationSec else 180,
                                    lyrics = "",
                                    albumArtUri = albumArtUri
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                list
            }

            _uiState.update {
                it.copy(
                    songs = songList,
                    currentSongIndex = 0,
                    progressSeconds = 0,
                    hasScanned = true
                )
            }
        }
    }

    fun loadDemoSongs() {
        val songList = listOf(
            Song(
                id = 1111,
                title = "Where Is My Mind",
                artist = "Pixies",
                album = "Death to the Pixies",
                albumArtRes = R.drawable.ic_album_art,
                durationSeconds = 229, // 3:49
                lyrics = """
                    With your feet in the air and your head on the ground.
                    Try this trick and spin it, yeah.
                    Your head will collapse but there's nothing in it.
                    And you'll ask yourself:
                    Where is my mind?
                    Where is my mind?
                    Where is my mind?
                    
                    Way out in the water, see it swimming.
                    
                    I was swimmin' in the Caribbean.
                    Animals were hiding behind the rock.
                    Except the little fish, but they told me,
                    He swears he's tryin' to talk to me, to me.
                    
                    Where is my mind?
                """.trimIndent()
            ),
            Song(
                id = 2222,
                title = "Here Comes Your Man",
                artist = "Pixies",
                album = "Doolittle",
                albumArtRes = R.drawable.ic_album_art,
                durationSeconds = 201, // 3:21
                lyrics = """
                    Outside there's a box car waiting.
                    Outside the family is debating.
                    About the rain, about the leaks in the roof.
                    And they're not too sure of the local sheriff.
                    
                    Here comes your man!
                    Here comes your man!
                    
                    There is a wind blows leaves and snow.
                    And a big bird, and it dies.
                """.trimIndent()
            ),
            Song(
                id = 3333,
                title = "Gouge Away",
                artist = "Pixies",
                album = "Doolittle",
                albumArtRes = R.drawable.ic_album_art,
                durationSeconds = 165, // 2:45
                lyrics = ""
            ),
            Song(
                id = 4444,
                title = "Hey",
                artist = "Pixies",
                album = "Doolittle",
                albumArtRes = R.drawable.ic_album_art,
                durationSeconds = 211, // 3:31
                lyrics = """
                    Hey!
                    Been trying to meet you.
                    Mmh, hey!
                    Must be a devil between us or in write-up.
                    Uh, we're in major league.
                    
                    Hey!
                """.trimIndent()
            )
        )
        _uiState.update {
            it.copy(
                songs = songList,
                currentSongIndex = 0,
                progressSeconds = 0,
                hasScanned = true
            )
        }
    }

    fun importSongFromUri(context: Context, uri: android.net.Uri) {
        val resolver = context.contentResolver
        try {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            resolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        scope.launch(Dispatchers.IO) {
            var title = "Unknown Title"
            var artist = "Unknown Artist"
            var album = "Unknown Album"
            var durationSec = 180

            val retriever = android.media.MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                title = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                artist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                album = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Imported Album"
                val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (durationStr != null) {
                    val durationMs = durationStr.toInt()
                    if (durationMs > 0) durationSec = durationMs / 1000
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (title.isBlank()) {
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            val displayName = cursor.getString(nameIndex)
                            if (!displayName.isNullOrBlank()) {
                                title = displayName.substringBeforeLast(".")
                            }
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                if (title.isBlank()) {
                    title = "Imported File"
                }
            }

            val newId = (System.currentTimeMillis() % 1000000).toInt() + 5000000

            val importedSong = Song(
                id = newId,
                title = title,
                artist = artist,
                album = album,
                albumArtRes = R.drawable.ic_album_art,
                durationSeconds = durationSec,
                lyrics = "",
                albumArtUri = "content://custom_import/$newId",
                fileUri = uri.toString()
            )

            launch(Dispatchers.Main) {
                _uiState.update { state ->
                    val updatedSongs = state.songs.toMutableList()
                    updatedSongs.add(0, importedSong)
                    state.copy(
                        songs = updatedSongs,
                        currentSongIndex = 0,
                        progressSeconds = 0,
                        hasScanned = true
                    )
                }
                playSongAtIndex(0)
            }
        }
    }

    private fun playLocalOrStream(song: Song) {
        _uiState.update { state ->
            val updatedRecent = state.recentlyPlayedIds.toMutableList()
            updatedRecent.remove(song.id)
            updatedRecent.add(0, song.id)

            val updatedCounts = state.playCounts.toMutableMap()
            updatedCounts[song.id] = (updatedCounts[song.id] ?: 0) + 1

            state.copy(
                recentlyPlayedIds = updatedRecent,
                playCounts = updatedCounts
            )
        }

        val context = app ?: return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                if (song.fileUri != null) {
                    setDataSource(context, android.net.Uri.parse(song.fileUri))
                } else if (song.albumArtUri != null && song.albumArtUri.startsWith("content://") && !song.albumArtUri.contains("custom_import")) {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        song.id.toLong()
                    )
                    setDataSource(context, uri)
                } else {
                    val streamUrl = when (song.id) {
                        1111 -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                        2222 -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
                        3333 -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
                        else -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
                    }
                    setDataSource(streamUrl)
                }
                prepareAsync()
                setOnPreparedListener {
                    start()
                    _uiState.update { it.copy(isPlaying = true) }
                    startTimer()
                    startService()
                }
                setOnCompletionListener {
                    nextSong()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        val currentState = _uiState.value
        if (currentState.songs.isEmpty()) return
        val nextPlayState = !currentState.isPlaying

        if (nextPlayState) {
            val song = currentState.currentSong
            if (song != null) {
                if (mediaPlayer == null) {
                    playLocalOrStream(song)
                } else {
                    try {
                        mediaPlayer?.start()
                        _uiState.update { it.copy(isPlaying = true) }
                        startTimer()
                        startService()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _uiState.update { it.copy(isPlaying = false) }
            stopTimer()
            startService() // update service notification
        }
    }

    fun playSongAtIndex(index: Int) {
        if (index in _uiState.value.songs.indices) {
            val song = _uiState.value.songs[index]
            _uiState.update {
                it.copy(
                    currentSongIndex = index,
                    progressSeconds = 0,
                    isPlaying = false // will set to true once prepared
                )
            }
            playLocalOrStream(song)
        }
    }

    fun nextSong() {
        val state = _uiState.value
        if (state.songs.isEmpty()) return

        val nextIndex = if (state.isShuffled) {
            state.songs.indices.random()
        } else {
            (state.currentSongIndex + 1) % state.songs.size
        }
        playSongAtIndex(nextIndex)
    }

    fun previousSong() {
        val state = _uiState.value
        if (state.songs.isEmpty()) return

        val prevIndex = if (state.currentSongIndex <= 0) {
            state.songs.size - 1
        } else {
            state.currentSongIndex - 1
        }
        playSongAtIndex(prevIndex)
    }

    fun seekTo(seconds: Int) {
        val currentSong = _uiState.value.currentSong ?: return
        val clampedSeconds = seconds.coerceIn(0, currentSong.durationSeconds)
        _uiState.update { it.copy(progressSeconds = clampedSeconds) }

        try {
            mediaPlayer?.seekTo(clampedSeconds * 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleShuffle() {
        _uiState.update { it.copy(isShuffled = !it.isShuffled) }
        startService()
    }

    fun toggleRepeat() {
        _uiState.update { it.copy(isRepeated = !it.isRepeated) }
        startService()
    }

    fun toggleFavorite(songIndex: Int) {
        _uiState.update { state ->
            val updated = state.likedSongIds.toMutableSet()
            if (updated.contains(songIndex)) {
                updated.remove(songIndex)
            } else {
                updated.add(songIndex)
            }
            state.copy(likedSongIds = updated)
        }
        startService()
    }

    fun toggleLyrics() {
        _uiState.update { it.copy(showLyrics = !it.showLyrics) }
    }

    fun toggleQueueSheet(show: Boolean) {
        _uiState.update { it.copy(showQueueSheet = show) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateSongLyrics(songId: Int, newLyrics: String) {
        _uiState.update { state ->
            val updatedSongs = state.songs.map { song ->
                if (song.id == songId) {
                    song.copy(lyrics = newLyrics)
                } else {
                    song
                }
            }
            state.copy(songs = updatedSongs)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            val curSec = mp.currentPosition / 1000
                            _uiState.update { it.copy(progressSeconds = curSec) }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
