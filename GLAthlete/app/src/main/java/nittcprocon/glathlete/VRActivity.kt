package nittcprocon.glathlete

import android.content.SharedPreferences
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.KeyEvent
import android.view.Surface

import com.google.vr.sdk.base.AndroidCompat
import com.google.vr.sdk.base.Eye
import com.google.vr.sdk.base.GvrActivity
import com.google.vr.sdk.base.GvrView
import com.google.vr.sdk.base.HeadTransform
import com.google.vr.sdk.base.Viewport

import javax.microedition.khronos.egl.EGLConfig

import java.lang.Math.PI
import java.lang.Math.max

import nittcprocon.glathlete.Types.*
import nittcprocon.glathlete.UtilsGL.checkGLError
import nittcprocon.glathlete.UtilsGL.createTexture
import nittcprocon.glathlete.UtilsMisc.calcMVP
import nittcprocon.glathlete.UtilsMisc.dump2fv
import nittcprocon.glathlete.UtilsMisc.loadSharedPreferences
import nittcprocon.glathlete.UtilsMisc.nearlyEquals
import nittcprocon.glathlete.UtilsMisc.rtp2xyz
import nittcprocon.glathlete.UtilsMisc.setSharedPreferences

/**
 * Google VR SDKを使って実際の描画を行う
 * TODO: カリング
 * FIXME: テクスチャユニットをTEXTURE0しか使わない前提になってる
 */

class VRActivity : GvrActivity(), GvrView.StereoRenderer, KeyEvent.Callback {
    private val TAG = "VRActivity"
    private val CAMERA_Z = 0.01f
    private val Z_NEAR = 0.1f
    private val Z_FAR = 100.0f
    // mat4
    private var modelMat = FloatArray(16)
    private var viewMat  = FloatArray(16)
    private var camera   = FloatArray(16)
    // vec2
    private var fCenter = FloatArray(2)
    private var rCenter = FloatArray(2)
    private var fLen    = FloatArray(2)
    private var rLen    = FloatArray(2)

    private var texture = 0
    private var tasks: Array<RenderingTask>? = null
    private var uri: String? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var mediaPlayer: MediaPlayer? = null

    // onCreateで初期化される
    private lateinit var view: GvrView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        initializeGvrView()

