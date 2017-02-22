package nittcprocon.glathlete

import android.net.Uri
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager

/**
 * ビジュアル調整
 * ビデオを再生する板ポリと、その上でパラメータを表示するモデル、ブレンディング部分を表示する板ポリ
 */

class VisualAdjustmentActivity : AppCompatActivity() {
    private val TAG = "VisualAdjustmentActivit"
    private lateinit var view: VisualAdjustmentView // onCreateで初期化

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = VisualAdjustmentView(MyContexts.applicationContext)
        view.setEGLContextClientVersion(2)
        val uri = intent.getStringExtra("uri")
        view.setRenderer(VisualAdjustmentRenderer(Uri.parse(uri)))

        setContentView(view)

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    public override fun onPause() {
        Log.v(TAG, "onPause")
        super.onPause()
        view.onPause()
    }

    public override fun onResume() {
        Log.v(TAG, "onResume")
        super.onResume()
        view.onResume()
    }
}
