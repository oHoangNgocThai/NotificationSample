package android.thaihn.notificationsample.ui.detail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyBroadcast : BroadcastReceiver() {

    companion object {
        private val TAG = MyBroadcast::class.java.simpleName
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "MyBroadcast: onReceive:$intent")
    }
}
