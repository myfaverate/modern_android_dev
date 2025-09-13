package io.github.customview.view1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.RegionIterator
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.TypefaceCompat
import com.google.android.material.canvas.CanvasCompat
import io.github.customview.R
import kotlin.math.tan
import androidx.core.graphics.withClip

private const val TAG: String = "CustomView1"

internal class CustomView1 : View {


    internal constructor(context: Context) : this(context = context, attrs = null)
    internal constructor(context: Context, attrs: AttributeSet?) : this(context = context, attrs = attrs, 0)
    internal constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val paint: Paint = Paint().apply {
        setColor(Color.GREEN)
        style = Paint.Style.FILL
        // strokeWidth = 2F
    }

    private val rect: Rect = Rect(100, 100, 800, 800)
    private val rect1: Rect = Rect(200, 200, 700, 700)
    private val rect2: Rect = Rect(300, 300, 600, 600)
    private val rect3: Rect = Rect(400, 400, 500, 500)

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.RED)
        canvas.save()

        canvas.clipRect(rect)
        canvas.drawColor(Color.GREEN)
        canvas.save()

        canvas.clipRect(rect1)
        canvas.drawColor(Color.BLUE)
        val count = canvas.save()

        canvas.clipRect(rect2)
        canvas.drawColor(Color.BLACK)
        canvas.save()

        canvas.clipRect(rect3)
        canvas.drawColor(Color.WHITE)


        canvas.restoreToCount(count)
        canvas.drawColor(Color.YELLOW)
    }

    private fun drawRegin(
        canvas: Canvas,
        region: Region,
        paint: Paint
    ) {
        val regionIterator: RegionIterator = RegionIterator(region)
        val rect = Rect()
        while (regionIterator.next(rect)){
            canvas.drawRect(rect, paint)
        }
    }

}
