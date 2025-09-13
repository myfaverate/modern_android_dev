package io.github.customview.view1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.RegionIterator
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withSave
import io.github.customview.R

private const val TAG: String = "ClipRgnView"
private const val CLIP_HEIGHT: Int = 30

internal class ClipRgnView  : View {

    internal constructor(context: Context) : this(context = context, attrs = null)
    internal constructor(context: Context, attrs: AttributeSet?) : this(context = context, attrs = attrs, 0)
    internal constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val bitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.image1)
    }

    private val region: Region = Region()

    private val paint: Paint = Paint()
    private val rect: Rect = Rect()

    private val tmpRect: Rect = Rect()
    private val tmpRectF: RectF = RectF()

    private var clipWidth: Int = 0

    override fun onDraw(canvas: Canvas) {
        val count: Int = bitmap.height / CLIP_HEIGHT
        region.setEmpty()
        for(i in 0 until count){
            if ((i and 1) == 0){
                rect.set(0, i * CLIP_HEIGHT, clipWidth, (i + 1) * CLIP_HEIGHT)
            } else {
                rect.set(bitmap.width - clipWidth, i * CLIP_HEIGHT, bitmap.width, (i + 1) * CLIP_HEIGHT)
            }
            region.union(rect)
        }

        canvas.withSave { // 1
            clipRegin(canvas, region)
            canvas.drawBitmap(bitmap, 0F, 0F, paint)
        }
        // drawRegin(canvas, region, paint) // 2

        if (clipWidth <= bitmap.width){
            clipWidth += 5
            invalidate()
        }
    }

    private fun drawRegin(
        canvas: Canvas,
        region: Region,
        paint: Paint
    ) {
        val regionIterator: RegionIterator = RegionIterator(region)
        while (regionIterator.next(tmpRect)){
            canvas.drawRect(tmpRect, paint)
        }
    }

    private fun clipRegin(
        canvas: Canvas,
        region: Region,
    ) {
        val path = regionToPath(region)
        canvas.clipPath(path)
    }

    private fun regionToPath(region: Region): Path {
        val path = Path()
        val iterator = RegionIterator(region)
        while (iterator.next(tmpRect)) {
            tmpRectF.set(tmpRect)
            path.addRect(tmpRectF, Path.Direction.CW)
        }
        return path
    }

}