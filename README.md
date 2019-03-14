# NotificationSample

# Introduction

* **Notification** là một tin nhắn mà Android hiển thị ra bên ngoài giao diện người dùng, nhằm cung cấp cho người dùng lời nhắc, liên lạc từ người khác hoặc cung cấp kịp thời khác từ ứng dụng của bạn. Người dùng có thể nhấn vào notification để mở ra ứng dụng của bạn hoặc thực hiện hành động trực tiệp trên thông báo.

* Thành phần của các thông báo như sau:

[](https://developer.android.com/images/ui/notifications/notification-callouts_2x.png)


`1` Small icon: Trường này là bắt buộc và có thể set qua `setSmallIcon()`.

`2` App name: Được cung cấp bởi hệ thống Android.

`3` Time stamp: Được cung cấp bởi hệ thống nhưng bạn có thể xử lý bằng `setWhen()` và ẩn nó đi bằng `setShowWhen(false)`. 

`4` Large icon: Đây là 1 option của Notification(thông thường sử dụng hình ảnh của danh bạ, không nên sử dụng icon ứng dụng của bạn). Set giá trị của nó qua hàm `setLargeIcon()`

`5` Title: Đây là 1 option có thể đưa thêm vào bằng cách sử dụng `setContentTitle()`.

`6` Text: Đây là một option có thể thêm bằng cách sử dụng `setContentText()`.

* Ngoài các thành phần chính kia, bạn cũng có thể thêm action cho notification, nhưng tối đa chỉ được 3 action mà thôi.
* Cũng có thể kể đến một vài chức năng như **expandable notification**, **notification group**, **notification channel**, ...
 
# Config Notification

> Hiện nay service được sử dụng nhiều nhất để gửi thông báo đi các thiết bị là của FireBase cung cấp - **FireBase Cloud Messaging**.

## Config service

* Kết nối project với FireBase, xem tại [đây](https://firebase.google.com/docs/android/setup)

* Tạo 1 service để có thể lắng nghe được những sự kiện từ FireBase gửi về và cũng sinh ra `refreshToken` để FireBase quản lý token gửi về:

```
class SampleMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = SampleMessagingService::class.java.simpleName
    }

    override fun onNewToken(refreshToken: String?) {
        Log.d(TAG, "RefreshToken:refreshToken")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d(TAG, "RemoteMessage:$remoteMessage")
    }
}
```
> Chú ý: Nếu ở trong Java thì bạn phải tạo 2 service khác nhau. Việc lấy token sẽ được lấy ở một service khác như `FirebaseInstanceIdService`. Nếu sử dụng trong Java thì chỉ cần tạo 1 service extend FirebaseInstanceIdService rồi override phương thức `onTokenRefresh()`.

* Nếu muốn lấy `refreshToken` ở ngoài Activity ta làm như sau: 

```
FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( MyActivity.this,  new OnSuccessListener<InstanceIdResult>() {
     @Override
     public void onSuccess(InstanceIdResult instanceIdResult) {
           String newToken = instanceIdResult.getToken();
           Log.e("newToken",newToken);

     }
 });
```

* Việc sử dụng `newToken` được sinh ra có thể xử lý được vài trường hợp như:
    
    * Ứng dụng xóa Instance ID
    * Ứng dụng được restored ở một thiết bị mới
    * Người dùng gỡ ứng dụng và cài lại
    * Người dùng clear data của ứng dụng. 

* Đừng quên việc khai báo service bên trong file cấu hình AndroidManifest.xml: 

```
<service
    android:name=".services.SampleMessagingService"
    android:stopWithTask="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGIN_EVENT" />
    </intent-filter>
</service>
```

# Handle Notification

## Receive Notification
> Notification được gửi về ứng dụng sẽ được service nhận qua `RemoteMessage` lưu trữ các thông tin cần thiết.
* **RemoteMessage**: Những thông tin của notification được lưu trữ bên trong object này, có thể chú ý đến 2 phần chính là `notification` và `data`.
    
    * `Notification`: Chứa các thông tin cơ bản cần có của 1 thông báo như **title**, **body**, **icon**, **tag**, **click_action**, **android_channel_id**. Thông thường sẽ không cần dùng đến hết cả những trường trên, chỉ là nếu ứng dụng không hoạt động hoặc không có kết nối thì hệ thống sẽ dựa vào những trường này để hiển thị thông báo mặc định của hệ thống.
    * `data`: Phần này quan trọng cho việc gửi về những thông tin cần thiết khác ở dạng **Map<String,String>**. Khi cần custom thông báo hoặc gửi những dữ liệu dành cho việc handle click thì sẽ rất cần thiết.
    
* Khi ứng dụng đang chạy thì những notification được gửi về sẽ được trả về trong hàm **onMessageReceived** trong MessagingService. Nếu ứng dụng đang tắt hoặc không có kết nối, notification sau đó sẽ được hệ thống Android gửi về như thông báo bình thường.
* Việc parser dữ liệu của remoteMessage được thực hiện bên trong MessageService như sau:

```
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
```

## Create Notification
> Sau khi đã nhận dữ liệu notification từ service về, việc tiếp theo cần làm là hiển thị chúng lên giao diện của người dùng.

### Simple Notification
* Thêm dependency `support-compat` vào trong file build.gradle module-level để sử dụng `NotificationCompat`: 

```
dependencies {
    implementation "com.android.support:support-compat:28.0.0"
}
```

* Sau khi nhận được dữ liệu notification bên trong object **RemoteMessage**, tạo một Notification Builder sử dụng dữ liệu đó để cấu hình cho Notification Builder:

```
private fun createNotificationBuilder(data: DataNotification?, notification: RemoteMessage.Notification?, intent: Intent): NotificationCompat.Builder {
    var channel: String = CHANNEL_COMMON

    data?.channel?.let {
        channel = it
    }

    val pendingIntent = PendingIntent.getActivity(mContext, REQUEST_CODE_PENDING_INTENT, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    return NotificationCompat.Builder(mContext, channel)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(notification?.title)
        .setDefaults(Notification.DEFAULT_SOUND)
        .setContentText(notification?.body)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText(notification?.body)
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
}
```

* Hiển thị thông báo lên màn hình sử dụng **Notification Manager**, với tham số là **builder** và **notificationId**. Việc quản lý ID của thông báo rất quan trọng vì nó cũng dành cho việc chỉnh sửa hoặc xóa notification.

```
fun showNotification(notificationId: Int, data: DataNotification?, notification: RemoteMessage.Notification?, intent: Intent) {
    val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            notificationManager.notify(notificationId, builder.build())
        }
    }
}
```
> Trên Android 8.0, việc hiển thị thông báo cần tạo ra 1 channel để nhóm các thông báo lại với nhau. Nếu từ Android 8.0 trở xuống thì vẫn hiển thị thông báo theo như cách thông thường.

### Actions Notification
* Thêm **Action** vào bên trong Notification, bạn có thể tạo ra tối đa 3 action tương ứng với 3 **PendingIntent** khác nhau. Những Intent này có thể mở ra Activity, start Service hoặc là gửi BroadCast.

```
when (type) {
            ActionType.ACTIVITY -> {
                val intent = Intent(mContext, DetailsActivity::class.java)
                return PendingIntent.getActivity(mContext, REQUEST_CODE_ACTION_MORE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            ActionType.SERVICE -> {
                val intent = Intent(mContext, SampleMessagingService::class.java)
                return PendingIntent.getService(mContext, REQUEST_CODE_ACTION_MORE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            ActionType.BROADCAST -> {
                val intent = Intent(mContext, MyBroadcast::class.java)
                intent.action = BROADCAST_ACTION_MORE
                intent.putExtra(EXTRA_NOTIFICATION_ID, 1)
                return PendingIntent.getService(mContext, REQUEST_CODE_ACTION_MORE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            else -> {
                val intent = Intent(mContext, MainActivity::class.java)
                return PendingIntent.getActivity(mContext, REQUEST_CODE_ACTION_MORE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

```

* Action sẽ được nhận ở Activity qua **Intent**, Service qua **onStartCommand** và BroadCast là ở trong **onReceive()**. Bạn có thể gửi thêm dữ liệu vào intent qua việc sử dụng **putExtra()**.

### Reply Notification

* Thêm ô nhập reply ngay trên thanh notification dựa vào thể hiện của **RemoteInput.Builder**:

```
private fun createReplyAction(notificationId: Int): NotificationCompat.Action {
        val replyLabel = "Enter your message"
        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }

        val intent = Intent(mContext, DetailsActivity::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val replyPendingIntent = PendingIntent.getActivity(mContext, REQUEST_CODE_ACTION_REPLY, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        return NotificationCompat.Action.Builder(R.drawable.ic_send_black_24dp, "Reply", replyPendingIntent)
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }
```

* Sau đó xử lý việc nhận action của **RemoteInput** ở Activity, Service hay Broadcast đều dựa vào dữ liệu của Intent gửi đến. Ở đây ví dụ lấy ở trong Activity:

```
RemoteInput.getResultsFromIntent(intent)?.let {
            val input = it.getCharSequence(NotificationUtil.KEY_TEXT_REPLY).toString()
            detailBinding.tvDetail.text = input

            // clear notification
            intent?.getIntExtra(NotificationUtil.EXTRA_NOTIFICATION_ID, 0)?.let {
                if (it != 0) {
                    val notificationUtil = NotificationUtil(this)
                    notificationUtil.clearNotification(it)
                }
            }

        }
```
> Sau khi nhận được input text thì thực hiện việc xóa notification đi. Đa số trong trường hợp này là gửi về service để tiến hành gửi đi mà không cần dùng giao diện. 

### Add a progress bar

* Thêm progress bar trong trường hợp muốn download một file nào đó mà hiển thị thông báo.

```
val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
    setContentTitle("Picture Download")
    setContentText("Download in progress")
    setSmallIcon(R.drawable.ic_notification)
    setPriority(NotificationCompat.PRIORITY_LOW
}
val PROGRESS_MAX = 100
val PROGRESS_CURRENT = 0
NotificationManagerCompat.from(this).apply {
    // Issue the initial notification with zero progress
    builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
    notify(notificationId, builder.build())

    // Do the job here that tracks the progress.
    // Usually, this should be in a 
    // worker thread 
    // To show progress, update PROGRESS_CURRENT and update the notification with:
    // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
    // notificationManager.notify(notificationId, builder.build());

    // When done, update the notification one more time to remove the progress bar
    builder.setContentText("Download complete")
            .setProgress(0, 0, false)
    notify(notificationId, builder.build())
}
``` 

* Việc cập nhật thông báo này cũng nên được thực hiện bởi IntentService, sau khi đó sẽ tự kết thúc.

### Lock screen visibility and Update

* Để cài đặt việc hiển thị trên màn hình khóa, chúng ta sử dụng **Visibility()** để thực hiện việc này với các tham số sau:

    * **VISIBILITY_PUBLIC**: Hiển thị tất cả nội dung của thông báo
    * **VISIBILITY_SECRET**: Không hiển thị bất kì thông báo nào trên màn hình khóa.
    * **VISIBILITY_PRIVATE**: Hiển thị một vài thông tin cơ bản như icon, title nhưng không phải là toàn bộ.
    
* Để cập nhật thông báo, chúng ta sử dụng **NotificationManagerCompat.notify()** với id notification đang sử dụng. Bạn cũng có thể thiết đặt **setOnlyAlertOnce()** để những thiết đặt khác(âm thanh, ...) không hiển thị ở lần cập nhật sau.
* Để xóa một thông báo, có vài cách dưới đâu:
    
    * **setAutoCancel()**: Khi người dùng click vào notificaiton.
    * **cancel()**: Truyền vào notification id để remove notification.
    * **cancelAll()**: Xóa toàn bộ thông báo
    * **setTimeoutAfter()**: Xóa thông báo sau một khoảng thời gian nhất định.