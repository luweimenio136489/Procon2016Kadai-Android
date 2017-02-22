package nittcprocon.glathlete

import android.opengl.GLES20

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.ArrayList

import nittcprocon.glathlete.Types.*

/**
 * インデックスバッファを使うモデル
 * 描画はそこそこ速いはず
 * 生成の処理は雑なので遅いかも
 */

internal class IndexedModel : Model {
    private var vertices: MutableList<Vec3f> = ArrayList()
    private var uniqueVertices: MutableList<Vec3f>? = null
    private var indices: MutableList<Short>? = null
    private var vbo: Int = 0
    private var ibo: Int = 0
    private var areDataPrepared = false
    private var isBufferBound = false

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
        if (!areDataPrepared)
            prepareData()

        if (!isBufferBound)
            bindBuffer()

        shader.useProgram()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glEnableVertexAttribArray(shader.getLocationOf("position"))
        GLES20.glVertexAttribPointer(shader.getLocationOf("position"), 3, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices!!.size, GLES20.GL_UNSIGNED_SHORT, 0)
    }

    private fun prepareData() {
        isBufferBound = false

        uniqueVertices = ArrayList<Vec3f>()
        indices = ArrayList<Short>()

        // 頂点を重複排除し、インデックスのリストをつくる (O(n log n)とかだと思う)
        // TODO: uniqueVerticesのデータ構造を工夫する？
        var dup = 0
        for (i in vertices.indices) {
            val v = vertices[i]
            if (uniqueVertices!!.contains(v)) { // Vec3f#equals
                indices!!.add(uniqueVertices!!.indexOf(v).toShort())
                dup++
            } else {
                uniqueVertices!!.add(v)
                indices!!.add((i - dup).toShort())
            }
        }

        areDataPrepared = true
    }

    private fun bindBuffer() {
        // 頂点配列をFloatBufferに押し込む
        val vFloatBuffer = ByteBuffer.allocateDirect(java.lang.Float.SIZE * 3 * uniqueVertices!!.size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        for (v in uniqueVertices!!)
            for (x in v.asArray())
                vFloatBuffer.put(x)
        vFloatBuffer.position(0)

        // インデックスをShortBufferに押し込む
        val iShortBuffer = ByteBuffer.allocateDirect(java.lang.Short.SIZE * indices!!.size).order(ByteOrder.nativeOrder()).asShortBuffer()
        for (i in indices!!)
            iShortBuffer.put(i)
        iShortBuffer.position(0)

        // GLのバッファオブジェクトを作る
        val buffer = IntArray(2)
        GLES20.glGenBuffers(2, buffer, 0)
        vbo = buffer[0]
        ibo = buffer[1]

        // 頂点バッファオブジェクト
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, java.lang.Float.SIZE * 3 * uniqueVertices!!.size, vFloatBuffer, GLES20.GL_STATIC_DRAW)

        // インデックスバッファオブジェクト
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, java.lang.Short.SIZE * indices!!.size, iShortBuffer, GLES20.GL_STATIC_DRAW)

        isBufferBound = true
    }
}
