package nittcprocon.glathlete

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.preference.PreferenceManager
import android.util.Log
import android.view.Surface

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import nittcprocon.glathlete.UtilsDroid.readRawTextFile
import nittcprocon.glathlete.UtilsGL.checkGLError
import nittcprocon.glathlete.UtilsGL.createTexture
import nittcprocon.glathlete.UtilsMisc.loadSharedPreferences

internal class VisualAdjustmentRenderer(private val uri: Uri) : GLSurfaceView.Renderer {
    private val TAG = "VisualAdjustmentRendere"
    private lateinit var quadShader: ShaderProgram
    private var tasks: Array<RenderingTask>? = null
    private var texture: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var mediaPlayerPrepared = false
    private val fCenter = FloatArray(2)
    private val rCenter = FloatArray(2)
    private val fLen    = FloatArray(2)
    private val rLen    = FloatArray(2)
    private var mediaPlayer: MediaPlayer? = null

    init {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyContexts.applicationContext)
        loadSharedPreferences(sharedPreferences, fCenter, rCenter, fLen, rLen)
    }

    //region GLSurfaceView.Renderer
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        Log.v(TAG, "onSurfaceCreated")

        // 背景色を設定
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 0.5f)

        val VIDEO_DEPTH = 0.0f

        quadShader = ShaderProgram(R.raw.v_quad, R.raw.f_quad)
        val videoModel = QuadModel(Types.Quad(
                Types.Vec3f(1.0f, -1.0f, VIDEO_DEPTH), Types.Vec3f(-1.0f, -1.0f, VIDEO_DEPTH),
                Types.Vec3f(1.0f, 1.0f, VIDEO_DEPTH), Types.Vec3f(-1.0f, 1.0f, VIDEO_DEPTH)
        ))

        val BLEND_DEPTH = 0.0f
        val BLEND_HEIGHT = 1.0f / 16.0f
        val BLEND_Y1 = 1.0f - BLEND_HEIGHT
        val blendModel = IndexedModel().addQuad(Types.Quad(
                Types.Vec3f(-1.0f, BLEND_Y1, BLEND_DEPTH), Types.Vec3f(1.0f, BLEND_Y1, BLEND_DEPTH),
                Types.Vec3f(1.0f, -1.0f, BLEND_DEPTH), Types.Vec3f(-1.0f, -1.0f, BLEND_DEPTH)
        ))

        tasks = arrayOf(RenderingTask(videoModel, quadShader))

        texture = createTexture()
        surfaceTexture = SurfaceTexture(texture)

        startPlayback()
    }

    override fun onDrawFrame(unused: GL10) {
        if (!mediaPlayerPrepared)
            return

        // 背景色を再描画
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        surfaceTexture!!.updateTexImage()
        val stTransform = FloatArray(16)
        surfaceTexture!!.getTransformMatrix(stTransform)
        quadShader.useProgram()
        quadShader.uniformMatrix4fv("stTransform", 1, false, stTransform, 0)

        for (task in tasks!!) {
            val shader = task.shader()
            shader.useProgram()
            shader.ifExistsUniform2fv("fCenter", 1, fCenter, 0)
            shader.ifExistsUniform2fv("rCenter", 1, rCenter, 0)
            shader.ifExistsUniform2fv("fLen", 1, fLen, 0)
            shader.ifExistsUniform2fv("rLen", 1, rLen, 0)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture)
            shader.uniform1i("texture", 0)

            task.render()

            checkGLError("Drawing poly")
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    //endregion
    private fun startPlayback() {
        val surface = Surface(surfaceTexture)
        try {
            Log.d(TAG, "creating MediaPlayer")
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(MyContexts.applicationContext, uri)
            mediaPlayer!!.setSurface(surface)
            mediaPlayer!!.isLooping = true

            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

            mediaPlayer!!.setOnPreparedListener { mediaPlayer ->
                mediaPlayerPrepared = true
                mediaPlayer.start()
                Log.d(TAG, "mediaPlayer.start()")
            }

            mediaPlayer!!.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
