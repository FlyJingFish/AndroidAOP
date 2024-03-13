package com.flyjingfish.androidaop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import com.flyjingfish.android_aop_annotation.anno.AndroidAopModifyExtendsClass

@AndroidAopModifyExtendsClass("androidx.appcompat.widget.AppCompatImageView")
open class ReplaceImageView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    ImageView(context, attrs, defStyleAttr, defStyleRes) {
    private var shapeType: ShapeType
    private var mDrawPath = Path()
    private var mDrawRectF: RectF? = null
    private var mImagePaint: Paint? = null

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : this(context, attrs, defStyleAttr, 0)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShapeImageView)
        shapeType = ShapeType.getType(a.getInt(R.styleable.ShapeImageView_FlyJFish_shape, 1))
        a.recycle()
        Log.e("ReplaceImageView", "ReplaceImageView-----")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.e("ReplaceImageView", "onSizeChanged-----")
        val paddingLeft = ViewUtils.getViewPaddingLeft(this)
        val paddingRight = ViewUtils.getViewPaddingRight(this)
        mDrawPath = Path()
        mImagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mImagePaint!!.xfermode = null
        mDrawRectF = RectF(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        Log.e("ReplaceImageView", "onDraw-----")
        mDrawPath.reset()
        if (shapeType == ShapeType.OVAL) {
            mDrawPath.addOval(mDrawRectF!!, Path.Direction.CCW)
        } else if (shapeType == ShapeType.RECTANGLE) {
//            mDrawPath.addRoundRect(mDrawRectF!!, FloatArray(), Path.Direction.CCW)
        }
        if (shapeType == ShapeType.OVAL || shapeType == ShapeType.RECTANGLE) {
            canvas.saveLayer(mDrawRectF, mImagePaint, Canvas.ALL_SAVE_FLAG)
            canvas.drawPath(mDrawPath, mImagePaint!!)
            mImagePaint!!.xfermode = SRC_IN
            canvas.saveLayer(mDrawRectF, mImagePaint, Canvas.ALL_SAVE_FLAG)
            super.onDraw(canvas)
            canvas.restore()
            mImagePaint!!.xfermode = null
        } else {
            super.onDraw(canvas)
        }
    }

    enum class ShapeType(val type: Int) {
        NONE(0), RECTANGLE(1), OVAL(2);

        companion object {
            fun getType(type: Int): ShapeType {
                return if (type == 1) {
                    RECTANGLE
                } else if (type == 2) {
                    OVAL
                } else {
                    NONE
                }
            }
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        //做一些监测或者再次修改
        Log.e("ReplaceImageView", "setImageDrawable-----")
    }

    fun setShapeType(shapeType: ShapeType) {
        this.shapeType = shapeType
        invalidate()
    }

    companion object {
        private val SRC_IN = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }
}