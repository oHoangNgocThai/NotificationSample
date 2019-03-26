# NotificationSample

# [Overview](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#introduction)

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
 
# [Config Notification](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#config-notification)

> Hiện nay service được sử dụng nhiều nhất để gửi thông báo đi các thiết bị là của FireBase cung cấp - **FireBase Cloud Messaging**.

## [Config service](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#config-service)

* Kết nối project với FireBase, xem tại [đây](https://firebase.google.com/docs/android/setup)
* Tạo 1 service extend từ **FirebaseMessagingService()** để có thể lắng nghe được những sự kiện từ FireBase gửi về và cũng sinh ra `refreshToken` trong hàm `onNewToken()` để FireBase quản lý token gửi về:
> Chú ý: Nếu ở trong Java thì bạn phải tạo 2 service khác nhau. Việc lấy token sẽ được lấy ở một service khác như `FirebaseInstanceIdService`. Nếu sử dụng trong Java thì chỉ cần tạo 1 service extend FirebaseInstanceIdService rồi override phương thức `onTokenRefresh()`.
* Nếu muốn lấy `refreshToken` ở ngoài Activity ta sử dụng **FirebaseInstanceId.getInstance().getInstanceId()**. 
* Việc sử dụng `newToken` được sinh ra có thể xử lý được vài trường hợp như:
    
    * Ứng dụng xóa Instance ID
    * Ứng dụng được restored ở một thiết bị mới
    * Người dùng gỡ ứng dụng và cài lại
    * Người dùng clear data của ứng dụng. 

* Đừng quên việc khai báo service bên trong file cấu hình AndroidManifest.xml với một `intent-filter` là **<action android:name="com.google.firebase.MESSAGING_EVENT" />**: 

# [Handle Notification](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#handle-notification)

## [Push Notification](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#push-notification)

* Bạn có thể gửi thông báo mà sử dụng trên Firebase console theo hướng dẫn tại [đây](https://firebase.google.com/docs/cloud-messaging/android/first-message)
* Để push notification được cho các device, chúng ta cần chạy ứng dụng và gửi các **refreshToken** lên server hoặc Firebase Database để sử dụng cho việc gửi thông báo.
* Việc thực hiện gửi thông báo tới các device của client, có thể được thực hiện bằng việc gửi request lên link của Firebase với các thông số cụ thể, thông thường sẽ có các trường `to`, `notification`, `data`. 
* Việc đầu tiên là gửi request với body là dữ liệu ở dạng `Json`, trong đó phải có trường `to` để chỉ định người được nhận thông báo, `notification` để chỉ những thông tin cơ bản của thông báo, đây là ví dụ khi push notification bằng Android device:

> Việc gửi thông báo từ đâu cũng không quan trọng, có thể là từ server của bạn, từ client, từ chính Firebase sử dụng Firebase Function. 

* Sau đó là gửi request lên link https://fcm.googleapis.com/fcm/send và method sử dụng là `POST`.
* Thêm header là `Authorization` chính là key của project ở trên Firebase console, có nó thì mới gửi lên thành công được. Key này được lấy ở trong Firebase consoler theo đường dẫn sau: `Project Setting\Cloud Messagin` và tìm đến phần `Legacy server key`. 
* Khi gửi thông báo thành công, api sẽ trả về cho bạn những thông tin cần thiết như việc gửi đi bao nhiêu thông báo thành công cũng như thất bại.

## [Receive Notification Service](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#receive-notification-service)

> Notification được gửi về ứng dụng sẽ được service nhận qua `RemoteMessage` lưu trữ các thông tin cần thiết.

* **RemoteMessage**: Những thông tin của notification được lưu trữ bên trong object này, có thể chú ý đến 2 phần chính là `notification` và `data`.
    
    * `Notification`: Chứa các thông tin cơ bản cần có của 1 thông báo như **title**, **body**, **icon**, **tag**, **click_action**, **android_channel_id**. Thông thường sẽ không cần dùng đến hết cả những trường trên, chỉ là nếu ứng dụng không hoạt động hoặc không có kết nối thì hệ thống sẽ dựa vào những trường này để hiển thị thông báo mặc định của hệ thống.
    * `data`: Phần này quan trọng cho việc gửi về những thông tin cần thiết khác ở dạng **Map<String,String>**. Khi cần custom thông báo hoặc gửi những dữ liệu dành cho việc handle click thì sẽ rất cần thiết.
    
* Khi ứng dụng đang chạy thì những notification được gửi về sẽ được trả về trong hàm **onMessageReceived** trong MessagingService. Nếu ứng dụng đang tắt hoặc không có kết nối, notification sau đó sẽ được hệ thống Android gửi về như thông báo bình thường.
* Việc parser dữ liệu của remoteMessage được thực hiện bên trong **MessageService** được lấy từ **remoteMessage.notification** và **remoteMessage.data**.

## [Create Notification](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#create-notification)

> Sau khi đã nhận dữ liệu notification từ service về, việc tiếp theo cần làm là hiển thị chúng lên giao diện của người dùng.

### [Simple Notification](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#simple-notification)

* Thêm dependency `com.android.support:support-compat:28.0.0` vào trong file build.gradle module-level để sử dụng `NotificationCompat`: 
* Sau khi nhận được dữ liệu notification bên trong object **RemoteMessage**, tạo một Notification sử dụng dữ liệu đó để cấu hình cho Notification Builder:
* Hiển thị thông báo lên màn hình sử dụng **Notification Manager**, với tham số là **builder** và **notificationId**. Việc quản lý ID của thông báo rất quan trọng vì nó cũng dành cho việc chỉnh sửa hoặc xóa notification.

> Trên Android 8.0, việc hiển thị thông báo cần tạo ra 1 channel để nhóm các thông báo lại với nhau. Nếu từ Android 8.0 trở xuống thì vẫn hiển thị thông báo theo như cách thông thường. Trong đó tham số Important để chỉ tầm quan trọng của thông báo đó, lần lượt là **IMPORTANCE_HIGH**, **IMPORTANCE_DEFAULT**, **IMPORTANCE_LOW**, **IMPORTANCE_MIN**.  

### [Actions Notification](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#actions-notification)

* Thêm **Action** vào bên trong Notification, bạn có thể tạo ra tối đa 3 action tương ứng với 3 **PendingIntent** khác nhau. Những Intent này có thể mở ra Activity, start Service hoặc là gửi BroadCast.

```
val builder: NotificationCompat = NotificationCompat.Builder(mContext, channel)
    ...
    .addAction(...)
    .build()

```

* Action sẽ được nhận ở Activity qua **Intent**, Service qua **onStartCommand** và BroadCast là ở trong **onReceive()**. Bạn có thể gửi thêm dữ liệu vào intent qua việc sử dụng **putExtra()**.

### [Reply Notification](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#reply-notification)

* Thêm ô nhập reply ngay trên thanh notification dựa vào thể hiện của **RemoteInput.Builder** là `ReplyInput`:

```
val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
    setLabel(replyLabel)
    build()
}
```

* Sau đó xử lý việc nhận action của **RemoteInput** ở Activity, Service hay Broadcast đều dựa vào dữ liệu của Intent gửi đến.

> Sau khi nhận được input text thì thực hiện việc xóa notification đi. Đa số trong trường hợp này là gửi về service để tiến hành gửi đi mà không cần dùng giao diện. 

### [Add a progress bar](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#add-a-progress-bar)

* Thêm progress bar trong trường hợp muốn download một file nào đó mà hiển thị thông báo, sử dụng việc cập nhật thông báo với id đã được lưu trữ.
* Việc cập nhật sử dụng phương thức **setProgress(max, current, false)** để set gía trị mới của progress, sau đó sử dụng **notify()** để cập nhật lại notification.
* Việc cập nhật thông báo này cũng nên được thực hiện bởi IntentService, sau khi đó sẽ tự kết thúc.

### [Lock screen visibility and Update](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#lock-screen-visibility-and-update)

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
    
## [Handle Action](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#handle-action)

### [When app off or disable network](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#when-app-off-or-disable-network)

* Nếu khi ứng dụng còn đang chạy, notification sẽ được nhận ở trong service, bạn có thể xử lý việc click action và di chuyển đến màn hình mong muốn.

* Nhưng nếu trong trường hợp tắt mạng hoặc tắt máy. Lúc này sẽ không có thông báo được hiện, đợi khi có mạng hiện lên thì hệ thống sẽ tự động hiển thị các thông báo chưa được nhận. Vì vậy khi click action sẽ chỉ mở ra được màn hình đầu tiên của ứng dụng, không direct sang đúng màn hình cần thiết. Vậy nên phải xử lý intent bên trong activity đầu tiên mở ra, data sẽ được gửi kèm vào trong đó ở dạng **extra** của Bundle:

> Nếu data có dữ liệu thì là do notification gửi đến, ta lấy được data bằng **intent.extra**, nếu không có thì là start activity với logic bình thường.

### [Control service play music](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#control-service-play-music)

* Tạo notification với các action như **play**, **pause**, **next**, **open**, ... để control được việc play nhạc.

* Những action trên sẽ được gửi về hàm  **onStartCommand()** bên trong service để xử lý dựa vào action của từng loại. 

* Sau đó gửi dữ liệu cập nhật ra ngoài giao diện bằng **Broadcast** hoặc là **BoundService**: 

# [Notification Advance](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#notification-advance)

## [Custom Notification Layout](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#custom-notification-layout)

1. Custom notification for content area

* Việc custom layout cho vùng content của notification thì bạn phải thêm **NotificationCompat.DecoratedCustomViewStyle** vào trong notification. Việc này sẽ giúp cho bạn giữ lại được tất cả các thông tin mà hệ thống cung cấp sẵn như **icon**, **time stamp**, **sub-text**, **action**.
* Để sử dụng chức năng này, trước hết chúng ta phải tạo một layout cho phần content muốn thay thế, sau đó inflate vào đối tượng **RemoteView** để gọi ra được layout đã custom hiển thị trong thông báo:
* Sau đó thêm trực tiếp các **RemoteView** đã custom vào **Notification.Builder**: 

```
val notificationLayout = RemoteViews(packageName, R.layout.notification_small)
val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_large)

val customNotification = NotificationCompat.Builder(context, CHANNEL_ID)
        ...
        .setCustomContentView(notificationLayout)
        .setCustomBigContentView(notificationLayoutExpanded)
        .build()
```

2. Custom full notification

* Bạn muốn thay đổi toàn bộ layout của Notification, chúng ta sử dụng layout tự tạo và inflate vào đối tượng của **RemoteView** như sau: 

```
private fun getRemoteViewChat( message: String, remoteMessage: RemoteMessage, bitmap: Bitmap): RemoteViews {
        val notificationView = RemoteViews(mContext.packageName, R.layout.layout_notification_chat)
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

## [Notification Badge](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#notification-badge)

* Từ Android 8.0(API 26) trở lên, sẽ xuất hiện thêm dấu chấm thông báo trên biểu tượng của icon khởi chạy ứng dụng. Người dùng có thể nhấn giữ vào icon để hiện ra các thông báo như hình dưới đây:
 
[](https://developer.android.com/images/ui/notifications/badges-open_2x.png)

* Theo mặc định dấu chấm này sẽ xuất hiện khi có thông báo, vì vậy nếu muốn ẩn dấu chấm đó đi thì sẽ sử dụng **setShowBadge(false)** ở trong **NotificationChannel**.
* Theo mặc định, mỗi thông báo sẽ được tăng một số được hiển thị trên menu, nhưng bạn có thể tự thêm số này vào ứng dụng của mình bằng phương thức **setNumber()**:
* Khi nhấn giữ lâu vào icon khởi động sẽ hiển thị biểu tượng lớn hoặc nhỏ liên quan đến thông báo nếu có. Theo mặc định sẽ hiển thị biểu tượng lớn, nhưng bạn có thể gọi **Notification.Builder.setBadgeIconType()** và truyền vào **BADGE_ICON_SMALL** để thay đổi.

## [Notification group](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#notification-group)

* Bắt đầu từ Android 7.0 (API 24), bạn có thể nhóm các notification vào với nhau thành 1 group để nhóm gọn và khi cần thì mở ra tất cả các notification cùng group.
* Để tạo một Notification group ta sử dụng phương thức **setGroup()**:
* Chúng ta cũng có thể nhóm lại các thông báo cùng với nhau nếu chúng là cùng một kiểu và chỉ cần tạo thêm notification mới chứa các notification trước đó, chúng sẽ được set chung vào 1 group:

```

val newMessageNotification1 = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
        ...
        .setGroup(GROUP_KEY_WORK_EMAIL)
        .build()

val newMessageNotification2 = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
        ...
        .setGroup(GROUP_KEY_WORK_EMAIL)
        .build()

val summaryNotification = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
        ....
        .setStyle(NotificationCompat.InboxStyle()
                .addLine("Alex Faarborg Check this out")
                .addLine("Jeff Chang Launch Party")
                .setBigContentTitle("2 new messages")
                .setSummaryText("janedoe@example.com"))
        .setGroup(GROUP_KEY_WORK_EMAIL)
        .setGroupSummary(true)
        .build()

NotificationManagerCompat.from(this).apply {
    notify(emailNotificationId1, newMessageNotification1)
    notify(emailNotificationId2, newMessageNotification2)
    notify(SUMMARY_ID, summaryNotification)
}
```

## [Notification channel](https://github.com/oHoangNgocThai/NotificationSample/blob/master/app/Documents.md#notification-channel)

* Như trên cũng đã nhắc đến Android O(API 26) trở lên có thể tạo các channel cho thông báo, sẽ có thêm các thông số của channel như **name**, **description**, **importance**:

```
val mChannel = NotificationChannel(CHANNEL_ID, name, importance).apply {
    mChannel.description = descriptionText
}
...
notificationManager.createNotificationChannel(mChannel)
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

