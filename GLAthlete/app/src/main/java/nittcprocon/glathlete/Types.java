package nittcprocon.glathlete;

import java.util.List;

/**
 * わざわざ自分で作りたくないシリーズ
 * static importを乱用していくスタイル
 */

class Types {
    // float2要素のベクトル
    public static class Vec2f {
        float x;
        float y;

        Vec2f(float x_, float y_) {
            this.x = x_;
            this.y = y_;
        }

        Float[] asArray() {
            return new Float[] {x, y};
        }

        List<Float> asList() {
            return java.util.Arrays.asList(asArray());
        }
    }

    // float3要素のベクトル
    static class Vec3f {
        float x;
        float y;
        float z;

        Vec3f(float x_, float y_, float z_) {
            this.x = x_;
            this.y = y_;
            this.z = z_;
        }

        Float[] asArray() {
            return new Float[] {x, y, z};
        }

        List<Float> asList() {
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
    static class Tri {
        Vec3f a;
        Vec3f b;
        Vec3f c;

        Tri(Vec3f a, Vec3f b, Vec3f c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        Vec3f[] asArray() {
            return new Vec3f[] {a, b, c};
        }
        List<Vec3f> asList() {
            return java.util.Arrays.asList(asArray());
        }
    }

    // 4頂点からなる四角形
    // a, b, c, d の順に右回り
    static class Quad {
        Vec3f a;
        Vec3f b;
        Vec3f c;
        Vec3f d;

        Quad(Vec3f a, Vec3f b, Vec3f c, Vec3f d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        Vec3f[] asArray() {
            return new Vec3f[] {a, b, c, d};
        }
        List<Vec3f> asList() {
            return java.util.Arrays.asList(asArray());
        }
    }
}
