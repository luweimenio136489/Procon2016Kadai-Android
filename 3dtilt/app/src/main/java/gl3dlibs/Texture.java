package gl3dlibs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * Created by ke on 2016/08/16.
 */
//テクスチャ
public class Texture extends GLObject {
    public int textureId;//テクスチャID
    public int width;    //幅
    public int height;   //高さ

    //バインド
    @Override
    public void bind() {
        GL10 gl=GLES.gl;
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D,textureId);
    }

    //アンバインド
    @Override
    public void unbind() {
        GL10 gl=GLES.gl;
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D,0);
    }

    //解放
    @Override
    public void dispose() {
        if (textureId!=0) {
            GLES.gl.glDeleteTextures(0,new int[]{textureId},0);
            textureId=0;
        }
    }

    //テクスチャの生成
    public static Texture createInstance(Bitmap bmp) {
        Texture result=new Texture();
        GL11 gl=GLES.gl;
        int[] bufferIds=new int[1];
        gl.glGenTextures(1,bufferIds,0);
        result.textureId=bufferIds[0];
        result.width=bmp.getWidth();
        result.height=bmp.getHeight();
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D,result.textureId);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D,0,bmp,0);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_NEAREST);
        gl.glBindTexture(GL10.GL_TEXTURE_2D,0);
        return result;
    }

    //テクスチャの生成
    public static Texture createTextureFromAsset(
            String assetFileName) throws IOException {
        InputStream in=GLES.context.getAssets().open(assetFileName);
        Bitmap bmp= BitmapFactory.decodeStream(in);
        Texture result=Texture.createInstance(bmp);
        bmp.recycle();
        in.close();
        return result;
    }
}
