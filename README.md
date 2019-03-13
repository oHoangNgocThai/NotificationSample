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
    
