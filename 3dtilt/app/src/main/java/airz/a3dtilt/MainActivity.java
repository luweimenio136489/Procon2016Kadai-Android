package airz.a3dtilt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 現状OPENGLで端末の傾きor読み込まれたファイルに則って
 * ドロイド君が傾きます．
 * 
 * TODO: ファイル読み込み動的に
 * TODO: 加速度対応
 * TODO: グラフ表示
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private GLSurfaceView glSurfaceView;

    protected final static double RAD2DEG = 180 / Math.PI;
    float[] rotationMatrix = new float[9];
    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];
    float[] attitude = new float[3];
    SensorManager sensorManager;
    public static double roll, azimuth, pitch;
    private Switch isreal;
    private TextView textView;
    private File file;
    private List<SensorData> datas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initSensor();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.gls);
        glSurfaceView.setRenderer(new GLRenderer(this));
        isreal = (Switch) findViewById(R.id.isreal);
        isreal.setChecked(true);
        textView = (TextView) findViewById(R.id.datatext);

        sensorManager.unregisterListener(this);
        file = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/test.txt");
        file.getParentFile().mkdir();


    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //パーミッションの確認
        if (requestCode == 100) {
            //許可をくれるまでパーミッション許可のアラートダイアログを出し続ける
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please allow permission", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    protected void initSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        // アクティビティが不可視状態になったときにリスナーを止める
        super.onStop();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values.clone();
                break;
        }

        if (geomagnetic != null && gravity != null) {
            calcSensorValue();
        }

    }

    public void calcSensorValue() {
        SensorManager.getRotationMatrix(
                rotationMatrix, null,
                gravity, geomagnetic);

        SensorManager.getOrientation(
                rotationMatrix,
                attitude);
        if (isreal.isChecked()) {
            azimuth = (attitude[0] * RAD2DEG) + azimuth;
            pitch = (attitude[1] * RAD2DEG) + pitch;
            roll = (attitude[2] * RAD2DEG) + roll;
            azimuth /= 2;//平滑化
            pitch /= 2;//平滑化
            roll /= 2;//平滑化
            textView.setText("pitch:" + (int) pitch + "\nroll:" + (int) roll + "\nazimuth:" + (int) azimuth);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.load:
                try {
                    String str;
                    datas.clear();
                    SensorData tmpdata;
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    while ((str = br.readLine()) != null) {
                        String[] raw = str.split(",");
                        tmpdata = new SensorData();
                        tmpdata.setTime(Integer.parseInt(raw[0]));
                        tmpdata.setAccel(new float[]{Float.valueOf(raw[1]), Float.valueOf(raw[2]), Float.valueOf(raw[3])});
                        tmpdata.setMagnetic(new float[]{Float.valueOf(raw[4]), Float.valueOf(raw[5]), Float.valueOf(raw[6])});
                        tmpdata.setGyro(new float[]{Float.valueOf(raw[7]), Float.valueOf(raw[8]), Float.valueOf(raw[9])});
                        datas.add(tmpdata);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.startButton:

                final Handler handler = new Handler();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < datas.size(); i++) {

                            try {
                                if (i != 0)
                                    Thread.sleep(datas.get(i - 1).getTime() - datas.get(i).getTime());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            final int finalI = i;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    gravity = datas.get(finalI).getAccel();
                                    geomagnetic = datas.get(finalI).getMagnetic();
                                    calcSensorValue();

                                }
                            });
                        }
                        // マルチスレッドにしたい処理 ここまで
                    }
                }).start();
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
