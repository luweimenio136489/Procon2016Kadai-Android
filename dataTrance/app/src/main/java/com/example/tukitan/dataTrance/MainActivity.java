package com.example.tukitan.dataTrance;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager sensor_manager;
    public static double Rad2Dec = (double) 180 / Math.PI;
    private float[] accel = new float[3];
    private double[] attitude = new double[2];
    double gravity;
    boolean isInited = false;
    private static boolean whiteState = false;
    long startTime;
    //TextView sValue,aValue;
    private double[] initalizeAttitude = new double[2];

    DatagramPacket dp;
    DatagramSocket ds;
    String address;
    int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_sensor_value);
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // ボタンを設定
        ((ImageButton) findViewById(R.id.startButton)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.stopButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.initButton)).setOnClickListener(this);
        /*sValue = (TextView) findViewById(R.id.sensorValue);
        aValue = (TextView) findViewById(R.id.attitudeVale);*/

        /*LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        logView = logFragment.getLogView();*/
    }

    protected void onResume() {
        super.onResume();

        //センサを取得
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    public void onSensorChanged(SensorEvent event) {
        // センサの値を取得
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accel = event.values.clone();
                attitude = getAttitude(accel);
                gravity = getGravity(accel, attitude);
                /*String viewString = String.format("X axis:%.2f  ", accel[0]) + String.format("Y axis:%.2f  ", accel[2])
                        + String.format("Z axis:%.2f", accel[1]);
                String attitudeString = String.format("X axis:%.3f ,Z axis:%.3f ,gravity:%.3f",attitude[0]*Rad2Dec,attitude[1]*Rad2Dec,gravity);
                sValue.setText(viewString);
                aValue.setText(attitudeString);*/
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

    @Override
    protected void onDestroy(){
        super.onDestroy();

    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //パーミッションの確認
        if (requestCode ==100) {
            //許可をくれるまでパーミッション許可のアラートダイアログを出し続ける
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please allow permission", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 100);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                if (isInited) {
                    whiteState = true;
                    writeFileInit();
                    startTime = System.currentTimeMillis();
                    (new Thread(new inputData())).start();
                    Toast.makeText(this, "書き込みを開始しました", Toast.LENGTH_SHORT).show();
                    address = ((EditText)findViewById(R.id.addressTxt)).getText().toString();
                    port = Integer.parseInt(((EditText)findViewById(R.id.portTxt)).getText().toString());
                    isInited = false;
                } else Toast.makeText(this, "先に初期化をしてください", Toast.LENGTH_SHORT).show();
                break;

            case R.id.initButton:
                Toast.makeText(this, "初期化しました", Toast.LENGTH_SHORT).show();
                //logHTML(ThetaRequest.getModeRequest(true, settionID), false);
                initalizeAttitude[0] = attitude[0];
                initalizeAttitude[1] = attitude[1];
                isInited = true;
                break;

            case R.id.stopButton:
                whiteState = false;
                Toast.makeText(this, "書き込みを停止しました", Toast.LENGTH_SHORT).show();
                break;

            case R.id.reconnect:
                break;

        }
    }


    synchronized private double[] getAttitude(float accelData[]) {
        // attitude[0]:X軸に対する回転角　attitude[1]:Y軸に対する回転角
        double attitude[] = new double[2];

        attitude[0] = Math.atan(accelData[1]/accelData[2]);
        attitude[1] = Math.atan(accelData[0]/accelData[2]);
        return attitude;
    }

    synchronized  private double getGravity (float[] accelData ,double[] slope){
        double answer;
        double XYvector = Math.sqrt(accelData[0]*accelData[0] + accelData[2]*accelData[2]);
        answer = Math.sqrt(XYvector*XYvector + accelData[1]*accelData[1]);
        answer = answer - 9.8;
        return answer;
    }

    private void writeFileInit(){
        try {
            ds = new DatagramSocket();
            InetAddress host = InetAddress.getByName(address);
            byte[] data = "Send by UDP.\r\n".getBytes();
            dp = new DatagramPacket(data,data.length,host,port);
            ds.send(dp);
            Thread.sleep(16);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NetworkOnMainThreadException e){
            Log.e("Error","接続できませんでした");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // データを出力するスレッド
    private class inputData implements Runnable {
        @Override
        public void run() {
            try {
                ds = new DatagramSocket();
            } catch (NetworkOnMainThreadException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            while (whiteState) {
                String[] str = new String[2];
                for(int i=0;i<2;i++){
                    str[i] = String.format("%.4f",attitude[i] - initalizeAttitude[i]);
                }
                String outputAttitude = System.currentTimeMillis() -startTime + "," +
                        str[0] + "," + str[1] + "," + String.format("%.4f",gravity) + "\r\n";
                try {
                    InetAddress host = InetAddress.getByName(address);
                    byte[] data = outputAttitude.getBytes();
                    dp = new DatagramPacket(data,data.length,host,port);
                    ds.send(dp);
                    Thread.sleep(16);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NetworkOnMainThreadException e){
                    Log.e("Error","接続できませんでした");
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
}
