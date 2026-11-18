# Dot Clock for Nothing Phone (4a)

A small Android live-wallpaper companion with a bold, full-screen landscape clock. The app screen is black with large white type and a red dot accent. It fills the display in immersive landscape mode while the app is open.

## Lock-screen limitation

The full-screen landscape clock appears while Dot Clock is open. Android apps cannot replace the secure lock screen or force Nothing OS's lock screen to landscape. The lock screen continues to use Nothing OS's built-in clock, style, and widgets. Dot Clock's live wallpaper provides a subtle dot pattern and red accent behind that system UI; it deliberately does not draw another time over the system clock.

## Install and use

1. Open this project in Android Studio (Android Gradle Plugin 8.10+, Kotlin 2.1+, Android SDK 36).
2. Build and install the `app` configuration on the Phone (4a) or another Android 7.0+ phone with live wallpapers enabled.
3. Open **Dot Clock** to see the bold full-screen landscape clock.
4. Tap **Set lock screen wallpaper** and choose **Lock screen** in Android's confirmation if that option is offered.
5. Adjust the actual lock-screen clock and widgets in Nothing OS Settings → Lock screen.

The app follows the phone's 12/24-hour clock preference and refreshes while open. No location, network, notification, or overlay permissions are requested. The wallpaper redraws on visibility and once per minute while active.

## Project contents

- `app/src/main/java/com/dotclock/lockclock/` — native Android activity and live-wallpaper service.
- `preview/` — visual previews, not captures from a running Phone (4a).

The app source is provided; this environment has no Android SDK or Gradle/JDK installation, so an APK has not been built or device-tested here.

## Suggested next features

- Optional white, amber, or red digit colors and a few bold font choices.
- A portrait mode that keeps the clock full-screen when the phone rotates.
- Optional date, next alarm, or weather cards using Android widgets where Nothing OS allows them.
- A dim night mode and adjustable dot density.

These ideas are not implemented in this starter app.
