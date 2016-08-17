package airz.a3dtilt;

/**
 * Created by ke on 2016/08/17.
 * 自動生成
 */
public class SensorData {
    private float[] accel = new float[3];
    private float[] magnetic = new float[3];
    private float[] gyro = new float[3];
    private int time;

    public void setTime(int time) {
        this.time = time;
    }

    public void setAccel(float[] accel) {
        this.accel = accel;
    }

    public void setGyro(float[] gyro) {
        this.gyro = gyro;
    }

    public void setMagnetic(float[] magnetic) {
        this.magnetic = magnetic;
    }

    public float[] getAccel() {
        return accel;
    }

    public float[] getGyro() {
        return gyro;
    }

    public float[] getMagnetic() {
        return magnetic;
    }

    public int getTime() {
        return time;
    }
}
