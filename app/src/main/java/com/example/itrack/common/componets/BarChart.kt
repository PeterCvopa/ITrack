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

    private var mTitleTextSize: Float = 0f
    private var mTextWidth: Float = 0f
    private var mTextColor: Int = 0
    private var mData: List<Float> = listOf()
    private var barColor: Int = 0
    private var titleText: String = "Graph"
    private var mBarMaxNumber: Int = 10

    private val barValueTextPaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color = Color.BLACK
    }
    private val barLinePaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 2f
        alpha = 150
    }
    private val barTitleTextPaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        textSize = 60f
        color = Color.BLACK
    }
    private val barLineTextPaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        textSize = 20f
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
                mTitleTextSize = getInteger(R.styleable.BarChart_titleText, 0).toFloat()
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

    fun getMaxNumberBar(): Int {
        return mBarMaxNumber
    }

    fun setMaxNumberBars(maxNumberBars: Int) {
        this.mBarMaxNumber = maxNumberBars
        invalidate()
        requestLayout()
    }

    fun setData(mData: List<Float>) {
        if (mData.size > 10) {
            this.mData = mData.take(10)
        }
        this.mData = mData
        invalidate()
        requestLayout()
    }

    private fun getNumberOfBars(): Int {
        return Math.min(mData.size, mBarMaxNumber)
    }

    override fun onDraw(canvas: Canvas) {
        //TODO wip need fix issiu when number of data items is too low
        //TODO fix alignment
        super.onDraw(canvas)
        val leftGraphOffset = 30
        val graphWidth = width - leftGraphOffset
        val numberOfBars = getNumberOfBars()
        val barWidth = calculateBarWidth(numberOfBars, graphWidth)
        val barSpacing = calculateBarSpaceWidth(numberOfBars, graphWidth)
        val titleHeight = barTitleTextPaint.textSize
        val graphHeight = height.toFloat()
        val heightMultiplicationCoefficient = calculateHeightCoefficient(graphHeight - titleHeight)
        val middleGraphYCoordinate = ((graphHeight - titleHeight) / 2) + titleHeight
        val oneQuarterLineHeight = ((graphHeight - titleHeight) / 4) + titleHeight
        val threeQuarterLineHeight = (3 * (graphHeight - titleHeight) / 4) + titleHeight
        var graphOffSet = leftGraphOffset.toFloat()
        canvas.apply {
            ///horizontal bottom line
            drawLine(graphOffSet, graphHeight, width.toFloat(), graphHeight, barLinePaint)
            ///horizontal  lines
            drawText(
                "100",
                0f,
                oneQuarterLineHeight + barLineTextPaint.textSize / 2,
                barLineTextPaint
            )
            drawLine(graphOffSet, oneQuarterLineHeight, width.toFloat(), oneQuarterLineHeight, barLinePaint)
            drawText(
                "100",
                0f,
                middleGraphYCoordinate + barLineTextPaint.textSize / 2,
                barLineTextPaint
            )
            drawLine(graphOffSet, middleGraphYCoordinate, width.toFloat(), middleGraphYCoordinate, barLinePaint)
            drawText(
                "100",
                0f,
                threeQuarterLineHeight + barLineTextPaint.textSize / 2,
                barLineTextPaint
            )
            drawLine(graphOffSet, threeQuarterLineHeight, width.toFloat(), threeQuarterLineHeight, barLinePaint)
            drawText(
                "100",
                0f,
                titleHeight + barLineTextPaint.textSize / 2,
                barLineTextPaint
            )
            drawLine(graphOffSet, titleHeight, width.toFloat(), titleHeight, barLinePaint)
            // start vertical line
            //drawLine(0f, titleHeight, 0f, graphHeight, barLinePaint)
            for (index in 0 until numberOfBars) {
                drawRect(
                    graphOffSet,
                    graphHeight - heightMultiplicationCoefficient * mData[index],
                    graphOffSet + barWidth,
                    graphHeight,
                    mRectanglePain
                )
                drawText(
                    mData[index].toString(),
                    graphOffSet + barWidth / 2,
                    graphHeight - heightMultiplicationCoefficient * mData[index],
                    barValueTextPaint
                )
                drawText(
                    titleText,
                    50f,
                    titleHeight,
                    barTitleTextPaint
                )
                graphOffSet += barSpacing + barWidth
            }

        }
    }

    private val mTextPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = mTextColor
        if (mTitleTextSize == 0f) {
            mTitleTextSize = textSize
        } else {
            textSize = mTitleTextSize
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