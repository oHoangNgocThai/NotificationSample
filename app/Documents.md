# NotificationSample

# Introduction

* **Notification** là một tin nhắn mà Android hiển thị ra bên ngoài giao diện người dùng, nhằm cung cấp cho người dùng lời nhắc, liên lạc từ người khác hoặc cung cấp kịp thời khác từ ứng dụng của bạn. Người dùng có thể nhấn vào notification để mở ra ứng dụng của bạn hoặc thực hiện hành động trực tiệp trên thông báo.

* Thành phần của các thông báo như sau:

![](https://developer.android.com/images/ui/notifications/notification-callouts_2x.png)


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

## Push Notification

* Bạn có thể gửi thông báo mà sử dụng trên Firebase console theo hướng dẫn tại [đây](https://firebase.google.com/docs/cloud-messaging/android/first-message)
* Để push notification được cho các device, chúng ta cần chạy ứng dụng và gửi các **refreshToken** lên server hoặc Firebase Database để sử dụng cho việc gửi thông báo.
* Việc thực hiện gửi thông báo tới các device của client, có thể được thực hiện bằng việc gửi request lên link của Firebase với các thông số cụ thể, thông thường sẽ có các trường `to`, `notification`, `data`. 
* Việc đầu tiên là gửi request với body là dữ liệu ở dạng `Json`, trong đó phải có trường `to` để chỉ định người được nhận thông báo, `notification` để chỉ những thông tin cơ bản của thông báo, đây là ví dụ khi push notification bằng Android device:

```
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
    }
```
> Việc gửi thông báo từ đâu cũng không quan trọng, có thể là từ server của bạn, từ client, từ chính Firebase sử dụng Firebase Function. 

* Sau đó là gửi request lên link https://fcm.googleapis.com/fcm/send và method sử dụng là `POST`.
* Thêm header là `Authorization` chính là key của project ở trên Firebase console, có nó thì mới gửi lên thành công được. Key này được lấy ở trong Firebase consoler theo đường dẫn sau: `Project Setting\Cloud Messagin` và tìm đến phần `Legacy server key`. 
* Khi gửi thông báo thành công, api sẽ trả về cho bạn những thông tin cần thiết như việc gửi đi bao nhiêu thông báo thành công cũng như thất bại, ví dụ như sau:

```
{"multicast_id":7819227569294632575,"success":1,"failure":0,"canonical_ids":0, "results":[{"message_id":"0:1505380615594006%4b0b45c44b0b45c4"}]}
```

## Receive Notification Service
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
> Trên Android 8.0, việc hiển thị thông báo cần tạo ra 1 channel để nhóm các thông báo lại với nhau. Nếu từ Android 8.0 trở xuống thì vẫn hiển thị thông báo theo như cách thông thường. Trong đó tham số Important để chỉ tầm quan trọng của thông báo đó, lần lượt là **IMPORTANCE_HIGH**, **IMPORTANCE_DEFAULT**, **IMPORTANCE_LOW**, **IMPORTANCE_MIN**.  

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
    
## Handle Action

### When app off or disable network
* Nếu khi ứng dụng còn đang chạy, notification sẽ được nhận ở trong service, bạn có thể xử lý việc click action và di chuyển đến màn hình mong muốn.

* Nhưng nếu trong trường hợp tắt mạng hoặc tắt máy. Lúc này sẽ không có thông báo được hiện, đợi khi có mạng hiện lên thì hệ thống sẽ tự động hiển thị các thông báo chưa được nhận. Vì vậy khi click action sẽ chỉ mở ra được màn hình đầu tiên của ứng dụng, không direct sang đúng màn hình cần thiết. Vậy nên phải xử lý intent bên trong activity đầu tiên mở ra, data sẽ được gửi kèm vào trong đó ở dạng **extra** của Bundle:

```
private fun parserDataFromBundle(bundle: Bundle?): DataNotification {
        return DataNotification(
            bundle?.getString(NotificationUtil.DATA_TYPE),
            bundle?.getString(NotificationUtil.DATA_CHANNEL),
            bundle?.getString(NotificationUtil.DATA_SENDER_ID),
            bundle?.getString(NotificationUtil.DATA_SENDER_AVATAR)
        )
    }
```

> Nếu data có dữ liệu thì là do notification gửi đến, ta lấy được data bằng **intent.extra**, nếu không có thì là start activity với logic bình thường.

### Control service play music

* Tạo notification với các action như **play**, **pause**, **next**, **open**, ... để control được việc play nhạc như sau:

```
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
```

* Những action trên sẽ được gửi về hàm  **onStartCommand()** bên trong service để xử lý: 

```
val action = intent?.action ?: return
        when (action) {
            ACTION_OPEN_APP -> {
                playSong(CURRENT_POSITION)
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
```

* Sau đó gửi dữ liệu cập nhật ra ngoài giao diện bằng broadcast: 

```
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
```

# Notification Advance

## Custom Notification Layout

1. Custom notification for content area
* Việc custom layout cho vùng content của notification thì bạn phải thêm **NotificationCompat.DecoratedCustomViewStyle** vào trong notification. Việc này sẽ giúp cho bạn giữ lại được tất cả các thông tin mà hệ thống cung cấp sẵn như **icon**, **time stamp**, **sub-text**, **action**.
* Để sử dụng chức năng này, trước hết chúng ta phải tạo một layout cho phần content muốn thay thế, sau đó inflate vào đối tượng **RemoteView** để gọi ra được layout đã custom hiển thị trong thông báo:

```
<TextView
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    android:layout_weight="1"
    android:text="@string/notification_title"
    android:id="@+id/notification_title"
    style="@style/TextAppearance.Compat.Notification.Title" />
```

* Sau đó thêm trực tiếp các **RemoteView** đã custom vào **Notification.Builder**: 

```
// Get the layouts to use in the custom notification
val notificationLayout = RemoteViews(packageName, R.layout.notification_small)
val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_large)

// Apply the layouts to the notification
val customNotification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        .setCustomContentView(notificationLayout)
        .setCustomBigContentView(notificationLayoutExpanded)
        .build()
```

2. Custom full notification
* Bạn muốn thay đổi toàn bộ layout của Notification, chúng ta sử dụng layout tự tạo và inflate vào đối tượng của **RemoteView** như sau: 

```
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
```

* Sau khi tạo ra Notification Builder, cần kiểm tra xem phiên bản Android có phải là từ API 16 trở đi không mới có thể sử dụng custom notification:

```
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
    builder = builder.setCustomBigContentView(getRemoteViewChat(message, time, partner, bitmap));
}
```

## Notification Badge

* Từ Android 8.0(API 26) trở lên, sẽ xuất hiện thêm dấu chấm thông báo trên biểu tượng của icon khởi chạy ứng dụng. Người dùng có thể nhấn giữ vào icon để hiện ra các thông báo như hình dưới đây:
 
[](https://developer.android.com/images/ui/notifications/badges-open_2x.png)

* Theo mặc định dấu chấm này sẽ xuất hiện khi có thông báo, vì vậy nếu muốn ẩn dấu chấm đó đi thì sẽ sử dụng **setShowBadge(false)** ở trong **NotificationChannel**.

```
val mChannel = NotificationChannel(id, name, importance).apply {
    description = descriptionText
    setShowBadge(false)
}
```

* Theo mặc định, mỗi thông báo sẽ được tăng một số được hiển thị trên menu, nhưng bạn có thể tự thêm số này vào ứng dụng của mình bằng phương thức **setNumber()**:

```
var notification = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
        .setContentTitle("New Messages")
        .setContentText("You've received 3 new messages.")
        .setSmallIcon(R.drawable.ic_notify_status)
        .setNumber(messageCount)
        .build()
```

* Khi nhấn giữ lâu vào icon khởi động sẽ hiển thị biểu tượng lớn hoặc nhỏ liên quan đến thông báo nếu có. Theo mặc định sẽ hiển thị biểu tượng lớn, nhưng bạn có thể gọi **Notification.Builder.setBadgeIconType()** và truyền vào **BADGE_ICON_SMALL** để thay đổi.

## Notification group

* Bắt đầu từ Android 7.0 (API 24), bạn có thể nhóm các notification vào với nhau thành 1 group để nhóm gọn và khi cần thì mở ra tất cả các notification cùng group.
* Để tạo một Notification group ta sử dụng phương thức **setGroup()**:

```
val GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL"

val newMessageNotification = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
        .setSmallIcon(R.drawable.new_mail)
        .setContentTitle(emailObject.getSenderName())
        .setContentText(emailObject.getSubject())
        .setLargeIcon(emailObject.getSenderAvatar())
        .setGroup(GROUP_KEY_WORK_EMAIL)
        .build()
```

* Chúng ta cũng có thể nhóm lại các thông báo cùng với nhau nếu chúng là cùng một kiểu và chỉ cần thêm nội dung vào. Ví dụ dưới đây sẽ cho thấy việc nhóm thông báo của các email theo từng group khác nhau:

```
//use constant ID for notification used as group summary
val SUMMARY_ID = 0
val GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL"

val newMessageNotification1 = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notify_email_status)
        .setContentTitle(emailObject1.getSummary())
        .setContentText("You will not believe...")
        .setGroup(GROUP_KEY_WORK_EMAIL)
        .build()

val newMessageNotification2 = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notify_email_status)
        .setContentTitle(emailObject2.getSummary())
        .setContentText("Please join us to celebrate the...")
        .setGroup(GROUP_KEY_WORK_EMAIL)
        .build()

val summaryNotification = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
        .setContentTitle(emailObject.getSummary())
        //set content text to support devices running API level < 24
        .setContentText("Two new messages")
        .setSmallIcon(R.drawable.ic_notify_summary_status)
        //build summary info into InboxStyle template
        .setStyle(NotificationCompat.InboxStyle()
                .addLine("Alex Faarborg Check this out")
                .addLine("Jeff Chang Launch Party")
                .setBigContentTitle("2 new messages")
                .setSummaryText("janedoe@example.com"))
        //specify which group this notification belongs to
        .setGroup(GROUP_KEY_WORK_EMAIL)
        //set this notification as the summary for the group
        .setGroupSummary(true)
        .build()

NotificationManagerCompat.from(this).apply {
    notify(emailNotificationId1, newMessageNotification1)
    notify(emailNotificationId2, newMessageNotification2)
    notify(SUMMARY_ID, summaryNotification)
}
```

## Notification channel

* Như trên cũng đã nhắc đến Android O(API 26) trở lên có thể tạo các channel cho thông báo, đây là ví dụ cho việc tạo 1 channel:

```
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    // Create the NotificationChannel
    val name = getString(R.string.channel_name)
    val descriptionText = getString(R.string.channel_description)
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
    mChannel.description = descriptionText
    // Register the channel with the system; you can't change the importance
    // or other notification behaviors after this
    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(mChannel)
}
```

* Đọc cài đặt notificaiton channel của từng channel đang có bằng cách lấy ra NotificationChannel thông qua phương thức **getNotificationChannel()** và lấy ra các giá trị cài đặt như **getVibrationPattern()**, **getSound()**, **getImportance()**.

* Để mở notification channel setting chúng ta cần sử dụng đối tượng Intent như sau:

```
val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    putExtra(Settings.EXTRA_CHANNEL_ID, myNotificationChannel.getId())
}
startActivity(intent)
```

* Để xóa một notification channel ta cần xác định được id của channel rồi làm như sau:

```
// The id of the channel.
val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
val id: String = "my_channel_01"
notificationManager.deleteNotificationChannel(id)
```

** Bạn cũng có thể tạo notification channel bên trong 1 group nào đó như sau: 

```
// The id of the group.
val groupId = "my_group_01"
// The user-visible name of the group.
val groupName = getString(R.string.group_name)
val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
notificationManager.createNotificationChannelGroup(NotificationChannelGroup(groupId, groupName))
```