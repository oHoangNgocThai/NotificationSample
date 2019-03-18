package android.thaihn.notificationsample.ui.detail

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.RemoteInput
import android.support.v7.app.AppCompatActivity
import android.thaihn.notificationsample.R
import android.thaihn.notificationsample.databinding.ActivityDetailsBinding
import android.thaihn.notificationsample.util.NotificationUtil
import android.util.Log

class DetailsActivity : AppCompatActivity() {

    companion object {
        private val TAG = DetailsActivity::class.java.simpleName
    }

    private lateinit var detailBinding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailBinding = DataBindingUtil.setContentView(this, R.layout.activity_details)
        handleReplyInput()
    }

    private fun handleReplyInput() {
        RemoteInput.getResultsFromIntent(intent)?.let {
            val input = it.getCharSequence(NotificationUtil.KEY_TEXT_REPLY).toString()
            Log.d(TAG, "handleReplyInput: bundle:$it---input:$input")
            detailBinding.tvDetail.text = input

            // clear notification
            intent?.getIntExtra(NotificationUtil.EXTRA_NOTIFICATION_ID, 0)?.let {
                Log.d(TAG, "notifiationId:$it")
                if (it != 0) {
                    val notificationUtil = NotificationUtil(this)
                    notificationUtil.clearNotification(it)
                }
            }

        }

    }
}
