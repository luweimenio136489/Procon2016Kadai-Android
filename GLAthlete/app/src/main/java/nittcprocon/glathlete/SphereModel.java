package nittcprocon.glathlete;

import android.util.Log;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * 球体のモデルを作って保持するクラス
 */

public class SphereModel extends Model3D {
    private static final String TAG = "SphereModel";
    static final int STACKS_DEFAULT = 24;
    static final int SLICES_DEFAULT = 24;
    static final float RADIUS_DEFAULT = 1.0f;

    SphereModel() {
        this(RADIUS_DEFAULT, STACKS_DEFAULT, SLICES_DEFAULT);
    }

    SphereModel(float radius) {
        this(radius, STACKS_DEFAULT, SLICES_DEFAULT);
    }

    SphereModel(float radius, int stacks, int slices) {
        super();
        for (int t = 0; t < stacks; t++) {
            float theta1 = (float)PI * t / stacks;
            float theta2 = (float)PI * (t + 1) / stacks;
            for (int p = 0; p < slices; p++) {
                float phi1 = (float)PI * 2.0f * p / slices;
                float phi2 = (float)PI * 2.0f * (p + 1) / slices;

                Vec3f v1 = rtp2xyz(radius, theta1, phi1);
                Vec3f v2 = rtp2xyz(radius, theta1, phi2);
                Vec3f v3 = rtp2xyz(radius, theta2, phi2);
                Vec3f v4 = rtp2xyz(radius, theta2, phi1);

                if (t == 0) {                       // top cap, v1 == v2
                    addTri(new Tri(v2, v3, v4));
                } else if (t + 1 == stacks) {       // bottom cap, v3 == v4
                    addTri(new Tri(v1, v2, v3));
                } else {
                    addQuad(new Quad(v1, v2, v3, v4));
                }
            }
        }
        Log.d(TAG, "Generated " + vertNum() + " vertices & " + indNum() + " indices");
        this.createBuffer();
    }

    // r, θ, φ から Vec3f(x, y, z)に
    private Vec3f rtp2xyz(float r, float t, float p) {
        return new Vec3f(
                (float)(r * sin(t) * cos(p)),
                (float)(r * sin(t) * sin(p)),
                (float)(r * cos(t))
        );
    }
}
