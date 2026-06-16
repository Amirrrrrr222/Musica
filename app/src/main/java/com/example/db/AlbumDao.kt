package com.example.db

import androidx.room.*
import com.example.model.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY id DESC")
    fun getAllAlbums(): Flow<List<Album>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    @Update
    suspend fun updateAlbum(album: Album)

    @Delete
    suspend fun deleteAlbum(album: Album)

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    suspend fun getAlbumById(id: Int): Album?
}
