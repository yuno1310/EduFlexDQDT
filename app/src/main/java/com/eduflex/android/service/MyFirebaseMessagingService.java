// app/src/main/java/com/eduflex/android/service/MyFirebaseMessagingService.java
package com.eduflex.android.service;

import android.app.NotificationManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.eduflex.android.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.app.NotificationChannel;
import android.os.Build;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d("FCM_TOKEN", "Message nhận được khi app đang mở");

        // Lấy title và body từ message
        String title = "Thông báo";
        String body = "";

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }

        showNotification(title, body);
    }

    @Override
    public void onNewToken(String token) {
        Log.d("FCM_TOKEN", "New token: " + token);
        // Gửi token lên backend để server biết gửi notification cho ai
        // sendTokenToServer(token);
    }

    private void showNotification(String title, String body) {
        // Tạo Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "eduflex_channel",
                    "EduFlex Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "eduflex_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Hiển thị
        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), builder.build());
    }
}
