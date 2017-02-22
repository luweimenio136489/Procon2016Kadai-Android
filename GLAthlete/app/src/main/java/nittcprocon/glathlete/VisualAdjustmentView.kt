package nittcprocon.glathlete

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

class VisualAdjustmentView(context: Context) : GLSurfaceView(context) {
    constructor(context: Context, attributeSet: AttributeSet) : this(context)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : this(context, attributeSet)
    private val TAG = "VisualAdjustmentView"

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y

        when (e.action) {
            MotionEvent.ACTION_MOVE -> Log.v(TAG, "touched ($x, $y)")
        }

        return true
    }
}
