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
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
    private float[] accel = new float[3];
    private double[] attitude = new double[2];
    double gravity;
    boolean debugState=false;
    private static boolean whiteState = false;
    long startTime;
    //TextView sValue,aValue;
    TextView state,xAccel,yAccel,zAccel,xAttitude,zAttitude,Grav;
    EditText counter;
    int time;
    private double[] initalizeAttitude = new double[2];
    DatagramPacket dp;
    DatagramSocket ds;
    String address;
    int port;
    CountDown countdown;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_sensor_value);
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // ボタンを設定
        ((Button) findViewById(R.id.stopButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.initButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.debug)).setOnClickListener(this);
        state = (TextView) findViewById(R.id.genzai);
        counter = (EditText) findViewById(R.id.countTimer);

        xAccel = (TextView) findViewById(R.id.xAcc);
        yAccel = (TextView) findViewById(R.id.yAcc);
        zAccel = (TextView) findViewById(R.id.zAcc);
        xAttitude = (TextView) findViewById(R.id.xAtti);
        zAttitude = (TextView) findViewById(R.id.yAtti);
        Grav = (TextView) findViewById(R.id.grav);
        //androidがスリープモードにならないように
        debugModeChange(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
                gravity = getGravity(accel);
                if(debugState) {
                    xAccel.setText("X軸加速度："+Float.toString(accel[0]));
                    yAccel.setText("Y軸加速度："+Float.toString(accel[2]));
                    zAccel.setText("Z軸加速度："+Float.toString(accel[1]));
                    xAttitude.setText("X軸の傾き："+Double.toString(attitude[0]));
                    zAttitude.setText("Z軸の傾き："+Double.toString(attitude[1]));
                    Grav.setText("重力方向加速度："+Double.toString(gravity));
                }
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

    // それぞれのボタンのコールバック関数
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.initButton:
                address = ((EditText)findViewById(R.id.addressTxt)).getText().toString();
                port = Integer.parseInt(((EditText)findViewById(R.id.portTxt)).getText().toString());
                time = (counter != null) ? Integer.parseInt(counter.getText().toString()) : null ;
                countdown = new CountDown(time*1000,1000);
                countdown.start();
                state.setText("現在の状態：初期化中");
                break;

            case R.id.stopButton:
                whiteState = false;
                if(countdown != null) countdown.cancel();
                Toast.makeText(this, "書き込みを停止しました", Toast.LENGTH_SHORT).show();
                state.setText("現在の状態：未初期化");
                counter.setText(Integer.toString(10));
                break;
            case R.id.debug:
                if (debugState) debugState = false;
                else debugState = true;
                debugModeChange(debugState);
        }
    }

    //加速度から傾きを求める関数
    synchronized private double[] getAttitude(float accelData[]) {
        // attitude[0]:X軸に対する回転角　attitude[1]:Z軸に対する回転角
        //accelData[0]:Xaccel accelData[1]:Zaccel accelData[2]:Yaccel
        double attitude[] = new double[2];

        attitude[0] = Math.atan2(accelData[2],accelData[1]);
        attitude[1] = Math.atan2(accelData[0],accelData[1]);
        return attitude;
    }
    //加速度から重直方向への加速度を求める関数
    synchronized  private double getGravity (float[] accelData){
        double answer;
        double XYvector = Math.sqrt(accelData[0]*accelData[0] + accelData[2]*accelData[2]);
        answer = Math.sqrt(XYvector*XYvector + accelData[1]*accelData[1]);
        answer = answer - 9.8;
        return answer;
    }


    /*
    デバッグモードでの表示/非表示
    state=true :表示
    state = false :非表示
     */
    private void debugModeChange(boolean state){
        if(state){
            xAccel.setVisibility(View.VISIBLE);
            yAccel.setVisibility(View.VISIBLE);
            zAccel.setVisibility(View.VISIBLE);
            xAttitude.setVisibility(View.VISIBLE);
            zAttitude.setVisibility(View.VISIBLE);
            Grav.setVisibility(View.VISIBLE);
        } else {
            xAccel.setVisibility(View.INVISIBLE);
            yAccel.setVisibility(View.INVISIBLE);
            zAccel.setVisibility(View.INVISIBLE);
            xAttitude.setVisibility(View.INVISIBLE);
            zAttitude.setVisibility(View.INVISIBLE);
            Grav.setVisibility(View.INVISIBLE);
        }
    }


    //初期化までのカウントダウンスレッド
    private class CountDown extends CountDownTimer {
        public CountDown(long millsec,long interval){
            super(millsec,interval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            time -= 1;
            counter.setText(Integer.toString(time));
        }

        @Override
        public void onFinish() {
            whiteState = true;
            counter.setText("0");
            state.setText("現在の状態：送信中");
            startTime = System.currentTimeMillis();
            initalizeAttitude[0] = attitude[0];
            initalizeAttitude[1] = attitude[1];
            Log.d("message", "Called onFinish()");
            (new Thread(new inputData())).start();
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
                double tmp;

                for(int i=0;i<2;i++){
                    if((initalizeAttitude[i] - attitude[i]) > Math.PI) tmp = initalizeAttitude[i] - attitude[i] - Math.PI;
                    else if((initalizeAttitude[i] - attitude[i]) < -Math.PI) tmp = initalizeAttitude[i] - attitude[i] + Math.PI;
                    else tmp = initalizeAttitude[i] - attitude[i];
                    str[i] = String.format("%.4f",tmp);
                }
                String outputAttitude = System.currentTimeMillis() -startTime + "," +
                        str[0] + "," + str[1] + "," + String.format("%.4f",gravity) + "\r\n";

                /*送ってるデータを確認するときは下のコメントを外して下さい*/
                //System.out.println(outputAttitude);

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
            try {
                InetAddress host = InetAddress.getByName(address);
                byte[] data = "exit".getBytes();
                dp = new DatagramPacket(data,data.length,host,port);
                ds.send(dp);
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