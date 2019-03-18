package android.thaihn.notificationsample.ui.playmusic

import android.content.*
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.thaihn.notificationsample.R
import android.thaihn.notificationsample.databinding.ActivityPlayMusicBinding
import android.thaihn.notificationsample.entity.MusicState
import android.thaihn.notificationsample.services.MusicPlayerService

class PlayMusicActivity : AppCompatActivity() {

    companion object {
        private val TAG = PlayMusicActivity::class.java.simpleName
    }

    private var mMusicService: MusicPlayerService? = null
    private var mBound = false

    private var mState: Int? = null
    private var mCurrentPosition: Int? = 0

    private lateinit var playMusicBinding: ActivityPlayMusicBinding

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicBinder
            mMusicService = binder.service

            mState = mMusicService?.getState()
            mCurrentPosition = mMusicService?.getCurrentPosition()

            mState?.let {
                updateUi()
            }

            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mMusicService = null
            mBound = false
        }
    }

    private val mBroadcastReceive = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let {
                mState = mMusicService?.getState()
                when (it) {
                    MusicPlayerService.ACTION_PLAYING -> {
                        updateUi()
                    }
                    MusicPlayerService.ACTION_PAUSE -> {
                        updateUi()
                    }
                    MusicPlayerService.ACTION_PREPARE -> {
                        updateUi()
                    }
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, MusicPlayerService::class.java),
            mConnection,
            Context.BIND_AUTO_CREATE
        )
        registerReceiver(mBroadcastReceive, initIntentFilter())

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playMusicBinding = DataBindingUtil.setContentView(this, R.layout.activity_play_music)

        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = MusicPlayerService.ACTION_OPEN_APP
        }
        startService(intent)


        playMusicBinding.imgPlay.setOnClickListener {
            mMusicService?.chooseState()
        }

        playMusicBinding.imgNext.setOnClickListener {
            mMusicService?.playNextSong()
        }

        playMusicBinding.imgPrevious.setOnClickListener {
            mMusicService?.playPreviousSong()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
        unregisterReceiver(mBroadcastReceive)
    }

    private fun updateUi() {
        when (mState) {
            MusicState.PLAYING.value -> {
                playMusicBinding.imgPlay.setImageResource(R.drawable.ic_pause_button_white)
            }
            MusicState.PAUSE.value -> {
                playMusicBinding.imgPlay.setImageResource(R.drawable.ic_media_play_symbol_white)
            }
        }
    }

    private fun initIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(MusicPlayerService.ACTION_PAUSE)
            addAction(MusicPlayerService.ACTION_PLAYING)
        }
    }
}
