package io.github.customview.view1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

private const val TAG: String = "CustomView2"

internal class CustomView2 @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs ,defStyleAttr){

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        strokeWidth = 5F
        style = Paint.Style.STROKE
    }

    private val squareRectF: RectF by lazy {
        RectF(width - 200F, 0F, width.toFloat(), 200F)
    }

    /**
     * 0, 0 -> 800, 800
     */
    // private val path: Path = Path().apply {
    //     paint.style = Paint.Style.FILL
    //     reset()
    //     addArc(0F, 600F, 200F, 800F, 0F, 180F)
    //     lineTo(0F, 500F)
    //     arcTo(0F, 0F, 800F, 800F, 180F, 90F, false)
    //     close()
    // }
    private val path: Path = Path().apply {
        paint.style = Paint.Style.FILL
        reset()
        // 逆时针半圆
        addArc(0F, 600F, 200F, 800F, 0F, 180F)
        lineTo(0F, 500F)
        // 逆时针 90°
        arcTo(0F, 0F, 800F, 800F, 180F, -90F, false)
        close()
    }

    override fun onDraw(canvas: Canvas) {
        Log.i(TAG, "onDraw...")
        canvas.drawRect(squareRectF, paint)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                performClick()
                if (squareRectF.contains(event.x, event.y)) {
                    paint.color = Color.GREEN
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                paint.color = Color.BLACK
                invalidate()
            }
            else -> {
                Log.i(TAG, "onTouchEvent -> else...")
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        Log.i(TAG, "performClick...")
        return super.performClick()
    }
}