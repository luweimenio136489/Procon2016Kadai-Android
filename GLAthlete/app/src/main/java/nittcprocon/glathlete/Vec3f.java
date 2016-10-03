package nittcprocon.glathlete;

import java.util.List;

public class Vec3f {
    public float x;
    public float y;
    public float z;

    Vec3f(float x_, float y_, float z_) {
        this.x = x_;
        this.y = y_;
        this.z = z_;
    }

    public Float[] asArray() {
        return new Float[] {x, y, z};
    }

    public List<Float> asList() {
        return java.util.Arrays.asList(asArray());
    }
}
