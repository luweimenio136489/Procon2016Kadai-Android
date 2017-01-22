package nittcprocon.glathlete;

import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import static nittcprocon.glathlete.Types.*;
import static nittcprocon.glathlete.UtilsMisc.*;
import static nittcprocon.glathlete.UtilsGL.*;
import static nittcprocon.glathlete.UtilsDroid.*;

/**
 * ビジュアル調整
 * ビデオを再生する板ポリと、その上でパラメータを表示するモデル、ブレンディング部分を表示する板ポリ
 */

public class VisualAdjustmentActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private static final String TAG = "VisualAdjustmentActivit";
    private GLSurfaceView view;
    private MediaPlayer mediaPlayer;
    private SurfaceTexture surfaceTexture;
    private ModelBuffer videoMBuffer, blendMBuffer;
    private ShaderProgram quadShader;
    private SharedPreferences sharedPreferences;
    private float[] fCenter, rCenter, fLen, rLen;
    private int texture;
    //region AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_adjustment);

        view = (GLSurfaceView) findViewById(R.id.visualAdjustmentGLSurfaceView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        view.setEGLContextClientVersion(2);
        view.setRenderer(this);

        fCenter = new float[2];
        rCenter = new float[2];
        fLen = new float[2];
        rLen = new float[2];

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadSharedPreferences(sharedPreferences, fCenter, rCenter, fLen, rLen);
    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop");
        super.onStop();
        mediaPlayer.stop();
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
    //region GLSurfaceView.Renderer
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.v(TAG, "onSurfaceCreated");
        // 背景色を設定
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);

        final float VIDEO_DEPTH = -0.1f;/*
        Model3D videoModel = new Model3D().addQuad(new Quad(
                new Vec3f(-1.0f, -1.0f, VIDEO_DEPTH), new Vec3f(1.0f, -1.0f, VIDEO_DEPTH),
                new Vec3f(1.0f, 1.0f, VIDEO_DEPTH), new Vec3f(-1.0f, 1.0f, VIDEO_DEPTH)
        ));*/
        Model3D videoModel = new Model3D().addQuad(new Quad(
                new Vec3f(1.0f, -1.0f, VIDEO_DEPTH), new Vec3f(-1.0f, -1.0f, VIDEO_DEPTH),
                new Vec3f(-1.0f, 1.0f, VIDEO_DEPTH), new Vec3f(1.0f, 1.0f, VIDEO_DEPTH)
        ));
        videoMBuffer = new ModelBuffer(videoModel);

        final float BLEND_DEPTH = 0.0f, BLEND_HEIGHT = 1.0f / 16.0f, BLEND_Y1 = 1.0f - BLEND_HEIGHT;
        Model3D blendModel = new Model3D().addQuad(new Quad(
                new Vec3f(-1.0f, BLEND_Y1, BLEND_DEPTH), new Vec3f(1.0f, BLEND_Y1, BLEND_DEPTH),
                new Vec3f(1.0f, -1.0f, BLEND_DEPTH), new Vec3f(-1.0f, -1.0f, BLEND_DEPTH)
        ));
        blendMBuffer = new ModelBuffer(blendModel);

        quadShader = new ShaderProgram(readRawTextFile(this, R.raw.v_quad), readRawTextFile(this, R.raw.f_quad));

        texture = createTexture();
        surfaceTexture = new SurfaceTexture(texture);

        startPlayback();
    }

    public void onDrawFrame(GL10 unused) {
        // 背景色を再描画
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        surfaceTexture.updateTexImage();
        float[] stTransform = new float[16];
        surfaceTexture.getTransformMatrix(stTransform);
        quadShader.useProgram();
        quadShader.uniformMatrix4fv("stTransform", 1, false, stTransform, 0);

        for (ShaderProgram shader : new ShaderProgram[] {quadShader}) {
            shader.useProgram();
            shader.ifExistsUniform2fv("fCenter", 1, fCenter, 0);
            shader.ifExistsUniform2fv("rCenter", 1, rCenter, 0);
            shader.ifExistsUniform2fv("fLen", 1, fLen, 0);
            shader.ifExistsUniform2fv("rLen", 1, rLen, 0);
        }

        Map<ShaderProgram, ModelBuffer> task = new HashMap<ShaderProgram, ModelBuffer>() {
            {
                put(quadShader, videoMBuffer);
            }
        };

        for (Map.Entry<ShaderProgram, ModelBuffer> t : task.entrySet()) {
            ShaderProgram shader = t.getKey();
            ModelBuffer buffer = t.getValue();
            int vbo = buffer.getVbo();
            int ibo = buffer.getIbo();
            int indices = buffer.getIndicesCount();

            shader.useProgram();

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glEnableVertexAttribArray(vbo);
            GLES20.glVertexAttribPointer(shader.getLocationOf("position"), 3, GLES20.GL_FLOAT, false, 0, 0);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices, GLES20.GL_UNSIGNED_SHORT, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
            shader.uniform1i("texture", 0);

            checkGLError("Drawing poly");
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
    //endregion
    //region 細かい処理
    public void startPlayback() {
        Surface surface = new Surface(surfaceTexture);
        try {
            String uri = getIntent().getStringExtra("uri");
            Log.d(TAG, "creating MediaPlayer");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(uri));
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);

            mediaPlayer.prepareAsync();

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
    //endregion
}
