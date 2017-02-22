package nittcprocon.glathlete

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent

internal class VisualAdjustmentView(context: Context) : GLSurfaceView(context) {
    private val TAG = "VisualAdjustmentView"

    init {
        setEGLContextClientVersion(2)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y

        when (e.action) {
            MotionEvent.ACTION_MOVE -> Log.v(TAG, "touched ($x, $y)")
        }

        return true
    }
}
