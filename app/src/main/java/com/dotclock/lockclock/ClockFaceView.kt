package com.dotclock.lockclock

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class ClockFaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val handler = Handler(Looper.getMainLooper())
    private var isTicking = false

    var isClockContentVisible = false
        set(value) {
            field = value
            invalidate()
        }

    var onSingleTapListener: (() -> Unit)? = null
    var onDoubleTapListener: (() -> Unit)? = null

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onSingleTapListener?.invoke()
            performClick()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTapListener?.invoke()
            return true
        }
    })

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isClockContentVisible) {
                ClockRenderer.updateBurnInOffset()
                invalidate()
            }
            if (isTicking) {
                handler.postDelayed(this, 10_000L)
            }
        }
    }

    fun startTicking() {
        if (!isTicking) {
            isTicking = true
            handler.removeCallbacks(tickRunnable)
            handler.post(tickRunnable)
        }
    }

    fun stopTicking() {
        isTicking = false
        handler.removeCallbacks(tickRunnable)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = gestureDetector.onTouchEvent(event)
        return handled || super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTicking()
    }

    override fun onDetachedFromWindow() {
        stopTicking()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ClockRenderer.draw(canvas, width.toFloat(), height.toFloat(), context, isClockContentVisible)
    }
}
