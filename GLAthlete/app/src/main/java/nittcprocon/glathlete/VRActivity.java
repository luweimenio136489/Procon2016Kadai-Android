package nittcprocon.glathlete;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;

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
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;

import static java.lang.Math.PI;

/**
 * Google VR SDKを使って実際の描画を行う
 * TODO: カリング
 * FIXME: テクスチャユニットをTEXTURE0しか使わない前提になってる
 */

public class VRActivity extends GvrActivity implements GvrView.StereoRenderer, KeyEvent.Callback {
    /*
     * グローバル人材
     */
    private static final String TAG = "VRActivity";
    private static final float CAMERA_Z = 0.01f, Z_NEAR = 0.1f, Z_FAR = 100.0f; // ？
    private float[] modelMat, viewMat, camera;      // 変換行列
    private float[] fLen, rLen, fCenter, rCenter;   // シェーダーに渡すマッピング定数
    private int texture;
    private boolean shadersReady = false;
    private ModelBuffer frontBuffer, sideBuffer, rearBuffer;
    private ShaderProgram frontShader, sideShader, rearShader;
    private String uri;
    private SurfaceTexture surfaceTexture;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        initializeGvrView();

        camera = new float[16];
        modelMat = new float[16];
        viewMat = new float[16];

        Intent intent = getIntent();
        uri = intent.getStringExtra("uri");
        Log.d(TAG, "URI: " + uri);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadSharedPreferences(sharedPreferences);
    }

    // 助けて
    @Override
    public boolean onKeyUp(int keycode, KeyEvent event) {
        switch (keycode) {
            // fCenter
            // fCenter.u
            case KeyEvent.KEYCODE_Q: // up
                fCenter[0] = Math.min(1.0f, fCenter[0] + 0.01f);
                Log.d(TAG, "fCenter.u -> " + fCenter[0]);
                break;
            case KeyEvent.KEYCODE_A: // down
                fCenter[0] = Math.max(0.0f, fCenter[0] - 0.01f);
                Log.d(TAG, "fCenter.u -> " + fCenter[0]);
                break;
            // fCenter.v
            case KeyEvent.KEYCODE_W: // up
                fCenter[1] = Math.min(1.0f, fCenter[1] + 0.01f);
                Log.d(TAG, "fCenter.v -> " + fCenter[1]);
                break;
            case KeyEvent.KEYCODE_S: // down
                fCenter[1] = Math.max(0.0f, fCenter[1] - 0.01f);
                Log.d(TAG, "fCenter.v -> " + fCenter[1]);
                break;

            // rCenter
            // rCenter.u
            case KeyEvent.KEYCODE_E: // up
                rCenter[0] = Math.min(1.0f, rCenter[0] + 0.01f);
                Log.d(TAG, "rCenter.u -> " + rCenter[0]);
                break;
            case KeyEvent.KEYCODE_D: // down
                rCenter[0] = Math.max(0.0f, rCenter[0] - 0.01f);
                Log.d(TAG, "rCenter.u -> " + rCenter[0]);
                break;
            // rCenter.v
            case KeyEvent.KEYCODE_R: // up
                rCenter[1] = Math.min(1.0f, rCenter[1] + 0.01f);
                Log.d(TAG, "rCenter.v -> " + rCenter[1]);
                break;
            case KeyEvent.KEYCODE_F: // down
                rCenter[1] = Math.max(0.0f, rCenter[1] - 0.01f);
                Log.d(TAG, "rCenter.v -> " + rCenter[1]);
                break;

            // fLen
            // fLen.u
            case KeyEvent.KEYCODE_T: // up
                fLen[0] = Math.min(1.0f, fLen[0] + 0.01f);
                Log.d(TAG, "fLen.u -> " + fLen[0]);
                break;
            case KeyEvent.KEYCODE_G: // down
                fLen[0] = Math.max(0.0f, fLen[0] - 0.01f);
                Log.d(TAG, "fLen.u -> " + fLen[0]);
                break;
            // fLen.v
            case KeyEvent.KEYCODE_Y: // up
                fLen[1] = Math.min(1.0f, fLen[1] + 0.01f);
                Log.d(TAG, "fLen.v -> " + fLen[1]);
                break;
            case KeyEvent.KEYCODE_H: // down
                fLen[1] = Math.max(0.0f, fLen[1] - 0.01f);
                Log.d(TAG, "fLen.v -> " + fLen[1]);
                break;

            // rLen
            // rLen.u
            case KeyEvent.KEYCODE_U: // up
                rLen[0] = Math.min(1.0f, rLen[0] + 0.01f);
                Log.d(TAG, "rLen.u -> " + rLen[0]);
                break;
            case KeyEvent.KEYCODE_J: // down
                rLen[0] = Math.max(0.0f, rLen[0] - 0.01f);
                Log.d(TAG, "rLen.u -> " + rLen[0]);
                break;
            // rLen.v
            case KeyEvent.KEYCODE_I: // up
                rLen[1] = Math.min(1.0f, rLen[1] + 0.01f);
                Log.d(TAG, "rLen.v -> " + rLen[1]);
                break;
            case KeyEvent.KEYCODE_K: // down
                rLen[1] = Math.max(0.0f, rLen[1] - 0.01f);
                Log.d(TAG, "rLen.v -> " + rLen[1]);
                break;

            default:
                return super.onKeyUp(keycode, event);
        }
        setShaderParams();
        return true;
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        /* モデルの生成 */
        Model3D frontModel, sideModel, rearModel;
        frontModel = new PartialSphereModelCreator(0.0f, (float)(0.4 * PI)).getPartialSphereModel();
        sideModel = new PartialSphereModelCreator((float)(0.4 * PI), (float)(0.6 * PI)).getPartialSphereModel();
        rearModel = new PartialSphereModelCreator((float)(0.6 * PI), (float)(PI)).getPartialSphereModel();
        checkGLError("Model Generation");
        Log.d(TAG, "Generated models");

        /* シェーダーのコンパイルとリンク */
        frontShader = new ShaderProgram(readRawTextFile(R.raw.v_front), readRawTextFile(R.raw.f_frontrear));
        sideShader = new ShaderProgram(readRawTextFile(R.raw.v_side), readRawTextFile(R.raw.f_side));
        rearShader = new ShaderProgram(readRawTextFile(R.raw.v_rear), readRawTextFile(R.raw.f_frontrear));
        checkGLError("Shader Compilation");
        Log.d(TAG, "Compiled GL shaders");
        shadersReady = true;

        /* 定数 */
        setShaderParams();

        /* バッファオブジェクト */
        frontBuffer = new ModelBuffer(frontModel);
        sideBuffer = new ModelBuffer(sideModel);
        rearBuffer = new ModelBuffer(rearModel);
        checkGLError("Creating buffer objects");
        Log.d(TAG, "Created buffer objects");

        texture = createTexture();
        surfaceTexture = new SurfaceTexture(texture);

        /* 変換行列の設定 */
        // TODO: 回転機能
        Matrix.setIdentityM(modelMat, 0);

        startPlayback();
    }

    private void setShaderParams() {
        if (!shadersReady) {
            Log.d(TAG, "setShaderParams called before shaders are ready");
            return;
        }

        GLES20.glUseProgram(frontShader.getProgram());
        GLES20.glUniform2fv(frontShader.getLocationOf("fCenter"), 1, fCenter, 0);
        GLES20.glUniform2fv(frontShader.getLocationOf("fLen"), 1, fLen, 0);

        GLES20.glUseProgram(sideShader.getProgram());
        GLES20.glUniform2fv(sideShader.getLocationOf("fCenter"), 1, fCenter, 0);
        GLES20.glUniform2fv(sideShader.getLocationOf("fLen"), 1, fLen, 0);
        GLES20.glUniform2fv(sideShader.getLocationOf("rCenter"), 1, rCenter, 0);
        GLES20.glUniform2fv(sideShader.getLocationOf("rLen"), 1, rLen, 0);

        GLES20.glUseProgram(rearShader.getProgram());
        GLES20.glUniform2fv(rearShader.getLocationOf("rCenter"), 1, rCenter, 0);
        GLES20.glUniform2fv(rearShader.getLocationOf("rLen"), 1, rLen, 0);

        checkGLError("Parameter Setting");
        Log.d(TAG, "Set GL parameters");
    }

    private void loadSharedPreferences(SharedPreferences p) {
        float fCenter_u = p.getFloat("front_center_u", 0.25f);
        float fCenter_v = p.getFloat("front_center_v", 0.4444f);
        fCenter = new float[] {fCenter_u, fCenter_v};

        float rCenter_u = p.getFloat("rear_center_u", 0.75f);
        float rCenter_v = p.getFloat("rear_center_v", 0.4444f);
        rCenter = new float[] {rCenter_u, rCenter_v};

        float fLen_u = p.getFloat("front_length_u", (float)(0.25 * 0.9));
        float fLen_v = p.getFloat("front_length_v", (float)(0.4444 * 0.9));
        fLen = new float[] {fLen_u, fLen_v};

        float rLen_u = p.getFloat("rear_length_u", (float)(0.25 * 0.9));
        float rLen_v = p.getFloat("rear_length_v", (float)(0.4444 * 0.9));
        rLen = new float[] {rLen_u, rLen_v};
    }

    public void startPlayback() {
        Surface surface = new Surface(surfaceTexture);
        try {
            Log.d(TAG, "creating MediaPlayer");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(uri));
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);

            mediaPlayer.prepareAsync();

            //mediaPlayer.setOnBufferingUpdateListener(this);
            //mediaPlayer.setOnCompletionListener(this);
            //mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    Log.d(TAG, "mediaPlayer.start()");
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
        }
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

    /*
     * コールバック(GvrView.StereoRenderer)
     */

    /* フレームの描画前にOpenGL ESの準備をする */
    /* FIXME: なんか失敗してる？(glError 1282: GL_INVALID_OPERATION) */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // 出力先, オフセット, 視点のx, y, z, 視点の中心のx, y, z, 上向きベクトルのx, y, z
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        surfaceTexture.updateTexImage();

        /* テクスチャバッファの変換行列 */
        float[] stTransform = new float[16];
        surfaceTexture.getTransformMatrix(stTransform);
        for (ShaderProgram shader : new ShaderProgram[] {frontShader, sideShader, rearShader}) {
            GLES20.glUseProgram(shader.getProgram());
            GLES20.glUniformMatrix4fv(shader.getLocationOf("stTransform"), 1, false, stTransform, 0);
        }

        checkGLError("onNewFrame");
    }

    /* 与えられたEyeに対してフレームを描画する */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(viewMat, 0, eye.getEyeView(), 0, camera, 0);
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        float[] mvpMat = calcMVP(modelMat, viewMat, perspective);

        Map<ShaderProgram, ModelBuffer> task = new HashMap<ShaderProgram, ModelBuffer>() {
            {
                put(frontShader, frontBuffer);
                put(sideShader, sideBuffer);
                put(rearShader, rearBuffer);
            }
        };

        for (Map.Entry<ShaderProgram, ModelBuffer> t : task.entrySet()) {
            ShaderProgram shader = t.getKey();
            ModelBuffer buffer = t.getValue();
            int vbo = buffer.getVbo();
            int ibo = buffer.getIbo();

            GLES20.glUseProgram(shader.getProgram());

            GLES20.glUniformMatrix4fv(shader.getLocationOf("mvpMat"), 1, false, mvpMat, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glEnableVertexAttribArray(vbo);
            GLES20.glVertexAttribPointer(shader.getLocationOf("position"), 3, GLES20.GL_FLOAT, false, 0, 0);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, buffer.getIndicesCount(), GLES20.GL_UNSIGNED_SHORT, 0);

            checkGLError("Drawing sphere");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
            GLES20.glUniform1i(shader.getLocationOf("texture"), 0);

            // unbind
            //GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            //GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
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

    private int createTexture() {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);

        checkGLError("createTexture");

        return textureId;
    }

    /* OpenGL ESの内部でエラーがないかチェックし、あったら例外を投げる */
    private static void checkGLError(String label) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlayer.stop();
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
        mediaPlayer.stop();
    }

    /*
     * ここから下はどうでもいいやつ
     */
    @Override
    public void onStart() {
        super.onStart();
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
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");
    }
}
