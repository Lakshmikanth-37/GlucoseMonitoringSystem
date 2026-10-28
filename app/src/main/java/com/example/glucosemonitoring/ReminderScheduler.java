package com.example.glucosemonitoring;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

public final class ReminderScheduler {
    public static final String PREFS = "glucose_monitoring_prefs";
    public static final String KEY_PROFILE = "profile";
    public static final String PROFILE_INDIA = "india";
    public static final String PROFILE_MINNEAPOLIS = "minneapolis";
    public static final String EXTRA_MEAL = "meal";
    public static final String EXTRA_PROFILE = "profile";
    public static final String CHANNEL_ID = "meal_reminders";
    public static final String WEBSITE = "https://app.netlify.com/projects/warm-starlight-025bb1/";

    public static final String[] MEALS = {"Breakfast", "Lunch", "Evening snack", "Dinner"};
    private static final int[] INDIA_TIMES = {800, 1300, 1630, 2000};
    private static final int[] MINNEAPOLIS_TIMES = {800, 1200, 1530, 1830};

    private ReminderScheduler() {}

    public static String zoneForProfile(String profile) {
        return PROFILE_MINNEAPOLIS.equals(profile) ? "America/Chicago" : "Asia/Kolkata";
    }

    public static int[] timesForProfile(String profile) {
        return PROFILE_MINNEAPOLIS.equals(profile) ? MINNEAPOLIS_TIMES : INDIA_TIMES;
    }

    public static void scheduleAll(Context context) {
        createNotificationChannel(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String profile = prefs.getString(KEY_PROFILE, PROFILE_INDIA);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        int[] times = timesForProfile(profile);
        for (int i = 0; i < MEALS.length; i++) {
            int requestCode = 4100 + i;
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra(EXTRA_MEAL, MEALS[i]);
            intent.putExtra(EXTRA_PROFILE, profile);
            PendingIntent pending = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pending);
            int hhmm = times[i];
            int hour = hhmm / 100;
            int minute = hhmm % 100;
            ZoneId zone = ZoneId.of(zoneForProfile(profile));
            ZonedDateTime now = ZonedDateTime.now(zone);
            ZonedDateTime next = now.toLocalDate().atTime(LocalTime.of(hour, minute)).atZone(zone);
            if (!next.isAfter(now)) {
                next = next.plusDays(1);
            }

            long triggerAtMillis = next.toInstant().toEpochMilli();
            setAlarmCompat(alarmManager, triggerAtMillis, pending);
        }
    }

    public static void scheduleNextForMeal(Context context, String meal) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String profile = prefs.getString(KEY_PROFILE, PROFILE_INDIA);
        for (int i = 0; i < MEALS.length; i++) {
            if (MEALS[i].equals(meal)) {
                int hhmm = timesForProfile(profile)[i];
                ZoneId zone = ZoneId.of(zoneForProfile(profile));
                ZonedDateTime now = ZonedDateTime.now(zone);
                ZonedDateTime next = now.toLocalDate().atTime(hhmm / 100, hhmm % 100).atZone(zone);
                if (!next.isAfter(now)) next = next.plusDays(1);

                Intent intent = new Intent(context, ReminderReceiver.class);
                intent.putExtra(EXTRA_MEAL, meal);
                intent.putExtra(EXTRA_PROFILE, profile);
                PendingIntent pending = PendingIntent.getBroadcast(
                    context, 4100 + i, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    setAlarmCompat(alarmManager, next.toInstant().toEpochMilli(), pending);
                }
                return;
            }
        }
    }


    private static void setAlarmCompat(AlarmManager alarmManager, long triggerAtMillis, PendingIntent pending) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending);
            } else {
                // Graceful fallback until the user grants the special Alarms & reminders permission.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending);
        }
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Meal reminders", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Daily glucose monitoring meal reminders");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 250, 500});
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showTestNotification(Context context) {
        android.content.Intent intent = new android.content.Intent(context, ReminderReceiver.class);
        intent.putExtra(EXTRA_MEAL, "Test reminder");
        intent.putExtra(EXTRA_PROFILE, context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PROFILE, PROFILE_INDIA));
        intent.putExtra("test", true);
        context.sendBroadcast(intent);
    }
}
