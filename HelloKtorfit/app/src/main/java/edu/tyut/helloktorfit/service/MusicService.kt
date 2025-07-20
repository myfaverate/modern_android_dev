package edu.tyut.helloktorfit.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import edu.tyut.helloktorfit.R
import kotlinx.coroutines.coroutineScope

private const val TAG: String = "MusicService"
private const val CHANNEL_ID: String = "MusicChannelId"
private const val NOTIFICATION_ID: Int = 0x0000_0003
private const val REQUEST_CODE: Int = 0x0000_0003
private const val PLAY_PAUSE_ACTION: String = "playPauseAction"
private const val PREV_ACTION: String = "prevAction"
private const val NEXT_ACTION: String = "nextAction"

@UnstableApi
internal class MusicService internal constructor() : MediaSessionService() {


    internal companion object {
        internal fun getServiceIntent(context: Context): Intent {
            return Intent(context, MusicService::class.java)
        }
    }

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }

    private val player: ExoPlayer by lazy {
        val loadControl: DefaultLoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                5_000,  // 最小缓冲 5 秒
                15_000, // 最大缓冲 15 秒
                1_000,  // 播放前最小缓冲
                2_000   // 重新缓冲后重新开始播放前的最小缓冲时间
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        // zsh
        ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()
    }

    private val mediaSessionCallback: MediaSession.Callback = @UnstableApi
    object : MediaSession.Callback {

        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            Log.i(TAG, "onMediaButtonEvent -> session: $session, controller: $controllerInfo, intent: $intent")
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            Log.i(TAG, "onSetMediaItems -> session: ${mediaSession.id}, controller: $controller, mediaItems: $mediaItems, startIndex: $startIndex, startPositionMs: $startPositionMs")
            return super.onSetMediaItems(
                mediaSession,
                controller,
                mediaItems,
                startIndex,
                startPositionMs
            )
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.i(TAG, "onConnect -> session: ${session.id}, controller: $controller")
            return super.onConnect(session, controller)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.i(TAG, "onCustomCommand -> session: ${session.id}, controller: $controller, customCommand: $customCommand, args: $args")
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    private val mediaSession: MediaSession by lazy {
        MediaSession.Builder(this, player)
            .setCallback(mediaSessionCallback)
            .build()
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate...")
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.e(TAG, "onPlayerError -> error: ${error.message} ", error)
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                Log.e(TAG, "onPlayerErrorChanged -> error: ${error?.message}", error)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when(playbackState){
                    Player.STATE_IDLE -> {
                        Log.i(TAG, "onPlaybackStateChanged -> 播放器空闲...")
                    }
                    Player.STATE_BUFFERING -> {
                        Log.i(TAG, "onPlaybackStateChanged -> 缓冲中...")
                    }
                    Player.STATE_READY -> {
                        Log.i(TAG, "onPlaybackStateChanged -> 准备就绪...")
                    }
                    Player.STATE_ENDED -> {
                        Log.i(TAG, "onPlaybackStateChanged -> 播放结束...")
                    }
                }
            }

            @OptIn(UnstableApi::class)
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log.i(TAG, "onIsPlayingChanged -> isPlaying: $isPlaying")
                updateNotification(createNotification())
                player.currentMediaItem?.mediaMetadata?.apply {
                    toBundle().keySet()?.forEach {
                        Log.i(TAG, "toBundle onIsPlayingChanged -> key: $it, value: ${toBundle().get(it)}")
                    }
                    extras?.keySet()?.forEach {
                        Log.i(TAG, "extras onIsPlayingChanged -> key: $it, value: ${extras?.get(it)}")
                    }
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand -> intent: $intent, flags: $flags, startId: $startId")
        if (intent?.action == PLAY_PAUSE_ACTION){
            Log.i(TAG, "onStartCommand play pause...")
            if (player.isPlaying){
                player.pause()
            } else {
                player.play()
            }
            val notification: Notification = createNotification()
            updateNotification(notification)
            return super.onStartCommand(intent, flags, startId)
        }
        if (intent?.action == PREV_ACTION){
            Log.i(TAG, "onStartCommand prev...")
            if (player.hasPreviousMediaItem()){
                player.seekToPreviousMediaItem()
            } else {
                Toast.makeText(this, "当前已是第一首歌曲", Toast.LENGTH_SHORT).show()
            }
            return super.onStartCommand(intent, flags, startId)
        }
        if (intent?.action == NEXT_ACTION){
            Log.i(TAG, "onStartCommand -> next...")
            if (player.hasNextMediaItem()){
                player.seekToNextMediaItem()
            }else {
                Toast.makeText(this, "当前已是最后一首歌曲", Toast.LENGTH_SHORT).show()
            }
            return super.onStartCommand(intent, flags, startId)
        }

        val channel: NotificationChannelCompat = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_MAX
        )
            .setName("音频播放")
            .build()
        notificationManager.createNotificationChannel(channel)
        val notification: Notification = createNotification()
        updateNotification(notification)

        val mediaItems = listOf<MediaItem>(
            MediaItem.Builder().setUri("http://192.168.31.90:8080/audio1.flac").build(),
            MediaItem.Builder().setUri("http://192.168.31.90:8080/audio2.flac").build(),
            MediaItem.Builder().setUri("http://192.168.31.90:8080/audio3.ogg").setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder().setTitle("策马奔腾").setArtist("凤凰传奇").build()).build(),
            MediaItem.Builder().setUri("http://192.168.31.90:8080/audio4.ogg").build(),
            MediaItem.Builder().setUri("http://192.168.31.90:8080/audio5.ogg").build(),
            MediaItem.Builder().setUri("http://192.168.31.90:8080/audio6.ogg").build(),
        )
        player.setMediaItems(mediaItems)
        player.prepare()
        player.play()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.i(TAG, "onGetSession -> controllerInfo: $controllerInfo")
        return mediaSession
    }

    @OptIn(UnstableApi::class)
    private fun createNotification(): Notification {
        val playPauseIntent: PendingIntent? = PendingIntentCompat.getService(this, REQUEST_CODE, getServiceIntent(this).setAction(PLAY_PAUSE_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT, false)
        val playPauseAction: NotificationCompat.Action
                = NotificationCompat.Action.Builder(IconCompat.createWithResource(this, if(player.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play), if(player.isPlaying) "暂停" else "播放", playPauseIntent).build()

        val prevIntent: PendingIntent? = PendingIntentCompat.getService(this, REQUEST_CODE, getServiceIntent(this).setAction(PREV_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT, false)
        val prevAction: NotificationCompat.Action
                = NotificationCompat.Action.Builder(IconCompat.createWithResource(this, android.R.drawable.ic_media_previous), "上一首", prevIntent).build()

        val nextIntent: PendingIntent? = PendingIntentCompat.getService(this, REQUEST_CODE, getServiceIntent(this).setAction(NEXT_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT, false)
        val nextAction: NotificationCompat.Action
                = NotificationCompat.Action.Builder(IconCompat.createWithResource(this, android.R.drawable.ic_media_next), "下一首", nextIntent).build()


        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
            .setShowActionsInCompactView(0, 1, 2)

        val title: String = player.currentMediaItem?.mediaMetadata?.title?.toString() ?: "未知歌曲"
        val artist: String = player.currentMediaItem?.mediaMetadata?.artist?.toString() ?: "未知作者"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setStyle(mediaStyle)
            .setContentTitle(title)
            .setContentText(artist)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .build()
    }

    private fun updateNotification(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            release()
            player.release()
        }
        Log.i(TAG, "onDestroy...")
    }


    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i(TAG, "onTaskRemoved -> intent: $rootIntent")
        rootIntent?.extras?.keySet()?.forEach {
            Log.i(TAG, "onTaskRemoved -> key: $it, value: ${rootIntent.extras?.get(it)}")
        }
        pauseAllPlayersAndStopSelf()
    }

}