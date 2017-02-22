package nittcprocon.glathlete

import android.opengl.GLES20
import android.util.Log
import nittcprocon.glathlete.UtilsDroid.readRawTextFile

import java.util.HashMap

import nittcprocon.glathlete.UtilsGL.checkGLError
import nittcprocon.glathlete.UtilsMisc.dump2fv

/**
 * GLのシェーダーをコンパイルし、パラメータの情報を保持する
 */

internal class ShaderProgram(vShader: String, fShader: String) {
    private val TAG = "ShaderProgram"
    val program: Int
    private val parameters = HashMap<String, ParamInfo>()

    // 結局locationしか使わない
    private class ParamInfo(var qualifier: ParamInfo.Qualifier, var size: Int, var type: Int, var location: Int) {
        internal enum class Qualifier {
            Attribute,
            Uniform
        }
    }

    init {
        val vShaderId = loadGLShader(GLES20.GL_VERTEX_SHADER, vShader)
        val fShaderId = loadGLShader(GLES20.GL_FRAGMENT_SHADER, fShader)

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vShaderId)
        GLES20.glAttachShader(program, fShaderId)
        GLES20.glLinkProgram(program)
        GLES20.glUseProgram(program)

        //checkGLError("createProgram");

        // パラメータの個数を取得
        val attribCount = IntArray(1)
        val uniformCount = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTES, attribCount, 0)
        GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORMS, uniformCount, 0)
        Log.d(TAG, "We have " + attribCount[0] + " attributes and " + uniformCount[0] + " uniforms")

        // パラメータの情報を取得
        // indexとlocationは多くの場合に等しい？
        for (i in 0..attribCount[0] - 1) {
            val size = IntArray(1)
            val type = IntArray(1)
            val attribName = GLES20.glGetActiveAttrib(program, i, size, 0, type, 0)
            val location = GLES20.glGetAttribLocation(program, attribName)
            parameters.put(attribName, ParamInfo(ParamInfo.Qualifier.Attribute, size[0], type[0], location))
            Log.d(TAG, "Attribute " + attribName + ": size " + size[0] + ", type " + type[0] + ", location " + location)
        }
        for (i in 0..uniformCount[0] - 1) {
            val size = IntArray(1)
            val type = IntArray(1)
            val uniformName = GLES20.glGetActiveUniform(program, i, size, 0, type, 0)
            val location = GLES20.glGetUniformLocation(program, uniformName)
            parameters.put(uniformName, ParamInfo(ParamInfo.Qualifier.Uniform, size[0], type[0], location))
            Log.d(TAG, "Uniform " + uniformName + ": size " + size[0] + ", type " + type[0] + ", location " + location)
        }

        checkGLError("ShaderProgram")
    }
    constructor(vResId: Int, fResId: Int) : this(readRawTextFile(vResId), readRawTextFile(fResId))

    fun useProgram() {
        GLES20.glUseProgram(program)
        checkGLError("useProgram: " + program)
    }

    fun deleteProgram() {
        GLES20.glUseProgram(0)
        GLES20.glDeleteProgram(program)
        checkGLError("deleteProgram: " + program)
    }

    fun uniform2fv(name: String, count: Int, vecs: FloatArray, offset: Int) {
        GLES20.glUniform2fv(getLocationOf(name), count, vecs, offset)
        checkGLError("uniform2fv: " + name + " -> " + dump2fv(vecs))
    }

    fun uniformMatrix4fv(name: String, count: Int, transpose: Boolean, mats: FloatArray, offset: Int) {
        GLES20.glUniformMatrix4fv(getLocationOf(name), count, transpose, mats, offset)
        checkGLError("uniformMatrix4fv: " + name)
    }

    fun uniform1i(name: String, value: Int) {
        GLES20.glUniform1i(getLocationOf(name), value)
        checkGLError("uniform1i: $name -> $value")
    }

    fun ifExistsUniform2fv(name: String, count: Int, floats: FloatArray, offset: Int) {
        if (hasParameter(name)) {
            uniform2fv(name, count, floats, offset)
        }
    }

    fun getLocationOf(name: String): Int {
        if (!parameters.containsKey(name)) {
            throw RuntimeException("ShaderProgram::getLocationOf: parameter $name not found")
        }
        return parameters[name]!!.location
    }

    fun hasParameter(name: String): Boolean {
        return parameters.containsKey(name)
    }

    private fun loadGLShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)

        // Get the compilation status.
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Error creating shader.")
        }

        return shader
    }
}
