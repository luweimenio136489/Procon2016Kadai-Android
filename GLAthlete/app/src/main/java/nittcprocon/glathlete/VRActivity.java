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

import javax.microedition.khronos.egl.EGLConfig;

import static java.lang.Math.PI;
import static java.lang.Math.max;

import static nittcprocon.glathlete.UtilsDroid.*;
import static nittcprocon.glathlete.UtilsGL.*;
import static nittcprocon.glathlete.UtilsMisc.*;
import static nittcprocon.glathlete.Types.*;

/**
 * Google VR SDKを使って実際の描画を行う
 * TODO: カリング
 * FIXME: テクスチャユニットをTEXTURE0しか使わない前提になってる
 */

public class VRActivity extends GvrActivity implements GvrView.StereoRenderer, KeyEvent.Callback {
    //region グローバル人材
    private static final String TAG = "VRActivity";
    private static final float CAMERA_Z = 0.01f, Z_NEAR = 0.1f, Z_FAR = 100.0f; // ？
    private float[] modelMat, viewMat, camera;      // 変換行列 (mat4)
    private float[] fCenter, rCenter, fLen, rLen;   // シェーダーに渡すマッピング定数 (vec2)
    private int texture;
    private RenderingTask[] tasks;
    private String uri;
    private SurfaceTexture surfaceTexture;
    private MediaPlayer mediaPlayer;
    private GvrView gvrView;
    private SharedPreferences sharedPreferences;
    //endregion
    //region GvrActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        initializeGvrView();

        camera = new float[16];
        modelMat = new float[16];
        viewMat = new float[16];

        fCenter = new float[2];
        rCenter = new float[2];
        fLen = new float[2];
        rLen = new float[2];

        Intent intent = getIntent();
        uri = intent.getStringExtra("uri");
        Log.d(TAG, "URI: " + uri);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadSharedPreferences(sharedPreferences, fCenter, rCenter, fLen, rLen);
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        final float fAngle = (float) (0.4 * PI), rAngle = (float) (0.6 * PI);

        Model frontModel, frontSideModel, rearSideModel, rearModel;
        frontModel = new IndexedModel();
        frontSideModel = new IndexedModel();
        rearSideModel = new IndexedModel();
        rearModel = new IndexedModel();

        generatePartialSphereModel(frontModel, 0.0f, fAngle);
        generatePartialSphereModel(frontSideModel, fAngle, (float)(0.5 * PI));
        generatePartialSphereModel(rearSideModel, (float)(0.5 * PI), rAngle);
        generatePartialSphereModel(rearModel, rAngle, (float)(PI));

        ShaderProgram frontShader, frontSideShader, rearSideShader, rearShader;
        frontShader = new ShaderProgram(readRawTextFile(this, R.raw.v_front), readRawTextFile(this, R.raw.f_frontrear));
        frontSideShader = new ShaderProgram(readRawTextFile(this, R.raw.v_frontside), readRawTextFile(this, R.raw.f_frontside));
        rearSideShader = new ShaderProgram(readRawTextFile(this, R.raw.v_rearside), readRawTextFile(this, R.raw.f_rearside));
        rearShader = new ShaderProgram(readRawTextFile(this, R.raw.v_rear), readRawTextFile(this, R.raw.f_frontrear));

        tasks = new RenderingTask[] {
                new RenderingTask(frontModel, frontShader),
                new RenderingTask(frontSideModel, frontSideShader),
                new RenderingTask(rearSideModel, rearSideShader),
                new RenderingTask(rearModel, rearShader)
        };

        setShaderParams();

        texture = createTexture();
        surfaceTexture = new SurfaceTexture(texture);

        /* 変換行列の設定 */
        // TODO: 回転機能
        Matrix.setIdentityM(modelMat, 0);

