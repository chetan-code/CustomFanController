package com.example.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
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

private var fanSpeedLowColor = 0
private var fanSpeedMediumColor = 0
private var fanSpeedMaxColor = 0

class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    //intialize variable outside of onDraw()
    private var radius = 0.0f  //radius of the circle
    private var fanSpeed = FanSpeed.OFF  //the active selection
    //position variable which will be used to fdraw label and indicate circle position
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
        //we recieve attribute set in the constructor
        //we can use them to set properties of local variables
        context.withStyledAttributes(attrs,R.styleable.DialView){
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1,0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2,0)
            fanSpeedMaxColor = getColor(R.styleable.DialView_fanColor3,0)
        }

        updateContentDescription()

        //accessibility feature - help in explaining what action to take
        ViewCompat.setAccessibilityDelegate(this , object : AccessibilityDelegateCompat(){
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    context.getString(if (fanSpeed != FanSpeed.HIGH) R.string.change else R.string.reset)
                )
                info.addAction(customClick)
            }
        })
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
        //tell talkback
        updateContentDescription()
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
        paint.color = when (fanSpeed){
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedMaxColor
        }
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

    /**
     * content description allow us to make view accessible for visually impaired people
     * we need to update the fanspeed description on click
     */
    fun updateContentDescription(){
        contentDescription = resources.getString(fanSpeed.label)
    }
}