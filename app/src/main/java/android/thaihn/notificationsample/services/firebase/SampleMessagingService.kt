package android.thaihn.notificationsample.services.firebase

import android.content.Intent
import android.thaihn.notificationsample.ui.MainActivity
import android.thaihn.notificationsample.util.NotificationUtil
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class SampleMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = SampleMessagingService::class.java.simpleName
    }

    override fun onNewToken(refreshToken: String?) {
        Log.d(TAG, "RefreshToken: $refreshToken")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        parserNotification(remoteMessage)

        val notificationUtil = NotificationUtil(this)

        val data = notificationUtil.parserData(remoteMessage?.data)
        Log.d(TAG, "data:$data")

        val intent = Intent(this, MainActivity::class.java)
        notificationUtil.showNotification(
                1,
                data,
                remoteMessage?.notification,
                intent)
    }

    private fun parserNotification(remoteMessage: RemoteMessage?) {
        remoteMessage?.notification?.let { notification ->
            val title = notification.title
            val body = notification.body
            val icon = notification.icon
            val click_action = notification.clickAction
            val sound = notification.sound
            val tag = notification.tag
        }

        remoteMessage?.data?.let {
            val jsonData = JSONObject(it)
            val type = jsonData.optString(NotificationUtil.DATA_TYPE, "")
            val channel = jsonData.optString(NotificationUtil.DATA_CHANNEL, "")
            val senderId = jsonData.optString(NotificationUtil.DATA_SENDER_ID, "")
        }
    }
}
