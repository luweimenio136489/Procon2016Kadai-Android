package nittcprocon.glathlete

import android.opengl.GLES11Ext
import android.opengl.GLES20

/**
 * GLのContextから呼ぶ便利メソッドたち
 * import staticして使う
 */

internal object UtilsGL {
    fun createTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val textureId = textureIds[0]
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())

        checkGLError("createTexture")

        return textureId
    }

    /* OpenGL ESの内部でエラーがないかチェックし、あったら例外を投げる */
    fun checkGLError(label: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException(label + ": glError " + error)
        }
    }
}
