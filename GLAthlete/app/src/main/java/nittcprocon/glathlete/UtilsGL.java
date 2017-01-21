package nittcprocon.glathlete;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

/**
 * GLのContextから呼ぶ便利メソッドたち
 * import staticして使う
 */

class UtilsGL {
    static int createTexture() {
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
    static void checkGLError(String label) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(label + ": glError " + error);
        }
    }
}
