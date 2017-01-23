package nittcprocon.glathlete;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static nittcprocon.glathlete.Types.*;

/**
 * 3つずつ取り出して順番に描画できる素直なモデル
 * 生成は楽だけど描画は遅い(はず)
 */

class SlowModel implements Model {
    protected List<Vec3f> vertices = new ArrayList<>();
    private boolean isBufferBound = false;
    private int vbo;

    public Model addTri(Tri tri) {
        isBufferBound = false;
        vertices.addAll(tri.asList());
        return this;
    }

    public Model addQuad(Quad quad) {
        addTri(new Tri(quad.a, quad.b, quad.c));
        addTri(new Tri(quad.d, quad.c, quad.a));
        return this;
    }

    public void drawWithShader(ShaderProgram shader) {
        if (!isBufferBound)
            bindBuffer();

        shader.useProgram();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(vbo);
        GLES20.glVertexAttribPointer(shader.getLocationOf("position"), 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size());
    }

    private void bindBuffer() {
        // 頂点配列をFloatBufferに押し込む
        FloatBuffer vFloatBuffer = ByteBuffer.allocateDirect(Float.SIZE * 3 * vertices.size()).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (Vec3f v : vertices)
            for (float x : v.asArray())
                vFloatBuffer.put(x);
        vFloatBuffer.position(0);

        // GLのバッファオブジェクトを作る
        int[] buffer = new int[1];
        GLES20.glGenBuffers(1, buffer, 0);
        vbo = buffer[0];

        // 頂点バッファオブジェクト
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Float.SIZE * 3 * vertices.size(), vFloatBuffer, GLES20.GL_STATIC_DRAW);

        isBufferBound = true;
    }
}
