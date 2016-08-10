/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.example.android.bluetoothchat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class SensorMainActivity extends SampleActivityBase implements SensorEventListener {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    private SensorManager sensor_manager;
    public static double Rad2Dec = (double) 180 / Math.PI;
    float[] matrix = new float[9];
    float[] accel = new float[3];
    float[] magnetic = new float[3];
    float rollEuler,pitchEuler,gravity;
    static float[] attitude = new float[3];
    static String inputStream;
    long startTime;
    boolean writeState=true;

    private ThetaS_Shutter thetaS_shutter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
        //パーミッションの許諾
        ActivityCompat.requestPermissions(SensorMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        // ボタンを設定
        //Button startWriting = (Button)findViewById(R.id.startWritingButton);
        Button stopWriting = (Button)findViewById(R.id.stopWritingButton);
        ImageButton cam_rec =(ImageButton) findViewById(R.id.rec);
        ImageButton cam_modevideo =(ImageButton) findViewById(R.id.modevideo);
        thetaS_shutter = new ThetaS_Shutter();
        //SDカードへのpathを準備
        final File file = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/test.text");
        file.getParentFile().mkdir();
        try {
            //最初の一行目に"write hedder infomations here"と書く
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("write hedder infomations here\r\n".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cam_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BluetoothChatFragment.BT_CONNECT==1) {
                    thetaS_shutter.startcapture();
                    Log.d("State","データ書き込み中");
                    final float[] initAttitude = new float[3];
                    float initRoll = 0;
                    float initPitch = 0;
                    final float initGravity;
                    initAttitude[0] = attitude[0];
                    initAttitude[1] = attitude[1];
                    initAttitude[2] = attitude[2];
                    if (BluetoothDataForRotate.rollCheck()) initRoll = BluetoothDataForRotate.getEuler("roll");
                    if (BluetoothDataForRotate.pitchCheck()) initPitch = BluetoothDataForRotate.getEuler("pitch");
                    initGravity = (float) Math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]);
                    startTime=System.currentTimeMillis();
                    //約60回/1秒で動き続ける
                    final float finalInitRoll = initRoll;
                    final float finalInitPitch = initPitch;

                    (new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (writeState) {

                                if (BluetoothDataForRotate.rollCheck())
                                    rollEuler = BluetoothDataForRotate.getEuler("roll");
                                if (BluetoothDataForRotate.pitchCheck())
                                    pitchEuler = BluetoothDataForRotate.getEuler("pitch");
                                gravity = (float) Math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]);
                                inputStream = System.currentTimeMillis() - startTime +
                                        "," + Integer.toString((int) (initAttitude[0] * Rad2Dec - attitude[0] * Rad2Dec)) +
                                        "," + Integer.toString((int) (initAttitude[1] * Rad2Dec - attitude[1] * Rad2Dec)) +
                                        "," + Integer.toString((int) (initAttitude[2] * Rad2Dec - attitude[2] * Rad2Dec)) +
                                        "," + Integer.toString((int) (finalInitRoll * Rad2Dec - rollEuler * Rad2Dec)) +
                                        "," + Integer.toString((int) (finalInitPitch * Rad2Dec - pitchEuler * Rad2Dec)) +
                                        "," + (initGravity - gravity) + "\n";
                                FileOutputStream fos = null;
                                try {

                                    fos = new FileOutputStream(file, true);
                                    fos.write(inputStream.getBytes());
                                    fos.close();
                                    try {
                                        Thread.sleep(17);

                                        //エラー処理
                                    } catch (InterruptedException e) {
                                    }

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    })).start();
                } else Log.d("State","Not BT Connect");
            }
        });
        stopWriting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("State","ファイル書き込み,撮影停止");
                thetaS_shutter.stopcapture();
                writeState = false;
            }
        });
        cam_modevideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                thetaS_shutter.mode(true);
            }
        });
    }

    public void sendRequest(String args) {
        SendRequestAsync asyncTask = new SendRequestAsync(getApplicationContext(), args);
        asyncTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }
    protected void onResume() {
        super.onResume();

        //センサを取得
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);


    }
    public void onSensorChanged(SensorEvent event) {
        //sleep(16);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accel = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetic = event.values.clone();
                break;
        }

        if (accel != null && magnetic != null) {
            // 加速度センサと磁気センサからX,Y,Z軸に対する傾きを出す
            SensorManager.getRotationMatrix(matrix, null, accel, magnetic);
            SensorManager.getOrientation(matrix, attitude);

            //attitude[0]:azimuth  attitude[1]:pitch  attitude[2]:roll
            //座標系は X:pitch Y:azimuth Z:roll とする

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
                Toast.makeText(SensorMainActivity.this,"Please allow permission",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(SensorMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }
    public class ThetaS_Shutter {
        public ThetaS_Shutter() {
            connect();
        }
        /**
         * セッションを開始。セッションIDを発行する。
         */
        void connect () {
            sendRequest("{\"name\": \"camera.startSession\" ,\"parameters\": {}}");
        }
        /**
         * 静止画撮影を開始する。
         */
        void shutter() {
            sendRequest("{\"name\": \"camera.takePicture\" ,\"parameters\": {\"sessionId\" :\"SID_0001\"}}");
        }


        /**
         * v2.0
         * 連続撮影を開始する。
         */
        void startcapture() {
            sendRequest("{\"name\": \"camera._startCapture\" ,\"parameters\": {\"sessionId\" :\"SID_0001\"}}");
        }

        /**
         * v2.0
         * 連続撮影を停止する。
         */
        void stopcapture() {
            sendRequest("{\"name\": \"camera._stopCapture\" ,\"parameters\": {\"sessionId\" :\"SID_0001\"}}");
        }

        /**
         * 撮影モード指定
         *
         * @param mode true:動画 false:画像
         */
        void mode(boolean mode) {
            String value;
            if (mode == true) {
                value = "_video";
            } else {
                value = "image";
            }

            option("captureMode", value);
        }

        private void option(String option_name, String option_value) {
            sendRequest("{\"name\": \"camera.setOptions\" ,\"parameters\": {\"sessionId\" :\"SID_0001\", \"options\": {\"" + option_name + "\": \"" + option_value + "\"}}}");
        }


    }

}
