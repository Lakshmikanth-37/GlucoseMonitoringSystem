package com.example.glucosemonitoring;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String meal = intent.getStringExtra(ReminderScheduler.EXTRA_MEAL);
        if (meal == null) meal = "Meal";
        String profile = intent.getStringExtra(ReminderScheduler.EXTRA_PROFILE);
        if (profile == null) {
            profile = context.getSharedPreferences(ReminderScheduler.PREFS, Context.MODE_PRIVATE)
                .getString(ReminderScheduler.KEY_PROFILE, ReminderScheduler.PROFILE_INDIA);
        }

        ReminderScheduler.createNotificationChannel(context);
        Intent tapIntent;
        if (ReminderScheduler.PROFILE_MINNEAPOLIS.equals(profile)) {
            tapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ReminderScheduler.WEBSITE));
            tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            tapIntent = new Intent(context, MainActivity.class);
            tapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent contentPending = PendingIntent.getActivity(
            context, ReminderScheduler.PROFILE_MINNEAPOLIS.equals(profile) ? 7102 : 7101,
            tapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = "Glucose Monitoring System";
        String message = "It's time for your " + meal.toLowerCase() + " reminder.";
        if (ReminderScheduler.PROFILE_MINNEAPOLIS.equals(profile)) {
            message += " Tap to open the monitoring website.";
        } else {
            message += " Please check in about glucose monitoring.";
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(context, ReminderScheduler.CHANNEL_ID)
            : new Notification.Builder(context);

        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new Notification.BigTextStyle().bigText(message))
            .setContentIntent(contentPending)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(Notification.PRIORITY_MAX)
            .setVibrate(new long[]{0, 500, 250, 500});

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notificationId = (int) (System.currentTimeMillis() & 0x7fffffff);
            manager.notify(notificationId, builder.build());
        }

        boolean isTest = intent.getBooleanExtra("test", false);
        if (!isTest) {
            ReminderScheduler.scheduleNextForMeal(context, meal);
        }
    }
}
