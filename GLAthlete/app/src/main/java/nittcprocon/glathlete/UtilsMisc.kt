package nittcprocon.glathlete

import android.content.SharedPreferences
import android.opengl.Matrix

import java.lang.Math.abs
import java.lang.Math.cos
import java.lang.Math.sin

import nittcprocon.glathlete.Types.*

/**
 * どこから呼んでもいい便利メソッドたち
 * import staticして使う
 */

internal object UtilsMisc {
    /* SharedPreferencesから値をロード */
    fun loadSharedPreferences(sharedPreferences: SharedPreferences, fCenter: FloatArray, rCenter: FloatArray, fLen: FloatArray, rLen: FloatArray) {
        fCenter[0] = sharedPreferences.getFloat("front_center_u", 0.25f)
        fCenter[1] = sharedPreferences.getFloat("front_center_v", 0.4444f)

        rCenter[0] = sharedPreferences.getFloat("rear_center_u", 0.75f)
        rCenter[1] = sharedPreferences.getFloat("rear_center_v", 0.4444f)

        fLen[0] = sharedPreferences.getFloat("front_length_u", (0.25 * 0.9).toFloat())
        fLen[1] = sharedPreferences.getFloat("front_length_v", (0.4444 * 0.9).toFloat())

        rLen[0] = sharedPreferences.getFloat("rear_length_u", (0.25 * 0.9).toFloat())
        rLen[1] = sharedPreferences.getFloat("rear_length_v", (0.4444 * 0.9).toFloat())
    }

    /* SharedPreferencesに値をセット */
    fun setSharedPreferences(sharedPreferences: SharedPreferences, fCenter: FloatArray, rCenter: FloatArray, fLen: FloatArray, rLen: FloatArray) {
        val editor = sharedPreferences.edit()
        editor.putFloat("front_center_u", fCenter[0])
        editor.putFloat("front_center_v", fCenter[1])
        editor.putFloat("rear_center_u", rCenter[0])
        editor.putFloat("rear_center_v", rCenter[1])
        editor.putFloat("front_length_u", fLen[0])
        editor.putFloat("front_length_v", fLen[1])
        editor.putFloat("rear_length_u", rLen[0])
        editor.putFloat("rear_length_v", rLen[1])
        editor.apply()
    }

    /* 3つの行列を一度に掛け算して返す */
    fun calcMVP(m: FloatArray, v: FloatArray, p: FloatArray): FloatArray {
        val mv = FloatArray(16)
        Matrix.multiplyMM(mv, 0, v, 0, m, 0)
        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, p, 0, mv, 0)
        return mvp
    }

    /* 2要素のfloatベクトルを(x, y)みたいな書式にする */
    fun dump2fv(fv: FloatArray?): String {
        if (fv == null)
            return "null"

        return "(" + fv[0] + ", " + fv[1] + ")"
    }

    fun nearlyEquals(a: Float, b: Float): Boolean {
        return abs(a - b) < 0.000001 // この数字は適当
    }

    // r, θ, φ から Vec3f(x, y, z)に
    fun rtp2xyz(r: Float, t: Float, p: Float): Vec3f {
        return Vec3f(
                (r.toDouble() * sin(t.toDouble()) * cos(p.toDouble())).toFloat(),
                (r.toDouble() * sin(t.toDouble()) * sin(p.toDouble())).toFloat(),
                (r * cos(t.toDouble())).toFloat()
        )
    }
}
