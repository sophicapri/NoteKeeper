package com.socap.notekeeper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat.getColor

/**
 * TODO: document your custom view class.
 */
class ModuleStatusView : View {
    var moduleStatus: BooleanArray = booleanArrayOf()
    private var spacing = 0f
    private var shapeSize = 0f
    private var outlineWidth = 0f
    private var moduleRectangles: Array<Rect> = emptyArray()
    private var outlineColor = 0
    private var fillColor = 0
    private lateinit var paintOutline: Paint
    private lateinit var paintFill: Paint
    private val radius by lazy { (shapeSize - outlineWidth) / 2 }

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

        spacing = 30f
        shapeSize = 144f
        outlineWidth = 6f
        outlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, Color.BLACK)
        fillColor = a.getColor(R.styleable.ModuleStatusView_fillColor, Color.RED)

        a.recycle()

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
        //fillColor = Color.RED
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHorizontalModules = getMaxHorizontalModules(specWidth)

        var desiredWidth: Int = (maxHorizontalModules * (shapeSize + spacing) - spacing).toInt()
        desiredWidth += paddingStart + paddingEnd

        val rows = (moduleStatus.size - 1) / maxHorizontalModules + 1
        var desiredHeight: Int = (rows * (shapeSize + spacing) - spacing).toInt()
        desiredHeight += paddingTop + paddingBottom

        val width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0)
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        setupModuleRectangles(w)
    }

    private fun setupModuleRectangles(width: Int) {
        val maxHorizontalModules = getMaxHorizontalModules(width)
        moduleRectangles = Array(moduleStatus.size) { Rect() }
        for (moduleIndex in moduleRectangles.indices) {
            val column = moduleIndex % maxHorizontalModules
            val row = moduleIndex / maxHorizontalModules
            val x = paddingStart + column * (shapeSize + spacing).toInt()
            val y = paddingTop + row * (shapeSize + spacing).toInt()
            moduleRectangles[moduleIndex] = Rect(
                x, y, (x + shapeSize).toInt(), (y + shapeSize).toInt()
            )
        }
    }

    private fun getMaxHorizontalModules(width: Int): Int {
        val availableWidth = width - paddingStart - paddingEnd
        val horizontalModulesThatCanFit = (availableWidth / (shapeSize + spacing)).toInt()
        //maxHorizontalModules = Math.min(horizontalModulesThatCanFit, moduleStatus.size)
        return horizontalModulesThatCanFit.coerceAtMost(moduleStatus.size)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action){
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val moduleIndex = findItemAtPoint(event.x, event.y)
                onModuleSelected(moduleIndex)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findItemAtPoint(x: Float, y: Float): Int {
        var moduleIndex = INVALID_INDEX
        for (index in 0..moduleRectangles.size){
            if (moduleRectangles[index].contains(x.toInt(), y.toInt())) {
                moduleIndex = index
                break
            }
        }
        return moduleIndex
    }

    private fun onModuleSelected(moduleIndex: Int) {
        if (moduleIndex == INVALID_INDEX)
            return
        moduleStatus[moduleIndex] = !moduleStatus[moduleIndex]

        // to update the view
        invalidate()
    }

    companion object {
        private const val EDIT_MODE_MODULE_COUNT = 7
        private const val INVALID_INDEX = -1
    }
}