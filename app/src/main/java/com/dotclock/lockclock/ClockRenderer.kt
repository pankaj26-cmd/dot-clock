package com.dotclock.lockclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object ClockRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val redColor = Color.rgb(229, 57, 53)
    private val mutedColor = Color.rgb(140, 140, 140)
    private val timeColor = Color.rgb(247, 247, 247)
    private val gridDotColor = Color.rgb(35, 35, 35)

    private var burnInOffsetX = 0f
    private var burnInOffsetY = 0f

    fun updateBurnInOffset() {
        burnInOffsetX = Random.nextInt(-10, 11).toFloat()
        burnInOffsetY = Random.nextInt(-10, 11).toFloat()
    }

    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        context: Context,
        isClockVisible: Boolean = true,
        showGrid: Boolean = true
    ) {
        val w = width
        val h = height

        if (w <= 0f || h <= 0f) return

        paint.reset()
        paint.isAntiAlias = true

        // Deep black background filling entire screen
        canvas.drawColor(Color.BLACK)

        // If clock content is hidden, return after drawing black canvas
        if (!isClockVisible) {
            return
        }

        // Draw dot matrix background grid across entire screen
        if (showGrid) {
            paint.color = gridDotColor
            paint.style = Paint.Style.FILL
            val step = (w / 32f).coerceAtLeast(20f)
            var y = step / 2f
            while (y < h) {
                var x = step / 2f
                while (x < w) {
                    canvas.drawCircle(x, y, 1.25f, paint)
                    x += step
                }
                y += step
            }
        }

        canvas.save()
        canvas.translate(burnInOffsetX, burnInOffsetY)

        val isPortrait = h > w
        val timeCenterY = if (isPortrait) h * 0.45f else h * 0.50f
        val dateY = if (isPortrait) h * 0.26f else h * 0.18f
        val redDotsY = if (isPortrait) h * 0.65f else h * 0.82f

        // 1. Draw Date
        paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = (if (isPortrait) w * 0.045f else h * 0.065f).coerceIn(18f, 48f)
        paint.color = mutedColor
        val dateStr = SimpleDateFormat("EEEE  •  d MMMM", Locale.getDefault())
            .format(Date())
            .uppercase(Locale.getDefault())
        canvas.drawText(dateStr, w / 2f, dateY, paint)

        // 2. Draw Time
        paint.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val is24Hour = DateFormat.is24HourFormat(context)
        val timePattern = if (is24Hour) "HH:mm" else "h:mm"
        val timeStr = SimpleDateFormat(timePattern, Locale.getDefault()).format(Date())

        val baseTextSize = 200f
        paint.textSize = baseTextSize
        val maxTextWidth = w * 0.90f
        val maxTextHeight = if (isPortrait) h * 0.35f else h * 0.60f
        val measuredWidth = paint.measureText(timeStr).coerceAtLeast(1f)

        val widthScale = maxTextWidth / measuredWidth
        val heightScale = maxTextHeight / baseTextSize
        val scale = widthScale.coerceAtMost(heightScale).coerceAtLeast(0.1f)

        paint.textSize = baseTextSize * scale
        paint.color = timeColor

        val fm = paint.fontMetrics
        canvas.drawText(timeStr, w / 2f, timeCenterY - (fm.ascent + fm.descent) / 2f, paint)

        // 3. Draw 5 Red Accent Dots
        paint.color = redColor
        val radius = (if (isPortrait) w * 0.010f else h * 0.012f).coerceAtLeast(4f)
        val gap = radius * 5f
        for (i in -2..2) {
            canvas.drawCircle(w / 2f + i * gap, redDotsY, radius, paint)
        }

        canvas.restore()
    }
}
