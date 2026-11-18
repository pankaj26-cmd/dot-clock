package com.dotclock.lockclock

import android.app.Activity
import android.app.KeyguardManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast

class MainActivity : Activity() {
    private var clockFace: ClockFaceView? = null
    private var buttonLayout: LinearLayout? = null
    private var userPresentReceiver: BroadcastReceiver? = null

    private val autoHideHandler = Handler(Looper.getMainLooper())
    private val autoHideRunnable = Runnable {
        hideClockAutomatically()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BootReceiver.startDotClockService(this)

        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } catch (_: Exception) {
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(false)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                @Suppress("DEPRECATION")
                window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } catch (_: Exception) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } catch (_: Exception) {
            }
        }

        try {
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
        } catch (_: Exception) {
        }

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        clockFace = ClockFaceView(this).apply {
            isClockContentVisible = false
            onSingleTapListener = {
                // Single tap: Show Dot Clock for 30 seconds then auto-disappear
                showClockFor30Seconds()
            }
            onDoubleTapListener = {
                // Double tap: Open normal phone lock screen
                openSystemLockScreen()
            }
        }
        root.addView(clockFace, FrameLayout.LayoutParams(-1, -1))

        buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            visibility = View.GONE
        }

        val screenSaverButton = Button(this).apply {
            text = "SET AS SCREEN SAVER"
            isAllCaps = false
            textSize = 12f
            letterSpacing = .06f
            setTextColor(0xFFFFFFFF.toInt())
            backgroundTintList = ColorStateList.valueOf(0xFF262626.toInt())
            setOnClickListener { openScreenSaverSettings() }
        }
        buttonLayout?.addView(screenSaverButton, LinearLayout.LayoutParams(-2, dp(44)).apply {
            rightMargin = dp(12)
        })

        val wallpaperButton = Button(this).apply {
            text = "SET AS WALLPAPER"
            isAllCaps = false
            textSize = 12f
            letterSpacing = .06f
            setTextColor(0xFFD6D6D6.toInt())
            backgroundTintList = ColorStateList.valueOf(0xFF191919.toInt())
            setOnClickListener { openWallpaperPreview() }
        }
        buttonLayout?.addView(wallpaperButton, LinearLayout.LayoutParams(-2, dp(44)))

        root.addView(
            buttonLayout,
            FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
                bottomMargin = dp(20)
            }
        )

        setContentView(root)
        hideSystemUI()
        registerUnlockReceiver()

        // Hidden by default when locked
        hideClockAutomatically()
    }

    private fun registerUnlockReceiver() {
        if (userPresentReceiver == null) {
            userPresentReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_USER_PRESENT) {
                        closeAndRemoveFromRecents()
                    }
                }
            }
            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
            registerReceiver(userPresentReceiver, filter)
        }
    }

    private fun closeAndRemoveFromRecents() {
        try {
            autoHideHandler.removeCallbacks(autoHideRunnable)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            } else {
                finish()
            }
        } catch (_: Exception) {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(false)
        }
        hideSystemUI()
        hideClockAutomatically()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let { controller ->
                    controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun showClockFor30Seconds() {
        autoHideHandler.removeCallbacks(autoHideRunnable)

        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val params = window.attributes
            params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            window.attributes = params
        } catch (_: Exception) {
        }

        clockFace?.isClockContentVisible = true
        clockFace?.invalidate()

        autoHideHandler.postDelayed(autoHideRunnable, 30_000L)
    }

    private fun hideClockAutomatically() {
        autoHideHandler.removeCallbacks(autoHideRunnable)
        clockFace?.isClockContentVisible = false
        clockFace?.invalidate()

        try {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val params = window.attributes
            params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
            window.attributes = params
        } catch (_: Exception) {
        }
    }

    private fun openSystemLockScreen() {
        try {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager?.requestDismissKeyguard(this, null)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            }
        } catch (_: Exception) {
        }
        closeAndRemoveFromRecents()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(false)
        }
        hideSystemUI()
        if (clockFace?.isClockContentVisible != true) {
            hideClockAutomatically()
        }
        clockFace?.startTicking()
    }

    override fun onPause() {
        autoHideHandler.removeCallbacks(autoHideRunnable)
        clockFace?.stopTicking()
        super.onPause()
    }

    override fun onDestroy() {
        userPresentReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {
            }
        }
        autoHideHandler.removeCallbacks(autoHideRunnable)
        clockFace?.stopTicking()
        super.onDestroy()
    }

    private fun openScreenSaverSettings() {
        Toast.makeText(
            this,
            "Select 'Dot Clock' in Screen Saver settings so it appears when the screen turns off.",
            Toast.LENGTH_LONG
        ).show()
        try {
            startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
        } catch (_: Exception) {
            try {
                startActivity(Intent("android.settings.DREAM_SETTINGS"))
            } catch (_: Exception) {
                Toast.makeText(
                    this,
                    "Go to Phone Settings -> Display -> Screen Saver and choose Dot Clock.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openWallpaperPreview() {
        try {
            val component = ComponentName(this, DotClockWallpaperService::class.java)
            startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            })
        } catch (_: Exception) {
            Toast.makeText(this, "Could not open wallpaper picker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
