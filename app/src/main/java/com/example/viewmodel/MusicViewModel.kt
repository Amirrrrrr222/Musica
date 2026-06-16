package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.model.Song
import com.example.model.Album
import kotlinx.coroutines.flow.StateFlow

data class PlayerUiState(
    val songs: List<Song> = emptyList(),
    val currentSongIndex: Int = 0,
    val isPlaying: Boolean = false,
    val progressSeconds: Int = 0,
    val isShuffled: Boolean = false,
    val isRepeated: Boolean = false,
    val likedSongIds: Set<Int> = emptySet(),
    val showLyrics: Boolean = false,
    val searchQuery: String = "",
    val hasScanned: Boolean = false,
    val showQueueSheet: Boolean = false,
    val recentlyPlayedIds: List<Int> = emptyList(),
    val playCounts: Map<Int, Int> = emptyMap(),
    val albums: List<Album> = emptyList()
) {
    val currentSong: Song?
        get() = if (songs.isNotEmpty() && currentSongIndex in songs.indices) songs[currentSongIndex] else null

    val filteredSongs: List<Song>
        get() = if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    init {
        PlaybackManager.init(application)
    }

    val uiState: StateFlow<PlayerUiState> = PlaybackManager.uiState

    fun scanDeviceSongs(context: Context) {
        PlaybackManager.scanDeviceSongs(context)
    }

    fun loadDemoSongs() {
        PlaybackManager.loadDemoSongs()
    }

    fun togglePlayPause() {
        PlaybackManager.togglePlayPause()
    }

    fun playSongAtIndex(index: Int) {
        PlaybackManager.playSongAtIndex(index)
    }

    fun nextSong() {
        PlaybackManager.nextSong()
    }

    fun previousSong() {
        PlaybackManager.previousSong()
    }

    fun seekTo(seconds: Int) {
        PlaybackManager.seekTo(seconds)
    }

    fun toggleShuffle() {
        PlaybackManager.toggleShuffle()
    }

    fun toggleRepeat() {
        PlaybackManager.toggleRepeat()
    }

    fun toggleFavorite(songIndex: Int) {
        PlaybackManager.toggleFavorite(songIndex)
    }

    fun toggleLyrics() {
        PlaybackManager.toggleLyrics()
    }

    fun updateSearchQuery(query: String) {
        PlaybackManager.updateSearchQuery(query)
    }

    fun updateSongLyrics(songId: Int, newLyrics: String) {
        PlaybackManager.updateSongLyrics(songId, newLyrics)
    }

    fun importSongFromUri(context: Context, uri: android.net.Uri) {
        PlaybackManager.importSongFromUri(context, uri)
    }

    fun toggleQueueSheet(show: Boolean) {
        PlaybackManager.toggleQueueSheet(show)
    }

    fun createAlbum(title: String, coverUri: String?, songIds: List<Int>) {
        PlaybackManager.createAlbum(title, coverUri, songIds)
    }

    fun updateAlbum(albumId: Int, newTitle: String, newCoverUri: String?, newSongIds: List<Int>) {
        PlaybackManager.updateAlbum(albumId, newTitle, newCoverUri, newSongIds)
    }

    fun deleteAlbum(albumId: Int) {
        PlaybackManager.deleteAlbum(albumId)
    }
}
