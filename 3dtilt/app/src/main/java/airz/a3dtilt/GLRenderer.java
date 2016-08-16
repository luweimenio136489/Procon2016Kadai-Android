package airz.a3dtilt;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import gl3dlibs.GLES;
import gl3dlibs.ObjLoader;
import gl3dlibs.Object3D;

/**
 * Created by ke on 2016/08/16.
 */
//レンダラー
public class GLRenderer implements
        GLSurfaceView.Renderer {
    //システム
    private float aspect;//アスペクト比
    private int angle; //回転角度

    //モデル
    private Object3D model = new Object3D();

    //コンストラクタ
    public GLRenderer(Context context) {
        GLES.context = context;
    }

    //サーフェイス生成時に呼ばれる
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES.gl = (GL11) gl10;

        //デプステストの有効化
        gl10.glEnable(GL10.GL_DEPTH_TEST);

        //光源色の有効化
        gl10.glEnable(GL10.GL_LIGHTING);
        gl10.glEnable(GL10.GL_LIGHT0);

        //光源色の指定
        gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT,
                new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
        gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE,
                new float[]{0.7f, 0.7f, 0.7f, 1.0f}, 0);
        gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR,
                new float[]{0.9f, 0.9f, 0.9f, 1.0f}, 0);

        //モデルの読み込み
        try {
            model.figure = ObjLoader.load("droid.obj");
        } catch (Exception e) {
            android.util.Log.e("debug", e.toString());
            for (StackTraceElement ste : e.getStackTrace()) {
                android.util.Log.e("debug", "    " + ste);
            }
        }
    }

    //画面サイズ変更時に呼ばれる
    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        //ビューポート変換
        gl10.glViewport(0, 0, w, h);
        aspect = (float) w / (float) h;
    }

    //毎フレーム描画時に呼ばれる
    @Override
    public void onDrawFrame(GL10 gl10) {
        //画面のクリア
        gl10.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT |
                GL10.GL_DEPTH_BUFFER_BIT);

        //射影変換
        gl10.glMatrixMode(GL10.GL_PROJECTION);
        gl10.glLoadIdentity();
        GLU.gluPerspective(gl10,
                45.0f,  //Y方向の画角
                aspect, //アスペクト比
                0.01f,  //ニアクリップ
                100.0f);//ファークリップ

        //光源位置の指定
        gl10.glMatrixMode(GL10.GL_MODELVIEW);
        gl10.glLoadIdentity();
        gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION,
                new float[]{5.0f, 5.0f, 5.0f, 0.0f}, 0);

        //ビュー変換
        GLU.gluLookAt(gl10,
                0, 0.8f, 5.0f,    //カメラの視点
                0, 0.8f, 0.0f,    //カメラの焦点
                0.0f, 1.0f, 0.0f);//カメラの上方向

        //モデル変換
        gl10.glRotatef((float) MainActivity.pitch, 1, 0, 0);//x
        gl10.glRotatef(-(float) MainActivity.roll, 0, 1, 0);//y
//        gl10.glRotatef((float) MainActivity.azimuth, 0, 0, 1);//z

        //モデルの描画
        model.draw();
    }
}