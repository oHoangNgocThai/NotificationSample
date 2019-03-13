package android.thaihn.notificationsample.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SampleMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = SampleMessagingService::class.java.simpleName
    }

    override fun onNewToken(refreshToken: String?) {
        Log.d(TAG, "RefreshToken: $refreshToken")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d(TAG, "RemoteMessage: $remoteMessage")
    }
}
