package com.example.glucosemonitoring;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private RadioButton indiaButton;
    private RadioButton minneapolisButton;
    private TextView status;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(ReminderScheduler.PREFS, MODE_PRIVATE);
        buildUi();
        requestNotificationPermissionIfNeeded();
    }

    private void buildUi() {
        int pad = dp(22);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFFF7F8FA);

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText("Glucose Monitoring System");
        title.setTextSize(25);
        title.setTextColor(0xFF152238);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Daily meal reminders");
        subtitle.setTextSize(16);
        subtitle.setTextColor(0xFF586579);
        subtitle.setPadding(0, dp(6), 0, dp(18));
        content.addView(subtitle);

        TextView choose = new TextView(this);
        choose.setText("Choose this phone's location profile");
        choose.setTextSize(17);
        choose.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        choose.setTextColor(0xFF152238);
        content.addView(choose);

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        indiaButton = new RadioButton(this);
        indiaButton.setText("India 🇮🇳 — IST");
        indiaButton.setTextSize(16);
        minneapolisButton = new RadioButton(this);
        minneapolisButton.setText("Minneapolis 🇺🇸 — Central Time");
        minneapolisButton.setTextSize(16);
        group.addView(indiaButton);
        group.addView(minneapolisButton);
        content.addView(group);

        String profile = prefs.getString(ReminderScheduler.KEY_PROFILE, ReminderScheduler.PROFILE_INDIA);
        (ReminderScheduler.PROFILE_MINNEAPOLIS.equals(profile) ? minneapolisButton : indiaButton).setChecked(true);

        TextView schedule = new TextView(this);
        schedule.setText(
            "\nINDIA (Asia/Kolkata)\n" +
            "08:00  Breakfast\n13:00  Lunch\n16:30  Evening snack\n20:00  Dinner\n\n" +
            "MINNEAPOLIS (America/Chicago)\n" +
            "08:00  Breakfast\n12:00  Lunch\n15:30  Evening snack\n18:30  Dinner"
        );
        schedule.setTextSize(15);
        schedule.setTextColor(0xFF344256);
        schedule.setPadding(0, dp(12), 0, dp(18));
        content.addView(schedule);

        Button save = new Button(this);
        save.setText("Save profile & schedule reminders");
        content.addView(save);
        save.setOnClickListener(v -> {
            String selected = minneapolisButton.isChecked()
                ? ReminderScheduler.PROFILE_MINNEAPOLIS
                : ReminderScheduler.PROFILE_INDIA;
            prefs.edit().putString(ReminderScheduler.KEY_PROFILE, selected).apply();
            ReminderScheduler.scheduleAll(this);
            requestExactAlarmPermissionIfNeeded();
            status.setText("Profile saved. Four daily reminders are scheduled for " +
                (selected.equals(ReminderScheduler.PROFILE_INDIA) ? "India." : "Minneapolis."));
            Toast.makeText(this, "Reminder schedule saved", Toast.LENGTH_LONG).show();
        });

        Button test = new Button(this);
        test.setText("Send a test notification");
        content.addView(test);
        test.setOnClickListener(v -> ReminderScheduler.showTestNotification(this));

        status = new TextView(this);
        status.setText("Choose the correct profile, then save. Allow notifications and exact alarms when prompted.");
        status.setTextColor(0xFF176B4D);
        status.setTextSize(14);
        status.setPadding(0, dp(12), 0, dp(8));
        content.addView(status);

        TextView note = new TextView(this);
        note.setText("Minneapolis notifications open the website when tapped. India notifications open this app. Alerts use your phone's notification sound and vibration settings.");
        note.setTextSize(13);
        note.setTextColor(0xFF586579);
        note.setPadding(0, dp(8), 0, dp(10));
        content.addView(note);

        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                    .setTitle("Allow exact alarms")
                    .setMessage("For reminders to arrive close to the selected times, allow Alarms & reminders for this app.")
                    .setNegativeButton("Later", (d, w) -> {})
                    .setPositiveButton("Open settings", (d, w) -> {
                        Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + getPackageName()));
                        startActivity(i);
                    }).show();
            }
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs != null) {
            ReminderScheduler.scheduleAll(this);
        }
    }
}
