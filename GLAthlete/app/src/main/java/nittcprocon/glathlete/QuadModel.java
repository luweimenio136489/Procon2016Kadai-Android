package nittcprocon.glathlete;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static nittcprocon.glathlete.Types.*;

/**
 * 四角形の板ポリ専用モデル
 * ジグザグ順(三角形ストリップ)
 */

class QuadModel implements Model {
    private FloatBuffer vFloatBuffer;
    private int vbo;
    private boolean isBufferBound = false;
    QuadModel(Quad q) {
        vFloatBuffer = ByteBuffer.allocateDirect(Float.SIZE * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (Vec3f v : q.asArray())
            for (float f : v.asArray())
                vFloatBuffer.put(f);
        vFloatBuffer.position(0);
    }

    public Model addTri(Tri tri) {
        // なにもしない
        return this;
    }

    public Model addQuad(Quad quad) {
        // なにもしない
        return this;
    }

    public void drawWithShader(ShaderProgram shader) {
        if (!isBufferBound)
            bindBuffer();

        shader.useProgram();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(shader.getLocationOf("position"));
        GLES20.glVertexAttribPointer(shader.getLocationOf("position"), 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private void bindBuffer() {
        int[] buffer = new int[1];
        GLES20.glGenBuffers(1, buffer, 0);
        vbo = buffer[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Float.SIZE * 3 * 4, vFloatBuffer, GLES20.GL_STATIC_DRAW);

        isBufferBound = true;
    }
}
