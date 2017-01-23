package nittcprocon.glathlete;

import android.opengl.GLES20;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static nittcprocon.glathlete.UtilsGL.*;
import static nittcprocon.glathlete.UtilsMisc.*;

/**
 * GLのシェーダーをコンパイルし、パラメータの情報を保持する
 * TODO: 実行時エラーチェック
 */

class ShaderProgram {
    private static final String TAG = "ShaderProgram";
    private int program;
    private Map<String, ParamInfo> parameters = new HashMap<>();

    // 結局locationしか使わない
    private static class ParamInfo {
        enum Qualifier {
            Attribute,
            Uniform
        }
        Qualifier qualifier;
        int size;
        int type;
        int location;

        ParamInfo(Qualifier qualifier, int size, int type, int location) {
            this.qualifier = qualifier;
            this.size = size;
            this.type = type;
            this.location = location;
        }
    }

    ShaderProgram(String vShader, String fShader) {
        int vShaderId = loadGLShader(GLES20.GL_VERTEX_SHADER, vShader);
        int fShaderId = loadGLShader(GLES20.GL_FRAGMENT_SHADER, fShader);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShaderId);
        GLES20.glAttachShader(program, fShaderId);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);

        //checkGLError("createProgram");

        // パラメータの個数を取得
        int[] attribCount = new int[1];
        int[] uniformCount = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTES, attribCount, 0);
        GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORMS, uniformCount, 0);
        Log.d(TAG, "We have " + attribCount[0] + " attributes and " + uniformCount[0] + " uniforms");

        // パラメータの情報を取得
        // indexとlocationは多くの場合に等しい？
        for (int i = 0; i < attribCount[0]; i++) {
            int[] size = new int[1];
            int[] type = new int[1];
            String attribName = GLES20.glGetActiveAttrib(program, i, size, 0, type, 0);
            int location = GLES20.glGetAttribLocation(program, attribName);
            parameters.put(attribName, new ParamInfo(ParamInfo.Qualifier.Attribute, size[0], type[0], location));
            Log.d(TAG, "Attribute " + attribName + ": size " + size[0] + ", type " + type[0] + ", location " + location);
        }
        for (int i = 0; i < uniformCount[0]; i++) {
            int[] size = new int[1];
            int[] type = new int[1];
            String uniformName = GLES20.glGetActiveUniform(program, i, size, 0, type, 0);
            int location = GLES20.glGetUniformLocation(program, uniformName);
            parameters.put(uniformName, new ParamInfo(ParamInfo.Qualifier.Uniform, size[0], type[0], location));
            Log.d(TAG, "Uniform " + uniformName + ": size " + size[0] + ", type " + type[0] + ", location " + location);
        }

        checkGLError("ShaderProgram");
    }

    int getProgram() {
        return program;
    }

    void useProgram() {
        GLES20.glUseProgram(program);
        checkGLError("useProgram: " + program);
    }

    public void deleteProgram() {
        GLES20.glUseProgram(0);
        GLES20.glDeleteProgram(program);
        checkGLError("deleteProgram: " + program);
    }

    void uniform2fv(String name, int count, float[] vecs, int offset) {
        GLES20.glUniform2fv(getLocationOf(name), count, vecs, offset);
        checkGLError("uniform2fv: " + name + " -> " + dump2fv(vecs));
    }

    void uniformMatrix4fv(String name, int count, boolean transpose, float[] mats, int offset) {
        GLES20.glUniformMatrix4fv(getLocationOf(name), count, transpose, mats, offset);
        checkGLError("uniformMatrix4fv: " + name);
    }

    void uniform1i(String name, int value) {
        GLES20.glUniform1i(getLocationOf(name), value);
        checkGLError("uniform1i: " + name + " -> " + value);
    }

    void ifExistsUniform2fv(String name, int count, float[] floats, int offset) {
        if (hasParameter(name)) {
            uniform2fv(name, count, floats, offset);
        }
    }

    int getLocationOf(String name) {
        if (!parameters.containsKey(name)) {
            throw new RuntimeException("ShaderProgram::getLocationOf: parameter " + name + " not found");
        }
        return parameters.get(name).location;
    }

    boolean hasParameter(String name) {
        return parameters.containsKey(name);
    }

    private int loadGLShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }
}