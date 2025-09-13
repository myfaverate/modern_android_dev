package io.github.customview.view1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import io.github.customview.R
import kotlin.math.min
import androidx.core.graphics.withClip

internal class CustomCircleView : View {

    internal constructor(context: Context) : this(context = context, attrs = null)
    internal constructor(context: Context, attrs: AttributeSet?) : this(context = context, attrs = attrs, 0)
    internal constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val path: Path = Path()
    private val bitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.image1)
    }
    private val paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5F
        // shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }
    private val rectF = RectF(200F, 200F, 200F, 200F)

    override fun onDraw(canvas: Canvas) {
        path.addCircle(bitmap.width / 2F, bitmap.height / 2F, bitmap.width / 2F, Path.Direction.CCW)
        // canvas.drawPath(path, paint)
        canvas.withClip(path) {
            drawBitmap(bitmap, 0F, 0F, paint)
        }
    }
}