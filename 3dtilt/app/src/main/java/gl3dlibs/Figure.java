package gl3dlibs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ke on 2016/08/16.
 */
//フィギュア
public class Figure {
    public HashMap<String,Material> materials;//マテリアル群
    public ArrayList<Mesh> meshs;    //メッシュ群

    //描画
    public void draw() {
        for (Mesh mesh:meshs) mesh.draw();
    }
}