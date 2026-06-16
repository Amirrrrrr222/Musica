package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val coverUri: String? = null,
    val songIdsJson: String // Comma-separated list of song IDs
) {
    fun getSongIds(): List<Int> {
        return if (songIdsJson.isBlank()) {
            emptyList()
        } else {
            songIdsJson.split(",").mapNotNull { it.trim().toIntOrNull() }
        }
    }

    companion object {
        fun createSongIdsJson(ids: List<Int>): String {
            return ids.joinToString(",")
        }
    }
}
