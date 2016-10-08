package nittcprocon.glathlete;

import java.util.List;

/**
 * 4頂点からなる四角形
 * a, b, c, d の順に右回り
 */

public class Quad {
    public Vec3f a;
    public Vec3f b;
    public Vec3f c;
    public Vec3f d;

    Quad(Vec3f a_, Vec3f b_, Vec3f c_, Vec3f d_) {
        this.a = a_;
        this.b = b_;
        this.c = c_;
        this.d = d_;
    }

    public Vec3f[] asArray() {
        return new Vec3f[] {a, b, c, d};
    }
    public List<Vec3f> asList() {
        return java.util.Arrays.asList(asArray());
    }
}
