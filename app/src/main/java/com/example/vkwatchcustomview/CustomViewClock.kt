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
import kotlin.properties.Delegates

class CustomViewClock(
    context: Context,
    attrs: AttributeSet? = null,
): View(context, attrs) {

    private var numeralsType: NumeralsType = NumeralsType.NONE
    private var hasSeconds: Boolean = false
    private var dialColor: Int = Color.WHITE
    private var mainColor: Int = Color.BLACK
    private var secondHandColor: Int = Color.RED

    private var hour: Int = 0
    private var minute: Int = 0
    private var second: Int = 0

    private val paint = Paint()

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
                hasSeconds = getBoolean(R.styleable.CustomViewClock_hasSeconds, false)
                dialColor = getColor(R.styleable.CustomViewClock_dialColor, Color.WHITE)
                mainColor = getColor(R.styleable.CustomViewClock_mainColor, Color.BLACK)
                secondHandColor = getColor(R.styleable.CustomViewClock_secondHandColor, Color.RED)
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

    private val rectF = RectF()
    private var side by Delegates.notNull<Float>()



    private fun getCurrentTime(): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        return Triple(hour, minute, second)
    }

    companion object {
        enum class NumeralsType {
            NONE, ARABIC, ROMAN
        }

        private const val TYPE_NONE = 0
        private const val TYPE_ARABIC = 1
        private const val TYPE_ROMAN = 2
    }

}