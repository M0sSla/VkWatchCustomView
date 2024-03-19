package com.example.vkwatchcustomview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class CustomViewClock(
    context: Context,
    attrs: AttributeSet? = null,
): View(context, attrs) {

    private var numeralsType: NumeralsType = NumeralsType.NONE
    private var hasSecondHand: Boolean = false
    private var secondaryColor: Int = Color.WHITE
    private var primaryColor: Int = Color.BLACK
    private var secondHandColor: Int = Color.GREEN

    private val paint = Paint()

    private var hours = 0
    private var minutes = 0
    private var seconds = 0

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.CustomViewClock,
        ).apply {
            try {
                numeralsType = when (getInt(R.styleable.CustomViewClock_numeralsType, TYPE_NONE)) {
                    TYPE_NONE -> NumeralsType.NONE
                    TYPE_ARABIC -> NumeralsType.ARABIC
                    TYPE_ROMAN -> NumeralsType.ROMAN
                    else -> NumeralsType.NONE
                }
                hasSecondHand = getBoolean(R.styleable.CustomViewClock_hasSecondHand, false)
                secondaryColor = getColor(R.styleable.CustomViewClock_secondaryColor, Color.WHITE)
                primaryColor = getColor(R.styleable.CustomViewClock_primaryColor, Color.GRAY)
                secondHandColor = getColor(R.styleable.CustomViewClock_secondHandColor, Color.MAGENTA)
            } finally {
                recycle()
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postInvalidateOnAnimation()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> minOf(
                widthSize,
                heightSize
            )

            widthMode == MeasureSpec.EXACTLY -> widthSize
            heightMode == MeasureSpec.EXACTLY -> heightSize

            else -> minOf(widthSize, heightSize)
        }

        setMeasuredDimension(size, size)
    }




    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val side = minOf(width, height).toFloat()
        val cx = side / 2
        val cy = side / 2
        val dialRadius = (side / 2 * 0.9f)
        val centerRadius = dialRadius / 20
        drawDial(dialRadius, centerRadius, cx, cy, canvas)

        val currentTime = getCurrentTime()

        hours = currentTime.first
        minutes = currentTime.second
        seconds = currentTime.third

        paint.color = primaryColor
        canvas.drawCircle(cx, cy,dialRadius, paint)
        paint.reset()

        if (numeralsType != NumeralsType.NONE) {
            drawNumbers(dialRadius, cx, cy, canvas, numeralsType)
        }
        drawHourHand(dialRadius, centerRadius, cx, cy, side, canvas)
        drawMinuteHand(dialRadius, centerRadius, cx, cy, side, canvas)
        if (hasSecondHand) {
            drawSecondHand(dialRadius, centerRadius, cx, cy, side, canvas)
            postInvalidateOnAnimation()
        } else {
            postDelayed({ invalidate() }, (60 - seconds) * 1000L)
        }
    }


    /**
     * Отрисовка циферблата, в зависимости от состояния NumeralsType
     */
    private fun drawNumbers(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas,
        type: NumeralsType
    ) {
        paint.reset()
        paint.color = primaryColor
        val textSize = dialRadius / 6
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER
        for (number in 1..12) {
            val angle = Math.PI * (number - 3) / 6
            val x = (cx + cos(angle) * (dialRadius - textSize)).toFloat()
            val y = (cy + sin(angle) * (dialRadius - textSize)).toFloat()

            val textHeight = paint.descent() - paint.ascent()
            val textOffset = (textHeight / 2) - paint.descent()
            if (type == NumeralsType.ARABIC) {
                canvas.drawText(number.toString(), x, y + textOffset, paint)
            } else {
                canvas.drawText(roman[number - 1], x, y + textOffset, paint)
            }
        }
    }

    /**
     * Отрисовка фона и центра часов
     */
    private fun drawDial(
        dialRadius: Float,
        centerRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        paint.reset()
        paint.color = secondaryColor
        canvas.drawCircle(cx, cy, dialRadius, paint)

        paint.color = primaryColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dialRadius / 10
        canvas.drawCircle(cx, cy, centerRadius, paint)
    }

    private fun getCurrentTime(): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        return Triple(hour, minute, second)
    }


    private val rectF = RectF()
    private fun drawHourHand(
        dialRadius: Float,
        centerRadius: Float,
        cx: Float,
        cy: Float,
        side: Float,
        canvas: Canvas
    ) {
        paint.reset()
        canvas.save()

        val hourRotation = getHourRotation()
        canvas.rotate(hourRotation, cx, cy)

        paint.color = primaryColor
        rectF.set(
            cx + centerRadius * 0.7f,
            cy - centerRadius,
            cx - centerRadius * 0.7f,
            cy - dialRadius * 0.5f
        )
        val roundedCorner = side / 32
        canvas.drawRoundRect(
            rectF,
            roundedCorner,
            roundedCorner,
            paint
        )

        canvas.restore()
    }

    private fun drawMinuteHand(
        dialRadius: Float,
        centerRadius: Float,
        cx: Float,
        cy: Float,
        side: Float,
        canvas: Canvas
    ) {
        paint.reset()
        canvas.save()

        val minuteRotation = getMinuteRotation()
        canvas.rotate(minuteRotation, cx, cy)

        paint.color = primaryColor
        rectF.set(
            cx + centerRadius * 0.5f,
            cy - centerRadius,
            cx - centerRadius * 0.5f,
            cy - dialRadius * 0.7f
        )
        val roundedCorner = side / 32
        canvas.drawRoundRect(
            rectF,
            roundedCorner,
            roundedCorner,
            paint
        )

        canvas.restore()
    }

    private fun drawSecondHand(
        dialRadius: Float,
        centerRadius: Float,
        cx: Float,
        cy: Float,
        side: Float,
        canvas: Canvas
    ) {
        paint.reset()
        canvas.save()

        val secondRotation = getSecondRotation()
        canvas.rotate(secondRotation, cx, cy)

        paint.color = secondHandColor
        rectF.set(
            cx + centerRadius / 4,
            cy + 2 * centerRadius,
            cx - centerRadius / 4,
            cy - dialRadius * 0.9f
        )
        val roundedCorner = side / 32
        canvas.drawRoundRect(
            rectF,
            roundedCorner,
            roundedCorner,
            paint
        )

        canvas.restore()
    }


    private fun getHourRotation(): Float = (hours % 12) * 30f + minutes / 2f
    private fun getMinuteRotation(): Float = minutes * 6f + seconds / 10f
    private fun getSecondRotation(): Float = seconds * 6f
    companion object {
        enum class NumeralsType {
            NONE, ARABIC, ROMAN
        }

        private const val TYPE_NONE = 0
        private const val TYPE_ARABIC = 1
        private const val TYPE_ROMAN = 2
        private val roman = arrayOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
    }

}