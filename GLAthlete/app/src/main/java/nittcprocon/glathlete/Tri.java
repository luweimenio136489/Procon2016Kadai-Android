package nittcprocon.glathlete;

import java.util.List;

public class Tri {
    public Vec3f a;
    public Vec3f b;
    public Vec3f c;

    Tri(Vec3f a_, Vec3f b_, Vec3f c_) {
        this.a = a_;
        this.b = b_;
        this.c = c_;
    }

    public Vec3f[] asArray() {
        return new Vec3f[] {a, b, c};
    }
    public List<Vec3f> asList() {
        return java.util.Arrays.asList(asArray());
    }
}
