package nittcprocon.glathlete;

import android.util.Log;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static nittcprocon.glathlete.Types.*;

public class SphereModelCreator {
    private static final String TAG = "SphereModelCreator";
    private static final float RADIUS_DEFAULT = 1.0f;
    private static final int STACKS_DEFAULT = 24, SLICES_DEFAULT = 24;

    private Model3D sphereModel;

    public SphereModelCreator() {
        this(RADIUS_DEFAULT, STACKS_DEFAULT, SLICES_DEFAULT);
    }

    public SphereModelCreator(float radius, int stacks, int slices) {
        sphereModel = new Model3D();
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
                    sphereModel.addTri(new Tri(v2, v3, v4));
                } else if (t + 1 == stacks) {       // bottom cap, v3 == v4
                    sphereModel.addTri(new Tri(v1, v2, v3));
                } else {
                    sphereModel.addQuad(new Quad(v1, v2, v3, v4));
                }
            }
        }
        sphereModel.createBuffer();
    }

    public Model3D getSphereModel() {
        return sphereModel;
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
