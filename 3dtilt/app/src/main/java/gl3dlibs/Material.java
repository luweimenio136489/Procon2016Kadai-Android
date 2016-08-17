package gl3dlibs;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ke on 2016/08/16.
 */
//マテリアル
public class Material extends GLObject {
    public Texture texture;             //テクスチャ
    public float[] ambient  =new float[4];//環境光
    public float[] diffuse  =new float[4];//拡散光
    public float[] speculer =new float[4];//鏡面光
    public float[] shininess=new float[1];//鏡面反射角度

    //バインド
    @Override
    public void bind() {
        GL10 gl=GLES.gl;
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_AMBIENT,ambient,0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_DIFFUSE,diffuse,0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_SPECULAR,speculer,0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_SHININESS,shininess,0);
        if (texture!=null) texture.bind();
    }

    //アンバインド
    @Override
    public void unbind() {
        if (texture!=null) texture.unbind();
    }

    //破棄
    @Override
    public void dispose() {
        if (texture!=null) {
            texture.dispose();
            texture=null;
        }
    }
}