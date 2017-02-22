package nittcprocon.glathlete

import nittcprocon.glathlete.Types.*

internal interface Model {
    fun addTri(tri: Tri): Model
    fun addQuad(quad: Quad): Model
    fun drawWithShader(shader: ShaderProgram)
}