        uri = intent.getStringExtra("uri")
        Log.d(TAG, "URI: " + uri!!)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        loadSharedPreferences(sharedPreferences, fCenter, rCenter, fLen, rLen)
    }

    override fun onSurfaceCreated(config: EGLConfig) {
        Log.i(TAG, "onSurfaceCreated")
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f)

        val fAngle = (0.4 * PI).toFloat()
        val rAngle = (0.6 * PI).toFloat()

        val frontModel: Model = IndexedModel()
        val frontSideModel: Model = IndexedModel()
        val rearSideModel: Model = IndexedModel()
        val rearModel: Model = IndexedModel()

        generatePartialSphereModel(frontModel, 0.0f, fAngle)
        generatePartialSphereModel(frontSideModel, fAngle, (0.5 * PI).toFloat())
        generatePartialSphereModel(rearSideModel, (0.5 * PI).toFloat(), rAngle)
        generatePartialSphereModel(rearModel, rAngle, PI.toFloat())

        val frontShader     = ShaderProgram(R.raw.v_front, R.raw.f_frontrear)
        val frontSideShader = ShaderProgram(R.raw.v_frontside, R.raw.f_frontside)
        val rearSideShader  = ShaderProgram(R.raw.v_rearside, R.raw.f_rearside)
        val rearShader      = ShaderProgram(R.raw.v_rear, R.raw.f_frontrear)

        tasks = arrayOf(RenderingTask(frontModel, frontShader), RenderingTask(frontSideModel, frontSideShader), RenderingTask(rearSideModel, rearSideShader), RenderingTask(rearModel, rearShader))

        setShaderParams()

        texture = createTexture()
        surfaceTexture = SurfaceTexture(texture)

        /* 変換行列の設定 */
        // TODO: 回転機能
        Matrix.setIdentityM(modelMat, 0)

        startPlayback()
    }

    public override fun onStop() {
        Log.v(TAG, "onStop")
        super.onStop()

        if (mediaPlayer!!.isPlaying)
            mediaPlayer!!.stop()
        mediaPlayer!!.reset()
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    public override fun onRestart() {
        Log.d(TAG, "onRestart")
        super.onRestart()
    }

    /* フレームの描画前にOpenGL ESの準備をする */
    override fun onNewFrame(headTransform: HeadTransform) {
        // 出力先, オフセット, 視点のx, y, z, 視点の中心のx, y, z, 上向きベクトルのx, y, z
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)

        surfaceTexture!!.updateTexImage()

        /* テクスチャバッファの変換行列 */
        val stTransform = FloatArray(16)
        surfaceTexture!!.getTransformMatrix(stTransform)

        for (task in tasks!!) {
            task.shader().useProgram()
            task.shader().uniformMatrix4fv("stTransform", 1, false, stTransform, 0)
        }

        checkGLError("onNewFrame")
    }

    /* 与えられたEyeに対してフレームを描画する */
    override fun onDrawEye(eye: Eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        Matrix.multiplyMM(viewMat, 0, eye.eyeView, 0, camera, 0)
        val perspective = eye.getPerspective(Z_NEAR, Z_FAR)
        val mvpMat = calcMVP(modelMat, viewMat, perspective)

        for (task in tasks!!) {
            task.shader().useProgram()
            task.shader().uniformMatrix4fv("mvpMat", 1, false, mvpMat, 0)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture)
            task.shader().uniform1i("texture", 0)

            task.render()

            // unbind
            //GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            //GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        checkGLError("onDrawEye")
    }

    override fun onRendererShutdown() {
        Log.v(TAG, "onRendererShutdown")
    }

    override fun onFinishFrame(viewport: Viewport) {}

    override fun onSurfaceChanged(width: Int, height: Int) {
        Log.v(TAG, "onSurfaceChanged")
    }

    // 助けて
    override fun onKeyUp(keycode: Int, event: KeyEvent?): Boolean {
        when (keycode) {
        // fCenter
        // fCenter.u
            KeyEvent.KEYCODE_Q // up
            -> {
                fCenter[0] = Math.min(1.0f, fCenter[0] + 0.01f)
                Log.d(TAG, "fCenter.u -> " + fCenter[0])
            }
            KeyEvent.KEYCODE_A // down
            -> {
                fCenter[0] = Math.max(0.0f, fCenter[0] - 0.01f)
                Log.d(TAG, "fCenter.u -> " + fCenter[0])
            }
        // fCenter.v
            KeyEvent.KEYCODE_W // up
            -> {
                fCenter[1] = Math.min(1.0f, fCenter[1] + 0.01f)
                Log.d(TAG, "fCenter.v -> " + fCenter[1])
            }
            KeyEvent.KEYCODE_S // down
            -> {
                fCenter[1] = Math.max(0.0f, fCenter[1] - 0.01f)
                Log.d(TAG, "fCenter.v -> " + fCenter[1])
            }

        // rCenter
        // rCenter.u
            KeyEvent.KEYCODE_E // up
            -> {
                rCenter[0] = Math.min(1.0f, rCenter[0] + 0.01f)
                Log.d(TAG, "rCenter.u -> " + rCenter[0])
            }
            KeyEvent.KEYCODE_D // down
            -> {
                rCenter[0] = Math.max(0.0f, rCenter[0] - 0.01f)
                Log.d(TAG, "rCenter.u -> " + rCenter[0])
            }
        // rCenter.v
            KeyEvent.KEYCODE_R // up
            -> {
                rCenter[1] = Math.min(1.0f, rCenter[1] + 0.01f)
                Log.d(TAG, "rCenter.v -> " + rCenter[1])
            }
            KeyEvent.KEYCODE_F // down
            -> {
                rCenter[1] = Math.max(0.0f, rCenter[1] - 0.01f)
                Log.d(TAG, "rCenter.v -> " + rCenter[1])
            }

        // fLen
        // fLen.u
            KeyEvent.KEYCODE_T // up
            -> {
                fLen[0] = Math.min(1.0f, fLen[0] + 0.01f)
                Log.d(TAG, "fLen.u -> " + fLen[0])
            }
            KeyEvent.KEYCODE_G // down
            -> {
                fLen[0] = Math.max(0.0f, fLen[0] - 0.01f)
                Log.d(TAG, "fLen.u -> " + fLen[0])
            }
        // fLen.v
            KeyEvent.KEYCODE_Y // up
            -> {
                fLen[1] = Math.min(1.0f, fLen[1] + 0.01f)
                Log.d(TAG, "fLen.v -> " + fLen[1])
            }
            KeyEvent.KEYCODE_H // down
            -> {
                fLen[1] = Math.max(0.0f, fLen[1] - 0.01f)
                Log.d(TAG, "fLen.v -> " + fLen[1])
            }

        // rLen
        // rLen.u
            KeyEvent.KEYCODE_U // up
            -> {
                rLen[0] = Math.min(1.0f, rLen[0] + 0.01f)
                Log.d(TAG, "rLen.u -> " + rLen[0])
            }
            KeyEvent.KEYCODE_J // down
            -> {
                rLen[0] = Math.max(0.0f, rLen[0] - 0.01f)
                Log.d(TAG, "rLen.u -> " + rLen[0])
            }
        // rLen.v
            KeyEvent.KEYCODE_I // up
            -> {
                rLen[1] = Math.min(1.0f, rLen[1] + 0.01f)
                Log.d(TAG, "rLen.v -> " + rLen[1])
            }
            KeyEvent.KEYCODE_K // down
            -> {
                rLen[1] = Math.max(0.0f, rLen[1] - 0.01f)
                Log.d(TAG, "rLen.v -> " + rLen[1])
            }

            KeyEvent.KEYCODE_C -> {
                setSharedPreferences(sharedPreferences, fCenter, rCenter, fLen, rLen)
                Log.d(TAG, "設定を保存")
            }

            KeyEvent.KEYCODE_Z -> {
                mediaPlayer!!.pause()
                return true
            }
            KeyEvent.KEYCODE_X -> {
                mediaPlayer!!.start()
                return true
            }

            else -> return super.onKeyUp(keycode, event)
        }

        // GLのメソッドはGLのスレッドから呼ぶ必要がある
        view.queueEvent { setShaderParams() }

        return true
    }

    private fun setShaderParams() {
        Log.d(TAG, "setShaderParams: setting ")
        Log.d(TAG, "fCenter: " + dump2fv(fCenter) + ", rCenter: " + dump2fv(rCenter))
        Log.d(TAG, "fLen: " + dump2fv(fLen) + ", rLen: " + dump2fv(rLen))

        for (task in tasks!!) {
            val shader = task.shader()
            shader.useProgram()
            shader.ifExistsUniform2fv("fCenter", 1, fCenter, 0)
            shader.ifExistsUniform2fv("rCenter", 1, rCenter, 0)
            shader.ifExistsUniform2fv("fLen", 1, fLen, 0)
            shader.ifExistsUniform2fv("rLen", 1, rLen, 0)
        }

        checkGLError("Parameter Setting")
        Log.d(TAG, "Set GL parameters")
    }

    fun startPlayback() {
        val surface = Surface(surfaceTexture)
        try {
            Log.d(TAG, "creating MediaPlayer")
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(applicationContext, Uri.parse(uri))
            mediaPlayer!!.setSurface(surface)
            mediaPlayer!!.isLooping = true

            //mediaPlayer.setOnBufferingUpdateListener(this);
            //mediaPlayer.setOnCompletionListener(this);
            //mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

            mediaPlayer!!.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()
                Log.d(TAG, "mediaPlayer.start()")
            }
            mediaPlayer!!.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun initializeGvrView() {
        setContentView(R.layout.activity_vr)

        view = findViewById(R.id.gvr_view) as GvrView
        view.setEGLConfigChooser(8, 8, 8, 8, 16, 8)

        view.setRenderer(this)
        view.setTransitionViewEnabled(true)
        view.setOnCardboardBackButtonListener { onBackPressed() }

        if (view.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true)
        }
        super.setGvrView(view)
    }

    private fun generatePartialSphereModel(model: Model, thetaBegin: Float, thetaEnd: Float) {
        val RADIUS = 1.0f
        val STACKS_PER_RADIANS = (24.0 / PI).toFloat()
        val STACKS = max(1.0, (STACKS_PER_RADIANS * (thetaEnd - thetaBegin)).toDouble()).toInt()
        val SLICES = 24

        generatePartialSphereModel(model, thetaBegin, thetaEnd, RADIUS, STACKS, SLICES)
    }

    private fun generatePartialSphereModel(model: Model, thetaBegin: Float, thetaEnd: Float, radius: Float, stacks: Int, slices: Int) {
        val theta = thetaEnd - thetaBegin
        for (t in 0..stacks - 1) {
            val theta1 = thetaBegin + theta * t / stacks
            val theta2 = thetaBegin + theta * (t + 1) / stacks
            for (p in 0..slices - 1) {
                val phi1 = PI.toFloat() * 2.0f * p.toFloat() / slices
                val phi2 = PI.toFloat() * 2.0f * (p + 1).toFloat() / slices

                val v1 = rtp2xyz(radius, theta1, phi1)
                val v2 = rtp2xyz(radius, theta1, phi2)
                val v3 = rtp2xyz(radius, theta2, phi2)
                val v4 = rtp2xyz(radius, theta2, phi1)

                if (theta1 == 0.0f) {                           // top/front cap, v1 == v2
                    model.addTri(Tri(v2, v3, v4))
                } else if (nearlyEquals(theta2, PI.toFloat())) {   // bottom/rear cap, v3 == v4
                    model.addTri(Tri(v1, v2, v3))
                } else {
                    model.addQuad(Quad(v1, v2, v3, v4))
                }
            }
        }
    }
}

