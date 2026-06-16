package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.media.MediaMetadata
import com.example.MainActivity
import com.example.R
import com.example.model.Song
import com.example.viewmodel.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var isForeground = false
    private var mediaSession: MediaSession? = null

    companion object {
        const val NOTIFICATION_ID = 4040
        const val CHANNEL_ID = "musica_playback_channel"
        const val CHANNEL_NAME = "Musica Playback Controls"

        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_NEXT = "com.example.action.NEXT"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Create and configure standard Android MediaSession for system/lockscreen/bluetooth sync
        mediaSession = MediaSession(this, "MusicaMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    PlaybackManager.togglePlayPause()
                }

                override fun onPause() {
                    PlaybackManager.togglePlayPause()
                }

                override fun onSkipToNext() {
                    PlaybackManager.nextSong()
                }

                override fun onSkipToPrevious() {
                    PlaybackManager.previousSong()
                }

                override fun onSeekTo(pos: Long) {
                    PlaybackManager.seekTo((pos / 1000).toInt())
                }
            })
            isActive = true
        }

        // Sync with the PlaybackManager state changes in real-time
        serviceScope.launch {
            PlaybackManager.uiState.collectLatest { state ->
                val song = state.currentSong
                if (song != null) {
                    updateMediaSessionState(song, state.isPlaying, state.progressSeconds)
                    val notification = buildMediaStyleNotification(song, state.isPlaying)
                    updateNotification(notification)
                } else {
                    // Stop service if there is no active song
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> PlaybackManager.togglePlayPause()
            ACTION_PREVIOUS -> PlaybackManager.previousSong()
            ACTION_NEXT -> PlaybackManager.nextSong()
        }
        return START_NOT_STICKY
    }

    private fun updateMediaSessionState(song: Song, isPlaying: Boolean, progressSeconds: Int) {
        val session = mediaSession ?: return

        // 1. Set metadata
        val builder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, song.durationSeconds * 1000L)

        // Try downloading/decoding album artwork bitmap for system display
        if (!song.albumArtUri.isNullOrEmpty() && song.albumArtUri.startsWith("content://")) {
            try {
                contentResolver.openInputStream(Uri.parse(song.albumArtUri))?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap)
                    }
                }
            } catch (e: Exception) {
                // Keep default metadata representation on exception
            }
        }
        session.setMetadata(builder.build())

        // 2. Set playback state and seek progress
        val stateBuilder = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SEEK_TO or
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE
            )
            .setState(
                if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                progressSeconds * 1000L,
                1.0f
            )
        session.setPlaybackState(stateBuilder.build())
    }

    private fun updateNotification(notification: Notification) {
        if (!isForeground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            isForeground = true
        } else {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private suspend fun buildMediaStyleNotification(song: Song, isPlaying: Boolean): Notification = withContext(Dispatchers.Default) {
        // Open app on click
        val openAppIntent = Intent(this@MusicService, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val openAppPendingIntent = PendingIntent.getActivity(this@MusicService, 0, openAppIntent, flags)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this@MusicService, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this@MusicService)
        }

        // Setup big album art large icon
        var largeIconBitmap: android.graphics.Bitmap? = null
        if (!song.albumArtUri.isNullOrEmpty() && song.albumArtUri.startsWith("content://")) {
            largeIconBitmap = try {
                contentResolver.openInputStream(Uri.parse(song.albumArtUri))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                null
            }
        }
        if (largeIconBitmap == null) {
            largeIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_album_art)
        }

        // Add play/pause control icons
        val prevAction = Notification.Action.Builder(
            android.R.drawable.ic_media_previous,
            "Previous",
            getPendingIntent(ACTION_PREVIOUS)
        ).build()

        val playPauseAction = Notification.Action.Builder(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            "Play/Pause",
            getPendingIntent(ACTION_PLAY_PAUSE)
        ).build()

        val nextAction = Notification.Action.Builder(
            android.R.drawable.ic_media_next,
            "Next",
            getPendingIntent(ACTION_NEXT)
        ).build()

        builder.setContentTitle(song.title)
            .setContentText(song.artist)
            .setSubText(song.album)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(largeIconBitmap)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isPlaying)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)

        // Set standard native Android MediaStyle to render beautiful blurred card inside OS notification drawer
        val sessionToken = mediaSession?.sessionToken
        if (sessionToken != null) {
            val mediaStyle = Notification.MediaStyle()
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
            builder.setStyle(mediaStyle)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }

        builder.build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { this.action = action }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getService(this, action.hashCode(), intent, flags)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls active song playback on Musica"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceJob.cancel()
        mediaSession?.release()
        super.onDestroy()
    }
}
