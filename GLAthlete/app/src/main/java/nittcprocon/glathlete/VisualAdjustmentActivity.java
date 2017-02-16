package nittcprocon.glathlete;

import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

/**
 * ビジュアル調整
 * ビデオを再生する板ポリと、その上でパラメータを表示するモデル、ブレンディング部分を表示する板ポリ
 */

public class VisualAdjustmentActivity extends AppCompatActivity {
    private static final String TAG = "VisualAdjustmentActivit";
    private GLSurfaceView view;

    //region AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_adjustment);

        view = (GLSurfaceView) findViewById(R.id.visualAdjustmentGLSurfaceView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        view.setEGLContextClientVersion(2);

        String uri = getIntent().getStringExtra("uri");
        view.setRenderer(new VisualAdjustmentRenderer(getApplicationContext(), Uri.parse(uri)));
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
        view.onPause();
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        view.onResume();
    }
    //endregion
}
