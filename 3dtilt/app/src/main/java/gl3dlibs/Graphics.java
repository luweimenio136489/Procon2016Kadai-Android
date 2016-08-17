package gl3dlibs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ke on 2016/08/16.
 */

//2Dグラフィックス
public class Graphics {
    public  int         screenW;     //画面幅
    public  int         screenH;     //画面高さ
    private FloatBuffer vertexBuffer;//頂点バッファ
    private FloatBuffer uvBuffer;    //UVバッファ

    //コンストラクタ
    public Graphics(int screenW,int screenH) {
        this.screenW=screenW;
        this.screenH=screenH;

        //頂点バッファの生成
        float[] vertexs={
                -1.0f, 1.0f,0.0f,//頂点0
                -1.0f,-1.0f,0.0f,//頂点1
                1.0f, 1.0f,0.0f,//頂点2
                1.0f,-1.0f,0.0f,//頂点3
        };
        vertexBuffer=makeFloatBuffer(vertexs);

        //UVバッファの生成
        float[] uvs={
                0.0f,0.0f,//左上
                0.0f,1.0f,//左下
                1.0f,0.0f,//右上
                1.0f,1.0f,//右下
        };
        uvBuffer=makeFloatBuffer(uvs);
    }

    //2D描画の設定
    public void setup2D() {
        GL10 gl=GLES.gl;

        //頂点配列の有効化
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        //デプステストと光源の無効化
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glDisable(GL10.GL_LIGHT0);

        //ブレンドの指定
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);

        //射影変換
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glColor4f(1.0f,1.0f,1.0f,1.0f);
        gl.glTexCoordPointer(2,GL10.GL_FLOAT,0,uvBuffer);
    }

    //イメージの描画
    public void drawImage(Texture texture,int x,int y) {
        drawImage(texture,x,y,texture.width,texture.height);
    }

    //イメージの描画
    public void drawImage(Texture texture,int x,int y,int w,int h) {
        drawImage(texture,x,y,w,h,0,0,texture.width,texture.height);
    }

    //イメージの描画
    public void drawImage(Texture texture,int dx,int dy,int dw,int dh,
                          int sx,int sy,int sw,int sh) {
        GL10 gl=GLES.gl;
        //ウィンドウ座標を正規化デバイス座標に変換
        float tw=(float)sw/(float)texture.width;
        float th=(float)sh/(float)texture.height;
        float tx=(float)sx/(float)texture.width;
        float ty=(float)sy/(float)texture.height;

        //前処理
        texture.bind();

        //テクスチャ行列の移動・拡縮
        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(tx,ty,0.0f);
        gl.glScalef(tw,th,1.0f);

        //ウィンドウ座標を正規化デバイス座標に変換
        float mx=((float)dx/(float)screenW)*2.0f-1.0f;
        float my=((float)dy/(float)screenH)*2.0f-1.0f;
        float mw=((float)dw/(float)screenW);
        float mh=((float)dh/(float)screenH);

        //モデルビュー行列の移動・拡大縮小
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(mx+mw,-(my+mh),0.0f);
        gl.glScalef(mw,mh,1.0f);

        //四角形の描画
        gl.glVertexPointer(3,GL10.GL_FLOAT,0,vertexBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP,0,4);

        //後処理
        texture.unbind();
    }

    //float配列をFloatBufferに変換
    private FloatBuffer makeFloatBuffer(float[] array) {
        FloatBuffer fb= ByteBuffer.allocateDirect(array.length*4).order(
                ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(array).position(0);
        return fb;
    }
}