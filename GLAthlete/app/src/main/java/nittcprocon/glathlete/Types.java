package nittcprocon.glathlete;

import java.util.List;

/**
 * わざわざ自分で作りたくないシリーズ
 * static importを乱用していくスタイル
 */

public class Types {
    // float2要素のベクトル
    public static class Vec2f {
        public float x;
        public float y;

        Vec2f(float x_, float y_) {
            this.x = x_;
            this.y = y_;
        }

        public Float[] asArray() {
            return new Float[] {x, y};
        }

        public List<Float> asList() {
            return java.util.Arrays.asList(asArray());
        }
    }

    // float3要素のベクトル
    public static class Vec3f {
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

        // dedupに使う
        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (this == obj)
                return true;

            if (getClass() == obj.getClass()) {
                Vec3f other = (Vec3f) obj;
                if (x == other.x && y == other.y && z == other.z)
                    return true;
            }

            return false;
        }
    }

    // 3頂点からなる三角形
    // a, b, c の順に右回り
    public static class Tri {
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

    // 4頂点からなる四角形
    // a, b, c, d の順に右回り
    public static class Quad {
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
}
