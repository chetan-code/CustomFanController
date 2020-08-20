package com.example.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


private enum class FanSpeed (val label : Int){
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    //extension function to change current fan speed to next fan speed
    fun next() = when (this){
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }

}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    //intialize variable outside of onDraw()
    private var radius = 0.0f  //radius of the circle
    private var fanSpeed = FanSpeed.OFF  //the active selection
    //position variable which will be used to draw label and indicate circle position
    private val pointPosition: PointF = PointF(0.0f,0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)


    }

    //to set default properties of a view
    init {
        isClickable = true //view is clickable
    }

    /**
     * This allow us to handle click on the view (isClickable = true in init())
     * changes fanspeed and redraws the view to display change
     *
     * NOTE : we can add onclicklistener to view later as --> super.performClick calls onclicklistener
     */
    override fun performClick(): Boolean {
        //must happen first, which enables accessibility events as well as calls onClickListener().
        if(super.performClick()) return true

        //change fan speed to next on click
        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        //invalidate entire view - forcing onDraw() to draw it again
        invalidate()
        return true
    }

    /**
     * called every time view size changes
     *calculate position , dimensions etc here
    */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //current radius of the dial
        radius = (min(w,h) / 2.0 * 0.8).toFloat()
    }

    /**
     *Extension function on PointF
     * Calculates X,y coordinates on the screen for
     * the text label and the current indicator (0,1,2,3)
     * @param pos , current fan speed
     * @param radius , radius of dial
     */
    private fun PointF.computeXYForSpeed(pos : FanSpeed, radius : Float){
        //angles are in radians
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI/4)
        x = (radius * cos(angle)).toFloat() + width/2
        y = (radius * sin(angle)).toFloat() + height/2
    }

    /**
     * Renders the view on screen with Canvas and Paint classes
     * @param canvas , screen canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Set dial background color to green if selection not off.
        paint.color = if (fanSpeed == FanSpeed.OFF) Color.GRAY else Color.GREEN
        // Draw the dial.
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        // Draw the indicator circle.
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius/12, paint)

        // Draw the text labels.- 4 times
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }
}