package com.example.db

import com.example.model.Album
import kotlinx.coroutines.flow.Flow

class AlbumRepository(private val albumDao: AlbumDao) {
    val allAlbums: Flow<List<Album>> = albumDao.getAllAlbums()

    suspend fun insert(album: Album): Long = albumDao.insertAlbum(album)

    suspend fun update(album: Album) = albumDao.updateAlbum(album)

    suspend fun delete(album: Album) = albumDao.deleteAlbum(album)

    suspend fun getAlbumById(id: Int): Album? = albumDao.getAlbumById(id)
}