        startPlayback();
    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop");
        super.onStop();

        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }
    //endregion
    //region GvrView.StereoRenderer
    /* フレームの描画前にOpenGL ESの準備をする */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // 出力先, オフセット, 視点のx, y, z, 視点の中心のx, y, z, 上向きベクトルのx, y, z
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        surfaceTexture.updateTexImage();

        /* テクスチャバッファの変換行列 */
        float[] stTransform = new float[16];
        surfaceTexture.getTransformMatrix(stTransform);

        for (RenderingTask task : tasks) {
            task.shader().useProgram();
            task.shader().uniformMatrix4fv("stTransform", 1, false, stTransform, 0);
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

        for (RenderingTask task : tasks) {
            task.shader().useProgram();
            task.shader().uniformMatrix4fv("mvpMat", 1, false, mvpMat, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
            task.shader().uniform1i("texture", 0);

            task.render();

            // unbind
            //GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            //GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        checkGLError("onDrawEye");
    }

    @Override
    public void onRendererShutdown() {
        Log.v(TAG, "onRendererShutdown");
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.v(TAG, "onSurfaceChanged");
    }
    //endregion
    //region KeyEvent.Callback
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

            case KeyEvent.KEYCODE_C:
                setSharedPreferences(sharedPreferences, fCenter, rCenter, fLen, rLen);
                Log.d(TAG, "設定を保存");
                break;

            case KeyEvent.KEYCODE_Z:
                mediaPlayer.pause();
                return true;
            case KeyEvent.KEYCODE_X:
                mediaPlayer.start();
                return true;

            default:
                return super.onKeyUp(keycode, event);
        }

        // GLのメソッドはGLのスレッドから呼ぶ必要がある
        gvrView.queueEvent(new Runnable() {
            @Override
            public void run() {
                setShaderParams();
            }
        });

        return true;
    }
    //endregion
    //region 細かい処理
    private void setShaderParams() {
        Log.d(TAG, "setShaderParams: setting ");
        Log.d(TAG, "fCenter: " + dump2fv(fCenter) + ", rCenter: " + dump2fv(rCenter));
        Log.d(TAG, "fLen: " + dump2fv(fLen) + ", rLen: " + dump2fv(rLen));

        for (RenderingTask task : tasks) {
            ShaderProgram shader = task.shader();
            shader.useProgram();
            shader.ifExistsUniform2fv("fCenter", 1, fCenter, 0);
            shader.ifExistsUniform2fv("rCenter", 1, rCenter, 0);
            shader.ifExistsUniform2fv("fLen", 1, fLen, 0);
            shader.ifExistsUniform2fv("rLen", 1, rLen, 0);
        }

        checkGLError("Parameter Setting");
        Log.d(TAG, "Set GL parameters");
    }

    public void startPlayback() {
        Surface surface = new Surface(surfaceTexture);
        try {
            Log.d(TAG, "creating MediaPlayer");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(uri));
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);

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
            mediaPlayer.prepareAsync();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeGvrView() {
        setContentView(R.layout.activity_vr);

        gvrView = (GvrView) findViewById(R.id.gvr_view);
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

    private void generatePartialSphereModel(Model model, float thetaBegin, float thetaEnd) {
        final float RADIUS = 1.0f;
        final float STACKS_PER_RADIANS = (float)(24.0 / PI);
        final int STACKS = (int)max(1.0, STACKS_PER_RADIANS * (thetaEnd - thetaBegin));
        final int SLICES = 24;

        generatePartialSphereModel(model, thetaBegin, thetaEnd, RADIUS, STACKS, SLICES);
    }

    private void generatePartialSphereModel(Model model, float thetaBegin, float thetaEnd, float radius, int stacks, int slices) {
        float theta = thetaEnd - thetaBegin;
        for (int t = 0; t < stacks; t++) {
            float theta1 = thetaBegin + theta * t / stacks;
            float theta2 = thetaBegin + theta * (t + 1) / stacks;
            for (int p = 0; p < slices; p++) {
                float phi1 = (float)PI * 2.0f * p / slices;
                float phi2 = (float)PI * 2.0f * (p + 1) / slices;

                Vec3f v1 = rtp2xyz(radius, theta1, phi1);
                Vec3f v2 = rtp2xyz(radius, theta1, phi2);
                Vec3f v3 = rtp2xyz(radius, theta2, phi2);
                Vec3f v4 = rtp2xyz(radius, theta2, phi1);

                if (theta1 == 0.0f) {                           // top/front cap, v1 == v2
                    model.addTri(new Tri(v2, v3, v4));
                } else if (nearlyEquals(theta2, (float)PI)) {   // bottom/rear cap, v3 == v4
                    model.addTri(new Tri(v1, v2, v3));
                } else {
                    model.addQuad(new Quad(v1, v2, v3, v4));
                }
            }
        }
    }
    //endregion
}
