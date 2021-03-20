package com.socap.notekeeper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat.getColor

/**
 * TODO: document your custom view class.
 */
class ModuleStatusView : View {
    var moduleStatus: BooleanArray = booleanArrayOf()
    private val spacing = 30f
    private val shapeSize = 144f
    private val outlineWidth = 6f
    private var moduleRectangles: Array<Rect> = emptyArray()
    private val outlineColor by lazy { Color.BLACK }
    private var fillColor = 0
    private lateinit var paintOutline: Paint
    private lateinit var paintFill: Paint
    private val radius by lazy { (shapeSize - outlineWidth) / 2 }
    private var maxHorizontalModules = 0

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        if (isInEditMode) setupEditModeValues()
        else fillColor = getColor(context, R.color.pluralsight_orange)
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ModuleStatusView, defStyle, 0
        )
        a.recycle()
        setupModuleRectangles()

        paintOutline = Paint(Paint.ANTI_ALIAS_FLAG)
        paintOutline.style = Paint.Style.STROKE
        paintOutline.strokeWidth = outlineWidth
        paintOutline.color = outlineColor

        paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill.style = Paint.Style.FILL
        paintFill.color = fillColor
    }

    private fun setupEditModeValues() {
        val exampleModuleValues = BooleanArray(EDIT_MODE_MODULE_COUNT)
        val middle = EDIT_MODE_MODULE_COUNT / 2
        for (i in 0 until middle) exampleModuleValues[i] = true
        moduleStatus = exampleModuleValues
        fillColor = Color.RED
    }

    private fun setupModuleRectangles() {
        moduleRectangles = Array(moduleStatus.size) { Rect() }
        for (moduleIndex in moduleRectangles.indices) {
            val x = paddingStart + moduleIndex * (shapeSize + spacing).toInt()
            val y = paddingTop
            moduleRectangles[moduleIndex] = Rect(
                x, y, (x + shapeSize).toInt(), (y + shapeSize).toInt()
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        moduleRectangles.forEachIndexed { moduleIndex, _ ->
            val x: Float = moduleRectangles[moduleIndex].centerX().toFloat()
            val y: Float = moduleRectangles[moduleIndex].centerY().toFloat()

            if (moduleStatus[moduleIndex])
                canvas.drawCircle(x, y, radius, paintFill)
            canvas.drawCircle(x, y, radius, paintOutline)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = specWidth - paddingStart - paddingEnd
        val horizontalModulesThatCanFit = (availableWidth / (shapeSize + spacing)).toInt()
        //maxHorizontalModules = Math.min(horizontalModulesThatCanFit, moduleStatus.size)
        maxHorizontalModules = horizontalModulesThatCanFit.coerceAtMost(moduleStatus.size)

        var desiredWidth: Int = (maxHorizontalModules * (shapeSize + spacing) - spacing).toInt()
        desiredWidth += paddingStart + paddingEnd

        val rows = (moduleStatus.size - 1 ) / maxHorizontalModules + 1
        var desiredHeight: Int = (rows * (shapeSize + spacing) - spacing).toInt()
        desiredHeight += paddingTop + paddingBottom

        val width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0)
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    companion object {
        private const val EDIT_MODE_MODULE_COUNT = 7
    }
}