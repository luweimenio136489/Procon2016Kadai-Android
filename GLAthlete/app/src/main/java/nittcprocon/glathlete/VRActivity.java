package nittcprocon.glathlete;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Google VR SDKを使って実際の描画を行う
 * TODO: カリング
 */

public class VRActivity extends GvrActivity implements GvrView.StereoRenderer {
    /*
     * グローバル人材
     */
    private static final String TAG = "VRActivity";
    private SphereModel sphereModel;
    private static final float CAMERA_Z = 0.01f, Z_NEAR = 0.1f, Z_FAR = 100.0f; // ？
    private float[] modelMat, viewMat, camera, headView;    // 変換行列
    private int vbo, ibo, texture;                          // バッファオブジェクト
    private int program;                                    // シェーダー
    private int positionLoc, mvpLoc, textureLoc;            // シェーダーパラメータ
    private Uri uri;                                        // RTSPストリームのURI

    /*
     * 初期化
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeGvrView();

        camera = new float[16];
        modelMat = new float[16];
        viewMat = new float[16];
        headView = new float[16];

        Intent intent = getIntent();
        uri = (Uri)intent.getSerializableExtra("uri");
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        /* モデルの生成 */
        sphereModel = new SphereModel();
        sphereModel.createBuffer();
        Log.d(TAG, "Generated model, vertices: " + sphereModel.vertNum());

        /* シェーダーのコンパイルとリンク */
        int vShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vshader);
        int fShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fshader);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShader);
        GLES20.glAttachShader(program, fShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        
        checkGLError("Sphere program");

        /* シェーダーに渡すものたち */
        positionLoc = GLES20.glGetAttribLocation(program, "position");
        mvpLoc = GLES20.glGetUniformLocation(program, "mvpMat");

        textureLoc = GLES20.glGetUniformLocation(program, "texture");

        /* バッファオブジェクト */
        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);
        vbo = buffers[0];
        ibo = buffers[1];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Float.SIZE * sphereModel.vertNum(), sphereModel.getVertices(), GLES20.GL_STATIC_DRAW);
        checkGLError("Setting vbo");
        Log.d(TAG, "vbo: " + vbo + ", size: " + sphereModel.vertNum());

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, Short.SIZE * sphereModel.indNum(), sphereModel.getIndices(), GLES20.GL_STATIC_DRAW);
        checkGLError("Setting ibo");
        Log.d(TAG, "ibo: " + ibo + ", size: " + sphereModel.indNum());

        Resources resources = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(resources, R.raw.texture);
        texture = loadTexture(bmp);

        /* 変換行列の設定 */
        Matrix.setIdentityM(modelMat, 0);

        checkGLError("onSurfaceCreated");
    }

    /*
     * コールバック
     */
    /* フレームの描画前にOpenGL ESの準備をする */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // 出力先, オフセット, 視点のx, y, z, 視点の中心のx, y, z, 上向きベクトルのx, y, z
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        headTransform.getHeadView(headView, 0); // これはなんだろう
        checkGLError("onNewFrame");
    }

    /* 与えられたEyeに対してフレームを描画する */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(viewMat, 0, eye.getEyeView(), 0, camera, 0);
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        float[] mvpMat = calcMVP(modelMat, viewMat, perspective);

        GLES20.glUseProgram(program);

        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMat, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(vbo);
        GLES20.glVertexAttribPointer(positionLoc, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, sphereModel.indNum(), GLES20.GL_UNSIGNED_SHORT, 0);

        checkGLError("Drawing sphere");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(textureLoc, 0);

        // unbind
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        checkGLError("onDrawEye");
    }

    /*
     * 便利なメソッドたち
     */
    /* 3つの行列を一度に掛け算して返す */
    private float[] calcMVP(float[] m, float[] v, float[] p) {
        float[] mv = new float[16];
        Matrix.multiplyMM(mv, 0, v, 0, m, 0);
        float[] mvp = new float[16];
        Matrix.multiplyMM(mvp, 0, p, 0, mv, 0);
        return mvp;
    }

    public void initializeGvrView() {
        setContentView(R.layout.activity_vr);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);
        gvrView.setOnCardboardBackButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        onBackPressed();
                    }
                }
        );

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
    }

    /* 生リソースをテキストとして読み、Stringで返す */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* 生リソースをシェーダーとして読み込む */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /* テクスチャを読み込む */
    private int loadTexture(Bitmap bmp) {
        //テクスチャメモリの確保
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];

        //テクスチャへのビットマップ指定
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        //テクスチャフィルタの指定
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);
        return textureId;
    }

    /* OpenGL ESの内部でエラーがないかチェックし、あったら例外を投げる */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /*
     * ここから下はどうでもいいやつ
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }
}
