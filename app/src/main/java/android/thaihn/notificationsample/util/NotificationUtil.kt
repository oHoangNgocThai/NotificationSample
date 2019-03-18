package android.thaihn.notificationsample.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import android.thaihn.notificationsample.R
import android.thaihn.notificationsample.entity.ActionType
import android.thaihn.notificationsample.entity.DataNotification
import android.thaihn.notificationsample.services.firebase.SampleMessagingService
import android.thaihn.notificationsample.ui.detail.DetailsActivity
import android.thaihn.notificationsample.ui.MainActivity
import android.thaihn.notificationsample.ui.detail.MyBroadcast
import android.widget.RemoteViews
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class NotificationUtil(context: Context) {

    companion object {
        const val DATA_TYPE = "type"
        const val DATA_CHANNEL = "channel"
        const val DATA_SENDER_ID = "sender_id"
        const val DATA_SENDER_AVATAR = "sender_avatar"

        const val CHANNEL_COMMON = "channel_common"
        const val REQUEST_CODE_PENDING_INTENT = 22
        const val REQUEST_CODE_ACTION_MORE = 23
        const val REQUEST_CODE_ACTION_REPLY = 11

        const val BROADCAST_ACTION_MORE = "broadcast_action_more"
        const val EXTRA_NOTIFICATION_ID = "notification_id"

        const val KEY_TEXT_REPLY = "key_text_reply"
    }

    private val mContext = context

    fun showNotification(
        notificationId: Int,
        data: DataNotification?,
        notification: RemoteMessage.Notification?,
        intent: Intent
    ) {
        val notificationManager =
            mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = data?.channel
            val descriptionText = notification?.body
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(data?.channel, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
            val builder = createNotificationBuilder(data, notification, intent)
            notificationManager.notify(notificationId, builder.build())
        } else {
            val avatar = data?.sender_avatar
            if (avatar != null) {
                // Notification with image avatar

            } else {
                val builder = createNotificationBuilder(data, notification, intent)
                // Add action
//                val action = createAction(ActionType.ACTIVITY)
//                builder.addAction(R.drawable.ic_more_horiz_black_24dp,
//                    mContext.getString(R.string.notification_action_more),
//                    action)

                // Add reply
                val action = createReplyAction(notificationId)
                builder.addAction(action)

                notificationManager.notify(notificationId, builder.build())
            }
        }
    }

    fun clearNotification(notificationId: Int) {
        with(NotificationManagerCompat.from(mContext)) {
            cancel(notificationId)
        }
    }

    private fun createReplyAction(notificationId: Int): NotificationCompat.Action {
        val replyLabel = "Enter your message"
        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }

        val intent = Intent(mContext, DetailsActivity::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val replyPendingIntent = PendingIntent.getActivity(
            mContext,
            REQUEST_CODE_ACTION_REPLY,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_send_black_24dp,
            "Reply",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun createAction(type: ActionType): PendingIntent {
        when (type) {
            ActionType.ACTIVITY -> {
                val intent = Intent(mContext, DetailsActivity::class.java)
                return PendingIntent.getActivity(
                    mContext,
                    REQUEST_CODE_ACTION_MORE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            ActionType.SERVICE -> {
                val intent = Intent(mContext, SampleMessagingService::class.java)
                return PendingIntent.getService(
                    mContext,
                    REQUEST_CODE_ACTION_MORE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            ActionType.BROADCAST -> {
                val intent = Intent(mContext, MyBroadcast::class.java)
                intent.action = BROADCAST_ACTION_MORE
                intent.putExtra(EXTRA_NOTIFICATION_ID, 1)
                return PendingIntent.getBroadcast(
                    mContext,
                    REQUEST_CODE_ACTION_MORE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            else -> {
                val intent = Intent(mContext, MainActivity::class.java)
                return PendingIntent.getActivity(
                    mContext,
                    REQUEST_CODE_ACTION_MORE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }


    private fun createNotificationBuilder(
        data: DataNotification?,
        notification: RemoteMessage.Notification?,
        intent: Intent
    ): NotificationCompat.Builder {
        var channel: String = CHANNEL_COMMON

        data?.channel?.let {
            channel = it
        }

        val pendingIntent = PendingIntent.getActivity(
            mContext,
            REQUEST_CODE_PENDING_INTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(mContext, channel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification?.title)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setContentText(notification?.body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notification?.body)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    fun parserData(data: Map<String, String>?): DataNotification? {
        data?.let {
            val jsonObject = JSONObject(it)
            return DataNotification(
                jsonObject.optString(DATA_TYPE, null),
                jsonObject.optString(DATA_CHANNEL, null),
                jsonObject.optString(DATA_SENDER_ID, null),
                jsonObject.optString(DATA_SENDER_AVATAR, null)
            )
        }
        return null
    }

    fun pushNotification(refreshToken: String) {
        val jsonNoti = JSONObject().apply {
            put(Constant.NOTI_BODY, "Demo message")
            put(Constant.NOTI_ICON, "R.drawable.luffy")
            put(Constant.NOTI_TITLE, "Username")
            put(Constant.NOTI_CLICK_ACTION, ".MainActivity")
        }
        val jsonData = JSONObject().apply {
            put(Constant.NOTI_CHAT, "Object to json")
            put(Constant.NOTI_TYPE, "chat")
        }
        val jsonRoot = JSONObject().apply {
            put(Constant.NOTI_TO, refreshToken)
            put(Constant.NOTI_NOTIFICATION, jsonNoti)
            put(Constant.NOTI_DATA, jsonData)
        }

        // Create request to link https://fcm.googleapis.com/fcm/send with method is POST
        // Don't forget add Header is: Authorization:Key=AIzaSyBzQM...
        // Key server get from FireBase console: Project Setting -> Cloud Messaging -> Legacy server key
    }

    private fun getRemoteViewChat( message: String, remoteMessage: RemoteMessage, bitmap: Bitmap): RemoteViews {
        val notificationView = RemoteViews(
           mContext.packageName,
            R.layout.layout_notification_chat
        )
        notificationView.setImageViewBitmap(R.id.imgAvatarNoti, bitmap)
        notificationView.setTextViewText(R.id.txtMessageNoti, remoteMessage.notification?.body)
        notificationView.setTextViewText(R.id.txtTitleNoti, remoteMessage.notification?.title)
        return notificationView
    }
}
