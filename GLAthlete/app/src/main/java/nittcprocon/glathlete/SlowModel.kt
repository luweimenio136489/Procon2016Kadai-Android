package nittcprocon.glathlete

import android.opengl.GLES20

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.ArrayList

import nittcprocon.glathlete.Types.*

/**
 * 3つずつ取り出して順番に描画できる素直なモデル
 * 生成は楽だけど描画は遅い(はず)
 */

internal open class SlowModel : Model {
    protected var vertices: MutableList<Vec3f> = ArrayList()
    private var isBufferBound = false
    private var vbo: Int = 0

    override fun addTri(tri: Tri): Model {
        isBufferBound = false
        vertices.addAll(tri.asList())
        return this
    }

    override fun addQuad(quad: Quad): Model {
        addTri(Tri(quad.a, quad.b, quad.c))
        addTri(Tri(quad.d, quad.c, quad.a))
        return this
    }

    override fun drawWithShader(shader: ShaderProgram) {
        if (!isBufferBound)
            bindBuffer()

        shader.useProgram()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glEnableVertexAttribArray(vbo)
        GLES20.glVertexAttribPointer(shader.getLocationOf("position"), 3, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size)
    }

    private fun bindBuffer() {
        // 頂点配列をFloatBufferに押し込む
        val vFloatBuffer = ByteBuffer.allocateDirect(java.lang.Float.SIZE * 3 * vertices.size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        for (v in vertices)
            for (x in v.asArray())
                vFloatBuffer.put(x)
        vFloatBuffer.position(0)

        // GLのバッファオブジェクトを作る
        val buffer = IntArray(1)
        GLES20.glGenBuffers(1, buffer, 0)
        vbo = buffer[0]

        // 頂点バッファオブジェクト
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, java.lang.Float.SIZE * 3 * vertices.size, vFloatBuffer, GLES20.GL_STATIC_DRAW)

        isBufferBound = true
    }
}
