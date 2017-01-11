package nittcprocon.glathlete;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Model3DのモデルをGLにバインドし、バッファオブジェクトとインデックスの数を保持する
 */

public class ModelBuffer {
    private static final String TAG = "ModelBuffer";
    private int vbo, ibo, indicesCount;

    ModelBuffer(Model3D model3D) {
        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);
        vbo = buffers[0];
        ibo = buffers[1];

        // 頂点バッファオブジェクト
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Float.SIZE * model3D.vertNum(), model3D.getVertices(), GLES20.GL_STATIC_DRAW);
        //checkGLError("Setting vbo");
        Log.d(TAG, "vbo: " + buffers[0] + ", size: " + model3D.vertNum());

        // インデックスバッファオブジェクト
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, Short.SIZE * model3D.indNum(), model3D.getIndices(), GLES20.GL_STATIC_DRAW);
        //checkGLError("Setting ibo");
        Log.d(TAG, "ibo: " + buffers[1] + ", size: " + model3D.indNum());

        indicesCount = model3D.indNum();
    }

    public int getVbo() {
        return vbo;
    }

    public int getIbo() {
        return ibo;
    }

    public int getIndicesCount() {
        return indicesCount;
    }
}
