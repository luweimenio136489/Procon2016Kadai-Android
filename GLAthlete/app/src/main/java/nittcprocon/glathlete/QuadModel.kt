package nittcprocon.glathlete

import android.opengl.GLES20

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

import nittcprocon.glathlete.Types.*

/**
 * 四角形の板ポリ専用モデル
 * ジグザグ順(三角形ストリップ)
 */

internal class QuadModel(q: Quad) : Model {
    private val vFloatBuffer: FloatBuffer
    private var vbo: Int = 0
    private var isBufferBound = false

    init {
        vFloatBuffer = ByteBuffer.allocateDirect(java.lang.Float.SIZE * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        for (v in q.asArray())
            for (f in v.asArray())
                vFloatBuffer.put(f)
        vFloatBuffer.position(0)
    }

    override fun addTri(tri: Tri): Model {
        // なにもしない
        return this
    }

    override fun addQuad(quad: Quad): Model {
        // なにもしない
        return this
    }

    override fun drawWithShader(shader: ShaderProgram) {
        if (!isBufferBound)
            bindBuffer()

        shader.useProgram()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glEnableVertexAttribArray(shader.getLocationOf("position"))
        GLES20.glVertexAttribPointer(shader.getLocationOf("position"), 3, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun bindBuffer() {
        val buffer = IntArray(1)
        GLES20.glGenBuffers(1, buffer, 0)
        vbo = buffer[0]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, java.lang.Float.SIZE * 3 * 4, vFloatBuffer, GLES20.GL_STATIC_DRAW)

        isBufferBound = true
    }
}
