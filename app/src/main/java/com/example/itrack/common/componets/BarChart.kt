package com.example.itrack.common.componets

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import com.example.itrack.R


class BarChart(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mTextHeight: Float = 0f
    private var mTextWidth: Float = 0f
    private var mTextColor: Int = 0
    private var mData: List<Int> = listOf()
    private var barColor: Int = 0
    private var titleText: String = "Graph"
    private var mBarMaxNumber: Int = 10

    private val barTextPaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color = Color.BLACK
    }
    private val barLinePaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    private val barTitleTextPaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        textSize = 60f
        color = Color.BLACK
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BarChart,
            0, 0
        ).apply {
            try {
                mBarMaxNumber = getInteger(R.styleable.BarChart_maxNumberOfBars, 0)
                barColor = getColor(R.styleable.BarChart_barColor, resources.getColor(R.color.primary_material_dark))
                titleText = getString(R.styleable.BarChart_titleText) ?: "Graph"
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = View.resolveSizeAndState(minw, widthMeasureSpec, 1)

        val minh: Int = View.MeasureSpec.getSize(w) - mTextWidth.toInt() + paddingBottom + paddingTop
        val h: Int = View.resolveSizeAndState(
            minh,
            heightMeasureSpec,
            0
        )
        setMeasuredDimension(w, h)
    }

    private fun getNumberOfBars(): Int {
        return Math.min(mData.size, mBarMaxNumber)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val numberOfBars = getNumberOfBars()
        val recWidth = calculateBarWidth(numberOfBars, width)
        val barSpace = calculateBarSpaceWidth(numberOfBars, width)
        var barOffSet = 0F
        val graphHeight = canvas.height.toFloat()
        val heightMultiplicationCoefficient = calculateHeightCoefficient(graphHeight - 60f)

        canvas.apply {
            for (index in 0 until numberOfBars) {

                drawLine(0f, 60f, width.toFloat(), 60f, barLinePaint)
                drawRect(
                    barOffSet,
                    graphHeight - heightMultiplicationCoefficient * mData[index],
                    barOffSet + recWidth,
                    graphHeight,
                    mRectanglePain
                )
                drawText(
                    mData[index].toString(),
                    barOffSet + recWidth / 2,
                    graphHeight - heightMultiplicationCoefficient * mData[index],
                    barTextPaint
                )
                drawText(
                    titleText,
                    100f,
                    60f,
                    barTitleTextPaint
                )
                barOffSet += barSpace + recWidth
            }
        }
    }

    fun getMaxNumberBar(): Int {
        return mBarMaxNumber
    }

    fun setMaxNumberBars(maxNumberBars: Int) {
        this.mBarMaxNumber = maxNumberBars
        invalidate()
        requestLayout()
    }

    fun setData(mData: List<Int>) {
        if (mData.size > 10) {
            this.mData = mData.take(10)
        }
        this.mData = mData
        invalidate()
        requestLayout()
    }

    private val mTextPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = mTextColor
        if (mTextHeight == 0f) {
            mTextHeight = textSize
        } else {
            textSize = mTextHeight
        }
    }

    private val mRectanglePain = Paint(ANTI_ALIAS_FLAG).apply {
        color = barColor
        strokeWidth = 10f
    }

    private val mPiePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val mShadowPaint = Paint(0).apply {
        color = 0x101010
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }


    private fun calculateBarWidth(barCount: Int, width: Int): Float {
        return if (width == 0) {
            0f
        } else {
            (width / (barCount - 0.1f)) * 0.9f
        }
    }

    private fun calculateBarSpaceWidth(barCount: Int, width: Int): Float {
        return if (width == 0) {
            0f
        } else {
            (width / (barCount - 0.1f)) * 0.1f
        }
    }

    private fun calculateHeightCoefficient(maxHeight: Float): Float {
        return mData.max()?.run { maxHeight / this } ?: 0f
    }
}