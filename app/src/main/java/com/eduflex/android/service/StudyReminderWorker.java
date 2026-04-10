package com.eduflex.android.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.eduflex.android.LoginActivity;
import com.eduflex.android.R;

/**
 * WorkManager worker that fires a local notification
 * reminding the user to study every day.
 * Scheduled as a PeriodicWorkRequest (once per day).
 */
public class StudyReminderWorker extends Worker {

    private static final String CHANNEL_ID = "study_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;

    // Randomized motivational messages
    private static final String[] MESSAGES = {
            "Time to learn something new today!",
            "Keep your streak alive — study now!",
            "Just 10 minutes of study can make a difference!",
            "Top learners study every day. Join them!",
            "Your daily learning goal is waiting!"
    };

    public StudyReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        createNotificationChannel();
        showReminderNotification();
        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Study Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Daily reminders to keep learning");

            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showReminderNotification() {
        Context ctx = getApplicationContext();

        // Tap opens the app
        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Random motivational message
        String message = MESSAGES[(int) (System.currentTimeMillis() % MESSAGES.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("EduFlex - Nhắc học tập")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
