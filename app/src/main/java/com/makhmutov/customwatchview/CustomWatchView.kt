package com.makhmutov.customwatchview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class CustomWatchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var hasNumbers = false
    private var hasDivisions = false
    private var hasSeconds = false
    private var isSquare = false

    private var backgroundColor: Int = Color.WHITE
    private var mainColor: Int = Color.BLACK

    private var hour: Int = 0
    private var minute: Int = 0
    private var second: Int = 0

    private val paint = Paint()

    private val timeShowExchangeHandler = Handler(Looper.getMainLooper())

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CustomWatchView,
            defStyleAttr,
            defStyleRes
        )

        hasNumbers = typedArray.getBoolean(R.styleable.CustomWatchView_hasNumbers, false)
        hasSeconds = typedArray.getBoolean(R.styleable.CustomWatchView_hasSeconds, false)
        hasDivisions = typedArray.getBoolean(R.styleable.CustomWatchView_hasDivisions, false)
        isSquare = typedArray.getBoolean(R.styleable.CustomWatchView_isSquare, false)
        backgroundColor =
            typedArray.getColor(R.styleable.CustomWatchView_backgroundColor, Color.WHITE)
        mainColor = typedArray.getColor(R.styleable.CustomWatchView_mainColor, Color.BLACK)

        typedArray.recycle()
    }

    private val timeRunnable = object : Runnable {
        override fun run() {
            val currency = getTime()
            hour = currency.first
            minute = currency.second
            second = currency.third

            invalidate()

            timeShowExchangeHandler.postDelayed(this, 1000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        timeShowExchangeHandler.post(timeRunnable)
    }

    private val backgroundRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = mainColor
        backgroundRect.set(0f, 0f, width.toFloat(), height.toFloat())
        val roundedCorner = minOf(width, height) / 8f
        canvas.drawRoundRect(
            backgroundRect,
            roundedCorner,
            roundedCorner,
            paint
        )

        paint.color = backgroundColor
        val cx = width / 2f
        val cy = height / 2f
        val dialRadius = (minOf(width, height) / 2 * 0.9).toFloat()
        canvas.drawCircle(
            cx,
            cy,
            dialRadius,
            paint
        )

        if (hasDivisions) {
            drawDivisions(dialRadius, cx, cy, canvas)
        }

        if (hasNumbers) {
            drawNumbers(dialRadius, cx, cy, canvas)
        }

        drawHourHand(dialRadius, cx, cy, canvas)
        drawMinuteHand(dialRadius, cx, cy, canvas)

        if (hasSeconds) {
            drawSecondHand(dialRadius, cx, cy, canvas)
        }
    }

    private fun drawDivisions(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas,
    ) {
        val strokeLengthLong = dialRadius / 15
        val strokeLengthShort = dialRadius / 30
        val strokeWidthLong = minOf(width, height) / 40f
        val strokeWidthShort = strokeWidthLong / 2

        for (i in 0 until 60) {
            val angle = Math.PI * i / 30
            val startX = (cx + cos(angle) * dialRadius).toFloat()
            val startY = (cy + sin(angle) * dialRadius).toFloat()
            val stopX: Float
            val stopY: Float

            if (i % 5 == 0) {
                paint.strokeWidth = strokeWidthLong
                stopX = (cx + cos(angle) * (dialRadius - strokeLengthLong)).toFloat()
                stopY = (cy + sin(angle) * (dialRadius - strokeLengthLong)).toFloat()
            } else {
                paint.strokeWidth = strokeWidthShort
                stopX = (cx + cos(angle) * (dialRadius - strokeLengthShort)).toFloat()
                stopY = (cy + sin(angle) * (dialRadius - strokeLengthShort)).toFloat()
            }

            paint.color = mainColor
            canvas.drawLine(startX, startY, stopX, stopY, paint)
        }
    }

    private fun drawSecondHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas,
    ) {
        canvas.save()

        val secondRotation = second * 6f
        canvas.rotate(secondRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = minOf(width, height) / 60f
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.8f, paint)

        canvas.restore()
    }


    private fun drawMinuteHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas,
    ) {
        canvas.save()

        val minuteRotation = minute * 6f + second / 10f
        canvas.rotate(minuteRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = minOf(width, height) / 30f
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.6f, paint)

        canvas.restore()
    }


    private fun drawHourHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas,
    ) {
        canvas.save()

        val hourRotation = (hour % 12) * 30f + minute / 2f
        canvas.rotate(hourRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = minOf(width, height) / 20f
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.4f, paint)

        canvas.restore()
    }

    private fun drawNumbers(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas,
    ) {
        paint.color = mainColor
        val textSize = dialRadius / 5
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER
        for (number in 1..12) {
            val angle = Math.PI * (number - 3) / 6
            val x = (cx + cos(angle) * (dialRadius - textSize)).toFloat()
            val y = (cy + sin(angle) * (dialRadius - textSize)).toFloat()

            val textHeight = paint.descent() - paint.ascent()
            val textOffset = (textHeight / 2) - paint.descent()
            canvas.drawText(number.toString(), x, y + textOffset, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val size = minOf(widthSize, heightSize)
        val finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)

        if (isSquare){
            super.onMeasure(finalMeasureSpec, finalMeasureSpec)
        }
        else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

    }


    private fun getTime(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        return Triple(hour, minute, second)
    }

}