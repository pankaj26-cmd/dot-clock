package com.dotclock.lockclock

import android.service.dreams.DreamService
import android.view.WindowManager

class DotClockDreamService : DreamService() {
    private var clockFace: ClockFaceView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = true
        isFullscreen = true
        isScreenBright = false // Start dark so clock only appears on single tap

        clockFace = ClockFaceView(this).apply {
            onSingleTapListener = {
                wakeUpScreenSaver()
            }
            onDoubleTapListener = {
                finish()
            }
        }
        setContentView(clockFace)
    }

    private fun wakeUpScreenSaver() {
        try {
            window?.let { win ->
                val params = win.attributes
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                win.attributes = params
            }
            clockFace?.invalidate()
        } catch (_: Exception) {
        }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        clockFace?.startTicking()
    }

    override fun onDreamingStopped() {
        clockFace?.stopTicking()
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        clockFace = null
        super.onDetachedFromWindow()
    }
}
