package nittcprocon.glathlete;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * モデルの頂点を保持するクラス
 * getXXX()というメソッドが軽量アクセサとは限らないので注意
 */

public class Model3D {
    private static final String TAG = "Model3D";
    private FloatBuffer vertices;
    private ShortBuffer indices;

    private ArrayList<Vec3f> arrayVerticesList;     // こいつの中身は3つずつ取り出して順番に描画できる
    private ArrayList<Vec3f> uniqueVerticesList;    // こいつには一意な頂点しか入ってない
    private ArrayList<Short> indicesList;           // ↑の添字に対応するインデックスバッファのもと
    private boolean isBufferFresh;

    Model3D() {
        arrayVerticesList = new ArrayList<Vec3f>();
        uniqueVerticesList = new ArrayList<Vec3f>();
        indicesList = new ArrayList<Short>();
        isBufferFresh = false;
    }

    public void addTri(Tri t) {
        isBufferFresh = false;
        arrayVerticesList.addAll(t.asList());
//        for (int i = verticesList.size() - 3; i < verticesList.size(); i++) {
//            indicesList.add((short)i);
//        }
    }

    public void addQuad(Quad q) {
        isBufferFresh = false;
        addTri(new Tri(q.a, q.b, q.c));
        addTri(new Tri(q.c, q.d, q.a));
//        for (short i : new short[] {0, 1, 2, 2, 3, 0}) {
//            indicesList.add((short)(verticesList.size() - 4 + i));
//        }
    }

    public FloatBuffer getVertices() {
        if(!isBufferFresh)
            createBuffer();

        return vertices;
    }

    public ShortBuffer getIndices() {
        if(!isBufferFresh)
            createBuffer();

        return indices;
    }

    public void createBuffer() {
        prepareIndices();

        Log.d(TAG, "createBuffer");
        Log.d(TAG, "vertices: " + vertNum());
        vertices = ByteBuffer.allocateDirect(Float.SIZE * vertNum()).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (Vec3f v : uniqueVerticesList)
            for (float x : v.asArray())
                vertices.put(x);
        vertices.position(0);

        Log.d(TAG, "indices: " + indNum());
        indices = ByteBuffer.allocateDirect(Short.SIZE * indNum()).order(ByteOrder.nativeOrder()).asShortBuffer();
        for (short i : indicesList)
            indices.put(i);
        indices.position(0);

        isBufferFresh = true;
    }

    public int vertNum() {
        return uniqueVerticesList.size() * 3;
    }

    public short indNum() {
        return (short)indicesList.size();
    }

    private void prepareIndices() {
        uniqueVerticesList = new ArrayList<Vec3f>();
        indicesList = new ArrayList<Short>();

        int dup = 0;
        for (int i = 0; i < arrayVerticesList.size(); i++) {
            Vec3f v = arrayVerticesList.get(i);
            if (uniqueVerticesList.contains(v)) {
                indicesList.add((short) uniqueVerticesList.indexOf(v));
                dup++;
            } else {
                uniqueVerticesList.add(v);
                indicesList.add((short)(i - dup));
            }

        }
    }
}
