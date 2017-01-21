package nittcprocon.glathlete;

import android.content.SharedPreferences;
import android.opengl.Matrix;

/**
 * どこから呼んでもいい便利メソッドたち
 * import staticして使う
 */

class UtilsMisc {
    /* SharedPreferencesから値をロード */
    static void loadSharedPreferences(SharedPreferences sharedPreferences, float[] fCenter, float[] rCenter, float[] fLen, float[] rLen) {
        fCenter[0] = sharedPreferences.getFloat("front_center_u", 0.25f);
        fCenter[1] = sharedPreferences.getFloat("front_center_v", 0.4444f);

        rCenter[0] = sharedPreferences.getFloat("rear_center_u", 0.75f);
        rCenter[1] = sharedPreferences.getFloat("rear_center_v", 0.4444f);

        fLen[0] = sharedPreferences.getFloat("front_length_u", (float)(0.25 * 0.9));
        fLen[1] = sharedPreferences.getFloat("front_length_v", (float)(0.4444 * 0.9));

        rLen[0] = sharedPreferences.getFloat("rear_length_u", (float)(0.25 * 0.9));
        rLen[1] = sharedPreferences.getFloat("rear_length_v", (float)(0.4444 * 0.9));
    }

    /* SharedPreferencesに値をセット */
    static void setSharedPreferences(SharedPreferences sharedPreferences, float[] fCenter, float[] rCenter, float[] fLen, float[] rLen) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("front_center_u", fCenter[0]);
        editor.putFloat("front_center_v", fCenter[1]);
        editor.putFloat("rear_center_u", rCenter[0]);
        editor.putFloat("rear_center_v", rCenter[1]);
        editor.putFloat("front_length_u", fLen[0]);
        editor.putFloat("front_length_v", fLen[1]);
        editor.putFloat("rear_length_u", rLen[0]);
        editor.putFloat("rear_length_v", rLen[1]);
        editor.apply();
    }

    /* 3つの行列を一度に掛け算して返す */
    static float[] calcMVP(float[] m, float[] v, float[] p) {
        float[] mv = new float[16];
        Matrix.multiplyMM(mv, 0, v, 0, m, 0);
        float[] mvp = new float[16];
        Matrix.multiplyMM(mvp, 0, p, 0, mv, 0);
        return mvp;
    }

    /* 2要素のfloatベクトルを(x, y)みたいな書式にする */
    static String dump2fv(float[] fv) {
        if (fv == null)
            return "null";

        return "(" + fv[0] + ", " + fv[1] + ")";
    }
}
