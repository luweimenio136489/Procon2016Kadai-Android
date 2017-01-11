package nittcprocon.glathlete;

import android.opengl.GLES20;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * GLのシェーダーをコンパイルし、パラメータの情報を保持する
 * TODO: 実行時エラーチェック
 */

public class ShaderProgram {
    private static final String TAG = "ShaderProgram";
    private int program;
    private Map<String, ParamInfo> parameters = new HashMap<>();

    // 結局locationしか使わない
    public static class ParamInfo {
        enum Qualifier {
            Attribute,
            Uniform
        }
        public Qualifier qualifier;
        public int size;
        public int type;
        public int location;

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
    }

    public int getProgram() {
        return program;
    }

    public int getLocationOf(String name) {
        return parameters.get(name).location;
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
