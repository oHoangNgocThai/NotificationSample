package android.thaihn.notificationsample.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder
import android.thaihn.notificationsample.R
import android.thaihn.notificationsample.entity.MusicState
import android.thaihn.notificationsample.ui.playmusic.PlayMusicActivity

class MusicPlayerService : Service(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener {

    companion object {
        private val TAG = MusicPlayerService::class.java.simpleName

        const val ACTION_PLAYING = "action-playing"
        const val ACTION_PAUSE = "action-pause"
        const val ACTION_PREPARE = "action-prepare"
        const val ACTION_NEXT = "action-next"
        const val ACTION_PREVIOUS = "action-previous"
        const val ACTION_OPEN_APP = "action-open-app"
        const val ACTION_PLAY_PAUSE = "action-play-pause"

        const val DEFAULT_ID_NOTIFICATION = 111

        private val ORDER_ACTION_PREVIOUS = 0
        private val ORDER_ACTION_PLAY_PAUSE = 1
        private val ORDER_ACTION_NEXT = 2

        private val CHANNEL_ID = "channel_music"
        private val CHANNEL_NAME = "Music"
    }

    private val mBinder = MusicBinder()

    inner class MusicBinder : Binder() {
        val service: MusicPlayerService
            get() = this@MusicPlayerService
    }

    private var mMediaPlayer: MediaPlayer? = null

    // Notification
    private var mPendingNext: PendingIntent? = null
    private var mPendingPrevious: PendingIntent? = null
    private var mPendingPlay: PendingIntent? = null
    private var mPendingItem: PendingIntent? = null
    private var mBuilder: NotificationCompat.Builder? = null
    private var mNotificationManager: NotificationManagerCompat? = null

    private val mListMusic =
        arrayListOf(R.raw.dung_nguoi_dung_thoi_diem, R.raw.thaythe, R.raw.buon_cua_anh)

    private var mCurrentPosition = 1

    private var mState = MusicState.PREPARE.value

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = NotificationManagerCompat.from(this)
        createNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleNotificationAction(intent)
        return START_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        mMediaPlayer?.start()
        mState = MusicState.PLAYING.value
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        playNextSong()
    }

    private fun prepareSong() {
        reset()
        mState = MusicState.PREPARE.value
        updateNotification()

        createSong()

        sendBroadcast(Intent(ACTION_PREPARE))
    }

    private fun createSong() {
        mMediaPlayer = MediaPlayer.create(this, mListMusic[mCurrentPosition])
        mMediaPlayer?.setOnCompletionListener(this)
        mMediaPlayer?.setOnPreparedListener(this)
    }

    fun getState(): Int {
        return mState
    }

    fun getCurrentPosition(): Int {
        return mCurrentPosition
    }

    fun startMusic() {
        mMediaPlayer?.start()
        mState = MusicState.PLAYING.value
        updateNotification()
        sendBroadcast(Intent(ACTION_PLAYING))
    }

    fun pauseMusic() {
        mMediaPlayer?.pause()
        mState = MusicState.PAUSE.value
        updateNotification()
        sendBroadcast(Intent(ACTION_PAUSE))
    }

    fun reset() {
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    fun chooseState() {
        if (mState == MusicState.PLAYING.value) {
            pauseMusic()
        } else {
            startMusic()
        }
    }

    private fun playSong(position: Int) {
        if (mListMusic.size == 0) {
            return
        }

        if (position == mCurrentPosition) {
            return
        }

        mCurrentPosition = position
        prepareSong()
    }

    fun playNextSong() {
        if (mListMusic.size == 0 && mCurrentPosition == mListMusic.size - 1) {
            mCurrentPosition = -1
        }
        mCurrentPosition++
        prepareSong()
        sendBroadcast(Intent(ACTION_NEXT))
    }

    fun playPreviousSong() {
        if (mListMusic.size == 0 || mCurrentPosition == 0) {
            return
        }
        mCurrentPosition--
        prepareSong()
        sendBroadcast(Intent(ACTION_PREVIOUS))
    }

    private fun handleNotificationAction(intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            ACTION_OPEN_APP -> {
                playSong(0)
                updateNotification()
            }
            ACTION_NEXT -> {
                playNextSong()
                updateNotification()
            }
            ACTION_PREVIOUS -> {
                playPreviousSong()
                updateNotification()
            }
            ACTION_PLAY_PAUSE -> {
                chooseState()
                updateNotification()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_LOW
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            manager.createNotificationChannel(mChannel)
        }
    }

    private fun createNotification() {
        val intentItem = Intent(this, PlayMusicActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = ACTION_OPEN_APP
        }
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(intentItem)
        mPendingItem = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val intentNext = Intent(this, MusicPlayerService::class.java).apply {
            action = ACTION_NEXT
        }
        mPendingNext = PendingIntent.getService(this, 0, intentNext, 0)

        val intentPrevious = Intent(this, MusicPlayerService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        mPendingPrevious = PendingIntent.getService(this, 0, intentPrevious, 0)

        val intentPlay = Intent(this, MusicPlayerService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        mPendingPlay = PendingIntent.getService(this, 0, intentPlay, 0)
        updateNotification()
    }

    private fun updateNotification() {
        if (mState == MusicState.PLAYING.value) {
            startForeground(DEFAULT_ID_NOTIFICATION, buildNotification())
        } else {
            buildNotification()?.let {
                mNotificationManager?.notify(DEFAULT_ID_NOTIFICATION, it)
            }
            stopForeground(false)
        }
    }

    private fun buildNotification(): Notification? {
        val iconPlayPause = if (mState == MusicState.PLAYING.value) {
            R.drawable.ic_pause_button_white
        } else {
            R.drawable.ic_media_play_symbol_white
        }
        mBuilder = NotificationCompat.Builder(this)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle("Title song")
            .setContentText("Singer song")
            .setSmallIcon(R.drawable.ic_music_player)
            .setContentIntent(mPendingItem)
            .addAction(R.drawable.ic_previous_white, "", mPendingPrevious)
            .addAction(iconPlayPause, "", mPendingPlay)
            .addAction(R.drawable.ic_next_white, "", mPendingNext)
            .setStyle(
                android.support.v4.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(
                        ORDER_ACTION_PREVIOUS,
                        ORDER_ACTION_PLAY_PAUSE, ORDER_ACTION_NEXT
                    )
            )
        return mBuilder?.build()
    }
}

