package nittcprocon.glathlete;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

class VisualAdjustmentView extends GLSurfaceView {
    private static final String TAG = "VisualAdjustmentView";
    private VisualAdjustmentRenderer renderer;

    VisualAdjustmentView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        renderer = new VisualAdjustmentRenderer(this);
        setRenderer(renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch(e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Log.v(TAG, "touched (" + x + ", " + y + ")");
        }

        return true;
    }
}
