package com.example.tukitan.SensorSaver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager sensor_manager;
    public static double Rad2Dec = (double) 180 / Math.PI;
    private float[] accel = new float[3];
    private float[] magnetic = new float[3];
    private float[] gyro = new float[3];
    private double[] attitude = new double[2];
    double gravity;
    File file, dataFile;
    boolean isInited = false;
    private static boolean whiteState = false;
    long startTime;
    TextView sValue,aValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_sensor_value);
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        // ボタンを設定
        ((ImageButton) findViewById(R.id.startButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.initButton)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.stopButton)).setOnClickListener(this);

        sValue = (TextView) findViewById(R.id.sensorValue);
        aValue = (TextView) findViewById(R.id.attitudeVale);

        //SDカードへのpathを準備
        file = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/test.txt");
        dataFile = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/sensorData.txt");
        file.getParentFile().mkdir();
        try {
            //最初の一行目に"write hedder infomations here"と書く
            FileOutputStream fos = new FileOutputStream(file);
            FileOutputStream fos2 = new FileOutputStream(dataFile);
            fos.write("write hedder infomations here.\r\n".getBytes());
            fos2.write("Writing Sensor Data.\r\n".getBytes());
            fos.close();
            fos2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void onResume() {
        super.onResume();

        //センサを取得
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);

    }

    public void onSensorChanged(SensorEvent event) {
        // センサの値を取得
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accel = event.values.clone();
                String viewString = String.format("X axis:%.2f  ", accel[0]) + String.format("Y axis:%.2f  ", accel[2])
                        + String.format("Z axis:%.2f", accel[1]);
                attitude = getAttitude(accel);
                String attitudeString = String.format("X axis:%.3f ,Z axis:%.3f",attitude[0]*Rad2Dec,attitude[1]*Rad2Dec);
                sValue.setText(viewString);
                aValue.setText(attitudeString);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetic = event.values.clone();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyro = event.values.clone();
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        if (sensor_manager != null) {
            sensor_manager.unregisterListener(this);
        }
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //パーミッションの確認
        if (requestCode == 100) {

            //許可をくれるまでパーミッション許可のアラートダイアログを出し続ける
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please allow permission", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                if (isInited) {
                    whiteState = true;
                    startTime = System.currentTimeMillis();
                    (new Thread(new inputData())).start();
                    Toast.makeText(this, "書き込みを開始しました", Toast.LENGTH_SHORT).show();
                    isInited = false;
                } else Toast.makeText(this, "先に初期化をしてください", Toast.LENGTH_SHORT).show();
                break;
            case R.id.initButton:
                Toast.makeText(this, "初期化しました", Toast.LENGTH_SHORT).show();
                isInited = true;
                break;
            case R.id.stopButton:

                whiteState = false;
                Toast.makeText(this, "書き込みを停止しました", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // データをストレージへ出力
    synchronized public void inputText() {
        String getData = System.currentTimeMillis() - startTime
                + accel[0] + "," + accel[1] + "," + accel[2]
                + magnetic[0] + "," + magnetic[1] + "," + magnetic[2]
                + gyro[0] + "," + gyro[1] + "," + gyro[2] + "\r\n";
        String outputAttitude = System.currentTimeMillis() -startTime
                + attitude[0] * Rad2Dec + "," + attitude[1] * Rad2Dec
                + gravity;
        FileOutputStream fos2 = null;
        try {
            fos2 = new FileOutputStream(dataFile, true);
            fos2.write(getData.getBytes());
            fos2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized private double[] getAttitude(float accelData[]) {
        // attitude[0]:X軸に対する回転角　attitude[1]:Y軸に対する回転角
        double attitude[] = new double[2];
        // 無理やり計算しています
        if (9.8<Math.abs(accelData[0])) {
            if(accelData[0]> 9.8) accelData[0] = (float)9.8;
            else accelData[0] = (float)-9.8;
        }
        if (9.8<Math.abs(accelData[1])) {
            if(accelData[1]> 9.8) accelData[1] = (float)9.8;
            else accelData[1] = (float)-9.8;
        }
        attitude[0] = Math.asin(accelData[1]/9.81);
        attitude[1] = Math.asin(accelData[0]/9.81);
        return attitude;
    }
    // データを入力するスレッド
    private class inputData implements Runnable {
        @Override
        public void run() {
            while (whiteState) {
                try {
                    Thread.sleep(16);
                    inputText();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
}
