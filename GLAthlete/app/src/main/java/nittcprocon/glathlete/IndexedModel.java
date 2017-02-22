package nittcprocon.glathlete;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import static nittcprocon.glathlete.Types.*;

/**
 * インデックスバッファを使うモデル
 * 描画はそこそこ速いはず
 * 生成の処理は雑なので遅いかも
 */

class IndexedModel extends SlowModel {
    private List<Vec3f> uniqueVertices;
    private List<Short> indices;
    private int vbo, ibo;
    private boolean areDataPrepared = false, isBufferBound = false;

    @Override
    public Model addTri(Tri tri) {
        super.addTri(tri);
        return this;
    }

    @Override
    public Model addQuad(Quad quad) {
        super.addQuad(quad);
        return this;
    }

    @Override
    public void drawWithShader(ShaderProgram shader) {
        if (!areDataPrepared)
            prepareData();

        if (!isBufferBound)
            bindBuffer();

        shader.useProgram();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(shader.getLocationOf("position"));
        GLES20.glVertexAttribPointer(shader.getLocationOf("position"), 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size(), GLES20.GL_UNSIGNED_SHORT, 0);
    }

    private void prepareData() {
        isBufferBound = false;

        uniqueVertices = new ArrayList<>();
        indices = new ArrayList<>();

        // 頂点を重複排除し、インデックスのリストをつくる (O(n log n)とかだと思う)
        // TODO: uniqueVerticesのデータ構造を工夫する？
        int dup = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Vec3f v = vertices.get(i);
            if (uniqueVertices.contains(v)) { // Vec3f#equals
                indices.add((short) uniqueVertices.indexOf(v));
                dup++;
            } else {
                uniqueVertices.add(v);
                indices.add((short)(i - dup));
            }
        }

        areDataPrepared = true;
    }

    private void bindBuffer() {
        // 頂点配列をFloatBufferに押し込む
        FloatBuffer vFloatBuffer = ByteBuffer.allocateDirect(Float.SIZE * 3 * uniqueVertices.size()).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (Vec3f v : uniqueVertices)
            for (float x : v.asArray())
                vFloatBuffer.put(x);
        vFloatBuffer.position(0);

        // インデックスをShortBufferに押し込む
        ShortBuffer iShortBuffer = ByteBuffer.allocateDirect(Short.SIZE * indices.size()).order(ByteOrder.nativeOrder()).asShortBuffer();
        for (short i : indices)
            iShortBuffer.put(i);
        iShortBuffer.position(0);

        // GLのバッファオブジェクトを作る
        int[] buffer = new int[2];
        GLES20.glGenBuffers(2, buffer, 0);
        vbo = buffer[0];
        ibo = buffer[1];

        // 頂点バッファオブジェクト
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, Float.SIZE * 3 * uniqueVertices.size(), vFloatBuffer, GLES20.GL_STATIC_DRAW);

        // インデックスバッファオブジェクト
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, Short.SIZE * indices.size(), iShortBuffer, GLES20.GL_STATIC_DRAW);

        isBufferBound = true;
    }
}
