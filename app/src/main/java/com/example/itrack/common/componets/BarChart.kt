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
import com.example.itrack.common.StringHelper


class BarChart(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mData: List<Float> = listOf()

    private var mTitleTextSize: Float = 0f
    private var mTextWidth: Float = 0f
    private var mTextColor: Int = 0

    private var barColor: Int = 0
    private var titleText: String = "Graph"
    private var mBarMaxNumber: Int = 10
    private val spacing = GraphProportions.MEDIUM
    private val leftGraphOffset = 50

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
                mTitleTextSize = getInteger(R.styleable.BarChart_titleTextSize, 50).toFloat()
            } finally {
                recycle()
            }
        }
    }

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
        textSize = mTitleTextSize
        color = Color.BLACK
    }
    private val barLineTextPaint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        color = Color.BLACK
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
        //TODO fix alignment
        super.onDraw(canvas)
        val graphWidth = width - leftGraphOffset
        val numberOfBars = getNumberOfBars()
        val barWidth = calculateBarWidth(numberOfBars, graphWidth)
        val barSpacing = calculateBarSpacingWidth(numberOfBars, graphWidth)
        val titleSpaceHeight = barTitleTextPaint.textSize + 20f
        val graphHeight = height.toFloat()
        val heightMultiplicationCoefficient = calculateHeightCoefficient(graphHeight - titleSpaceHeight)

        val threeQuarterLineHeight = ((graphHeight - titleSpaceHeight) / 4) + titleSpaceHeight
        val middleGraphYCoordinate = ((graphHeight - titleSpaceHeight) / 2) + titleSpaceHeight
        val oneQuarterLineHeight = (3 * (graphHeight - titleSpaceHeight) / 4) + titleSpaceHeight

        val maxValue = mData.max() ?: 0.0f
        val oneQuarterLineTitle = maxValue / 4
        val middleQuarterLineTitle = maxValue / 2
        val threeQuarterLineTitle = 3 * maxValue / 4

        var graphOffSet = leftGraphOffset.toFloat()
        canvas.apply {
            ///horizontal bottom line
            drawLine(graphOffSet, graphHeight, width.toFloat(), graphHeight, barLinePaint)
            ///horizontal  lines
            drawText(
                StringHelper.toText(oneQuarterLineTitle),
                0f,
                oneQuarterLineHeight + barLineTextPaint.textSize / 2,
                barLineTextPaint
            )
            drawLine(graphOffSet, oneQuarterLineHeight, width.toFloat(), oneQuarterLineHeight, barLinePaint)
            drawText(
                StringHelper.toText(middleQuarterLineTitle),
                0f,
                middleGraphYCoordinate + barLineTextPaint.textSize / 2,
                barLineTextPaint
            )
            drawLine(graphOffSet, middleGraphYCoordinate, width.toFloat(), middleGraphYCoordinate, barLinePaint)
            drawText(
                StringHelper.toText(threeQuarterLineTitle),
                0f,
                threeQuarterLineHeight + barLineTextPaint.textSize / 2,
                barLineTextPaint
            )
            drawLine(graphOffSet, threeQuarterLineHeight, width.toFloat(), threeQuarterLineHeight, barLinePaint)
            drawText(
                StringHelper.toText(maxValue),
                0f,
                titleSpaceHeight + barLineTextPaint.textSize / 2,
                barLineTextPaint
            )

            drawLine(graphOffSet, titleSpaceHeight, width.toFloat(), titleSpaceHeight, barLinePaint)
            drawText(
                titleText,
                50f,
                barTitleTextPaint.textSize,
                barTitleTextPaint
            )
            for (index in 0 until numberOfBars) {
                drawRect(
                    graphOffSet,
                    graphHeight - heightMultiplicationCoefficient * mData[index],
                    graphOffSet + barWidth,
                    graphHeight,
                    mRectanglePain
                )
                drawText(
                    StringHelper.toText(mData[index]),
                    graphOffSet + barWidth / 3,
                    graphHeight - heightMultiplicationCoefficient * mData[index] + barValueTextPaint.textSize,
                    barValueTextPaint
                )
                graphOffSet += barSpacing + barWidth
            }
        }
    }

    private val mRectanglePain = Paint(ANTI_ALIAS_FLAG).apply {
        color = barColor
        strokeWidth = 10f
    }

    private val mShadowPaint = Paint(0).apply {
        color = 0x101010
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }


    private fun calculateBarWidth(realBarCount: Int, width: Int): Float {
        return if (width == 0) {
            0f

        } else {
            val barCount = if (realBarCount < 3) {
                3
            } else {
                realBarCount
            }

            (width / (barCount - spacing.spacingProportion)) * spacing.barProportion
        }
    }

    private fun calculateBarSpacingWidth(realBarCount: Int, width: Int): Float {
        return if (width == 0) {
            0f
        } else {
            val barCount = if (realBarCount < 3) {
                3
            } else {
                realBarCount
            }
            (width / (barCount - spacing.spacingProportion)) * spacing.spacingProportion
        }
    }

    private fun calculateHeightCoefficient(maxHeight: Float): Float {
        return mData.max()?.run { maxHeight / this } ?: 0f
    }

    enum class GraphProportions(var spacingProportion: Float) {
        LOW(0.1f), MEDIUM(0.3f), HIGH(0.5f);

        val barProportion: Float = 1.0f - spacingProportion
    }
}