package nittcprocon.glathlete;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.max;
import static nittcprocon.glathlete.Types.*;

/**
 * 球のthetaBeginからthetaEndまでの部分のモデルを生成する(は？)
 */

public class PartialSphereModelCreator {
    private static final String TAG = "PartialSphereModelCreator";
    private static final float RADIUS_DEFAULT = 1.0f;
    private static final float STACKS_PER_RADIANS = (float)(24.0 / PI);
    private static final int SLICES_DEFAULT = 24;

    private Model3D partialSphereModel;

    public PartialSphereModelCreator(float thetaBegin, float thetaEnd) {
        // call to this() must be first statement in constructor body だから読みにくくても許して
        this(thetaBegin, thetaEnd, RADIUS_DEFAULT, (int)max(1.0, STACKS_PER_RADIANS * (thetaEnd - thetaBegin)), SLICES_DEFAULT);
    }

    public PartialSphereModelCreator(float thetaBegin, float thetaEnd, float radius, int stacks, int slices) {
        partialSphereModel = new Model3D();
        float theta = thetaEnd - thetaBegin;
        for (int t = 0; t < stacks; t++) {
            float theta1 = thetaBegin + theta * t / stacks;
            float theta2 = thetaBegin + theta * (t + 1) / stacks;
            for (int p = 0; p < slices; p++) {
                float phi1 = (float)PI * 2.0f * p / slices;
                float phi2 = (float)PI * 2.0f * (p + 1) / slices;

                Vec3f v1 = rtp2xyz(radius, theta1, phi1);
                Vec3f v2 = rtp2xyz(radius, theta1, phi2);
                Vec3f v3 = rtp2xyz(radius, theta2, phi2);
                Vec3f v4 = rtp2xyz(radius, theta2, phi1);

                if (theta1 == 0.0f) {                           // top/front cap, v1 == v2
                    partialSphereModel.addTri(new Tri(v2, v3, v4));
                } else if (nearlyEquals(theta2, (float)PI)) {   // bottom/rear cap, v3 == v4
                    partialSphereModel.addTri(new Tri(v1, v2, v3));
                } else {
                    partialSphereModel.addQuad(new Quad(v1, v2, v3, v4));
                }
            }
        }
        partialSphereModel.createBuffer();
    }

    public Model3D getPartialSphereModel() {
        return partialSphereModel;
    }

    // r, θ, φ から Vec3f(x, y, z)に
    private Vec3f rtp2xyz(float r, float t, float p) {
        return new Vec3f(
                (float)(r * sin(t) * cos(p)),
                (float)(r * sin(t) * sin(p)),
                (float)(r * cos(t))
        );
    }

    private boolean nearlyEquals(float a, float b) {
        return abs(a - b) < 0.000001; // この数字は適当
    }
}
