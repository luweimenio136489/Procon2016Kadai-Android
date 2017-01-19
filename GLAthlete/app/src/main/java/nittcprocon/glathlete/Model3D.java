package nittcprocon.glathlete;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import static nittcprocon.glathlete.Types.*;

/**
 * モデルの頂点・インデックスバッファの生データを保持するクラス
 * addXXX()を呼んでポリゴンを追加する
 * getXXX()というメソッドが軽量アクセサとは限らないので注意
 */

class Model3D {
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

    Model3D addTri(Tri t) {
        isBufferFresh = false;
        arrayVerticesList.addAll(t.asList());
        return this;
    }

    Model3D addQuad(Quad q) {
        isBufferFresh = false;
        addTri(new Tri(q.a, q.b, q.c));
        addTri(new Tri(q.c, q.d, q.a));
        return this;
    }

    FloatBuffer getVertices() {
        if(!isBufferFresh)
            createBuffer();

        return vertices;
    }

    ShortBuffer getIndices() {
        if(!isBufferFresh)
            createBuffer();

        return indices;
    }

    void createBuffer() {
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

    public int numberOfUniqueVertices() {
        return uniqueVerticesList.size();
    }

    int vertNum() {
        return uniqueVerticesList.size() * 3;
    }

    short indNum() {
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

    // FIXME: 微妙
    public interface AttribCalculator {
        public abstract Vec2f texCoordAt(Vec3f xyz);
    }

    @FunctionalInterface
    public interface ModelGenerator {
        public abstract ArrayList<Vec3f> generateModel();
    }

    public void generateModelWith(ModelGenerator modelGenerator) {
        isBufferFresh = false;
        arrayVerticesList = modelGenerator.generateModel();
        createBuffer();
    }

    public void addAttribCalculatedWith(AttribCalculator attribCalculator) {

    }
}
