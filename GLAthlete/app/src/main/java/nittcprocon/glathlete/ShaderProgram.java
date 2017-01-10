package nittcprocon.glathlete;

import android.opengl.GLES20;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * GLのシェーダーをコンパイルし、ハンドルを保持する
 */

public class ShaderProgram {
    private static final String TAG = "ShaderProgram";
    private int program;
    private Map<String, Integer> parameters = new HashMap<>();

    public static class Parameter {
        enum Qualifier {
            Attribute,
            Uniform
        }
        public Qualifier qualifier;
        public String name;

        Parameter(Qualifier qualifier_, String name_) {
            qualifier = qualifier_;
            name = name_;
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
    }

    public int getProgram() {
        return program;
    }

    public int getLocationOf(String name) {
        return parameters.get(name);
    }

    public void setParamInfo(Set<Parameter> paramSet) {
        for (Parameter p : paramSet) {
            parameters.put(p.name, getParamLocation(p));
        }
    }

    private int getParamLocation(Parameter parameter) {
        switch (parameter.qualifier) {
            case Attribute:
                return GLES20.glGetAttribLocation(program, parameter.name);
            case Uniform:
                return  GLES20.glGetUniformLocation(program, parameter.name);
            default:
                throw new RuntimeException("This can't happen");
        }
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
