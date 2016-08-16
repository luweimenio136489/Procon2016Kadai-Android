package gl3dlibs;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ke on 2016/08/16.
 */
//3Dオブジェクト
public class Object3D {
    public Figure  figure;                     //フィギュア
    public Vector3 position=new Vector3();     //位置
    public Vector3 rotate  =new Vector3();     //回転
    public Vector3 scale   =new Vector3(1,1,1);//拡縮
    public ArrayList<Object3D> childs=new ArrayList<Object3D>();//子

    //描画
    public void draw() {
        GL10 gl=GLES.gl;
        gl.glPushMatrix();
        gl.glTranslatef(position.x,position.y,position.z);
        gl.glRotatef(rotate.z,0,0,1);
        gl.glRotatef(rotate.y,0,1,0);
        gl.glRotatef(rotate.x,1,0,0);
        gl.glScalef(scale.x,scale.y,scale.z);
        figure.draw();
        for (int i=0;i<childs.size();i++) childs.get(i).draw();
        gl.glPopMatrix();
    }
}