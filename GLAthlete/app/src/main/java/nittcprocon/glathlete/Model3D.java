package nittcprocon.glathlete;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class Model3D {
    private static final String TAG = "Model3D";
    public FloatBuffer vertices;
    public ShortBuffer indices;

    private ArrayList<Vec3f> verticesList;
    private ArrayList<Short> indicesList;

    Model3D() {
        verticesList = new ArrayList<Vec3f>();
        indicesList = new ArrayList<Short>();
    }

    public void addTri(Tri t) {
        verticesList.addAll(t.asList());
        for (int i = verticesList.size() - 3; i < verticesList.size(); i++) {
            indicesList.add((short)i);
        }
    }

    public void addQuad(Quad q) {
        verticesList.addAll(q.asList());
        for (short i : new short[] {0, 1, 2, 2, 3, 0}) {
            indicesList.add((short)(verticesList.size() - 4 + i));
        }
    }

    public void createBuffer() {
        Log.d(TAG, "createBuffer");
        Log.d(TAG, "vertices: " + vertNum());
        vertices = ByteBuffer.allocateDirect(Float.SIZE * vertNum()).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (Vec3f v : verticesList)
            for (float x : v.asArray())
                vertices.put(x);
        vertices.position(0);

        Log.d(TAG, "indices: " + indNum());
        indices = ByteBuffer.allocateDirect(Short.SIZE * indNum()).order(ByteOrder.nativeOrder()).asShortBuffer();
        for (short i : indicesList)
            indices.put(i);
        indices.position(0);
    }

    public int vertNum() {
        return verticesList.size() * 3;
    }

    public int indNum() {
        return indicesList.size();
    }
}
