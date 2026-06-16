package com.example.model

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtRes: Int,
    val durationSeconds: Int,
    val lyrics: String,
    val albumArtUri: String? = null,
    val fileUri: String? = null
)
