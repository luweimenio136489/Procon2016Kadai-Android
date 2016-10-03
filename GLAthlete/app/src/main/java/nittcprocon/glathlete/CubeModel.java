package nittcprocon.glathlete;

/**
 * 立方体のモデル(デバッグ用)
 */

public class CubeModel extends Model3D {
    CubeModel() {
        float l = 0.5f;
        Vec3f[] v = {
                new Vec3f(-l,  l, -l),
                new Vec3f( l,  l, -l),
                new Vec3f( l, -l, -l),
                new Vec3f(-l, -l, -l),
                new Vec3f(-l,  l,  l),
                new Vec3f( l,  l,  l),
                new Vec3f( l, -l,  l),
                new Vec3f(-l, -l,  l)
        };
        addQuad(new Quad(v[0], v[1], v[2], v[3]));
        addQuad(new Quad(v[0], v[4], v[5], v[1]));
        addQuad(new Quad(v[1], v[5], v[6], v[2]));
        addQuad(new Quad(v[7], v[6], v[2], v[3]));
        addQuad(new Quad(v[0], v[4], v[7], v[3]));
        addQuad(new Quad(v[4], v[5], v[6], v[7]));
        createBuffer();
    }
}
