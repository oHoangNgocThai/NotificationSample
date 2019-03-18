package android.thaihn.notificationsample.ui

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.RemoteInput
import android.support.v7.app.AppCompatActivity
import android.thaihn.notificationsample.R
import android.thaihn.notificationsample.databinding.ActivityMainBinding
import android.thaihn.notificationsample.entity.DataNotification
import android.thaihn.notificationsample.ui.detail.DetailsActivity
import android.thaihn.notificationsample.ui.playmusic.PlayMusicActivity
import android.thaihn.notificationsample.util.Constant
import android.thaihn.notificationsample.util.NotificationUtil
import android.thaihn.notificationsample.util.PreferenceUtil
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this) { instanceIdResult ->
            val refreshToken = instanceIdResult.token
            Log.d(TAG, "refreshToken: $refreshToken")
            val oldToken = PreferenceUtil.instance[Constant.PREF_TOKEN, String::class.java]
            if (oldToken != refreshToken) {
                PreferenceUtil.instance.put(Constant.PREF_TOKEN, refreshToken)
            }
        }

        mainBinding.btnPlayMusic.setOnClickListener {
            startActivity(Intent(this, PlayMusicActivity::class.java))
        }

        handleDirectionNotifcation()
        handleReplyInput()
    }

    private fun handleReplyInput() {
        RemoteInput.getResultsFromIntent(intent)?.let {
            val input = it.getCharSequence(NotificationUtil.KEY_TEXT_REPLY).toString()
            mainBinding.tvMain.text = input
        }
    }

    private fun handleDirectionNotifcation() {
        val data = parserDataFromBundle(intent.extras)
        if (data.type != null) {
            startActivity(Intent(this, DetailsActivity::class.java))
        } else {

        }
    }


    private fun parserDataFromBundle(bundle: Bundle?): DataNotification {
        return DataNotification(
            bundle?.getString(NotificationUtil.DATA_TYPE),
            bundle?.getString(NotificationUtil.DATA_CHANNEL),
            bundle?.getString(NotificationUtil.DATA_SENDER_ID),
            bundle?.getString(NotificationUtil.DATA_SENDER_AVATAR)
        )
    }
}
