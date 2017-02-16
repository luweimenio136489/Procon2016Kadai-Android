package nittcprocon.glathlete;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static nittcprocon.glathlete.UtilsDroid.*;
import static nittcprocon.glathlete.UtilsGL.*;
import static nittcprocon.glathlete.UtilsMisc.*;

class VisualAdjustmentRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "VisualAdjustmentRendere";
    private Context context;
    private ShaderProgram quadShader;
    private RenderingTask[] tasks;
    private int texture;
    private SurfaceTexture surfaceTexture;
    private boolean mediaPlayerPrepared = false;
    private float[] fCenter, rCenter, fLen, rLen;
    private MediaPlayer mediaPlayer;
    private Uri uri;

    VisualAdjustmentRenderer(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
        fCenter = new float[2];
        rCenter = new float[2];
        fLen = new float[2];
        rLen = new float[2];

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        loadSharedPreferences(sharedPreferences, fCenter, rCenter, fLen, rLen);
    }

    VisualAdjustmentRenderer(VisualAdjustmentView view) {

    }

    //region GLSurfaceView.Renderer
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.v(TAG, "onSurfaceCreated");

        // 背景色を設定
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 0.5f);

        final float VIDEO_DEPTH = 0.0f;

        quadShader = new ShaderProgram(readRawTextFile(context, R.raw.v_quad), readRawTextFile(context, R.raw.f_quad));
        Model videoModel = new QuadModel(new Types.Quad(
                new Types.Vec3f(1.0f, -1.0f, VIDEO_DEPTH), new Types.Vec3f(-1.0f, -1.0f, VIDEO_DEPTH),
                new Types.Vec3f(1.0f, 1.0f, VIDEO_DEPTH), new Types.Vec3f(-1.0f, 1.0f, VIDEO_DEPTH)
        ));

        final float BLEND_DEPTH = 0.0f, BLEND_HEIGHT = 1.0f / 16.0f, BLEND_Y1 = 1.0f - BLEND_HEIGHT;
        Model blendModel = new IndexedModel().addQuad(new Types.Quad(
                new Types.Vec3f(-1.0f, BLEND_Y1, BLEND_DEPTH), new Types.Vec3f(1.0f, BLEND_Y1, BLEND_DEPTH),
                new Types.Vec3f(1.0f, -1.0f, BLEND_DEPTH), new Types.Vec3f(-1.0f, -1.0f, BLEND_DEPTH)
        ));

        tasks = new RenderingTask[] {
                new RenderingTask(videoModel, quadShader)
        };

        texture = createTexture();
        surfaceTexture = new SurfaceTexture(texture);

        startPlayback();
    }

    public void onDrawFrame(GL10 unused) {
        if (!mediaPlayerPrepared)
            return;

        // 背景色を再描画
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        surfaceTexture.updateTexImage();
        float[] stTransform = new float[16];
        surfaceTexture.getTransformMatrix(stTransform);
        quadShader.useProgram();
        quadShader.uniformMatrix4fv("stTransform", 1, false, stTransform, 0);

        for (RenderingTask task : tasks) {
            ShaderProgram shader = task.shader();
            shader.useProgram();
            shader.ifExistsUniform2fv("fCenter", 1, fCenter, 0);
            shader.ifExistsUniform2fv("rCenter", 1, rCenter, 0);
            shader.ifExistsUniform2fv("fLen", 1, fLen, 0);
            shader.ifExistsUniform2fv("rLen", 1, rLen, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
            shader.uniform1i("texture", 0);

            task.render();

            checkGLError("Drawing poly");
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
    //endregion
    //region 細かい処理
    private void startPlayback() {
        Surface surface = new Surface(surfaceTexture);
        try {
            Log.d(TAG, "creating MediaPlayer");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayerPrepared = true;
                    mediaPlayer.start();
                    Log.d(TAG, "mediaPlayer.start()");
                }
            });

            mediaPlayer.prepareAsync();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    //endregion
}
