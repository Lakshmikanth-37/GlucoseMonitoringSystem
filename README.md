# Glucose Monitoring System

An Android reminder app with four daily meal reminders for either India or Minneapolis. The selected profile controls the schedule and time zone.

## Schedules

**India (`Asia/Kolkata`)**
- Breakfast — 08:00
- Lunch — 13:00
- Evening snack — 16:30
- Dinner — 20:00

**Minneapolis (`America/Chicago`)**
- Breakfast — 08:00
- Lunch — 12:00
- Evening snack — 15:30
- Dinner — 18:30

Minneapolis reminders follow Central Time daylight-saving changes automatically. Choose the correct profile on each phone. The app schedules alarms locally; it does not send data between the phones.

## Build the APK with GitHub (no Android Studio)

1. Create a new GitHub repository, for example `GlucoseMonitoringSystem`.
2. Upload the contents of this folder to the repository root (the `.github` folder must be included).
3. Open **Actions** and enable workflows if GitHub asks.
4. The workflow **Build Android APK** runs on push. You can also run it from Actions using **Run workflow**.
5. Open the completed workflow run, scroll to **Artifacts**, and download `glucose-monitoring-system-debug-apk`.
6. Extract the ZIP and install `app-debug.apk` on each phone.

The APK is a debug-signed APK for personal testing, not a Play Store release.

## On each phone

1. Install the APK.
2. Open **Glucose Monitoring System**.
3. Select **India** on your phone and **Minneapolis** on her phone.
4. Tap **Save profile & schedule reminders**.
5. Allow notifications.
6. If prompted, allow **Alarms & reminders** / exact alarms in Android settings.
7. For reliable delivery, allow background activity and set battery usage to unrestricted if the phone maker's battery controls delay alarms.

On Android 13+, notification permission is required. Exact alarm settings differ by Android version and phone manufacturer.

## Notification behavior

- The Minneapolis notification opens `https://app.netlify.com/projects/warm-starlight-025bb1/` when tapped.
- India notifications are reminders and open the app instead.
- Alerts use the system notification sound and vibration. Android can mute or suppress these through Do Not Disturb, notification settings, or battery policies; this app does not bypass those controls or guarantee a full-screen alarm.
- This app is a reminder tool, not a glucose sensor or medical device.

## Important

The app uses the profile's named time zone rather than relying on the phone's current time zone. If the selected profile is changed, tap **Save profile & schedule reminders** again. Alarms are restored after reboot and rescheduled when the system time or time zone changes.
