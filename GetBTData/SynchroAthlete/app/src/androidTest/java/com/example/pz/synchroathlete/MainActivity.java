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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

import java.io.BufferedWriter;
import java.io.File;
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
public class MainActivity extends SampleActivityBase implements SensorEventListener{

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    private SensorManager sensor_manager;
    public TextView acsserelate;
    public TextView magnet;
    public TextView roll, pitch, azimuth;
    public TextView eulerRad;
    public static double Rad2Dec = (double) 180 / Math.PI;
    float[] matrix = new float[9];
    float[] accel = new float[3];
    float[] magnetic = new float[3];
    float rollEuler,pitchEuler,gravity;
    static float[] attitude = new float[3];
    Handler mHandler = new Handler();
    static String inputStream;
    String path = "/data/data/com.example.android.bluetoothchat/files/SensorData.txt";
    String strToTest;
    public File file = new File(path);
    boolean isThreadStart=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

/*        acsserelate = (TextView) findViewById(R.id.text_value);
        magnet = (TextView) findViewById(R.id.value2);
        roll = (TextView) findViewById(R.id.roll);
        pitch = (TextView) findViewById(R.id.pitch);
        azimuth = (TextView) findViewById(R.id.azimuth);
        eulerRad = (TextView) findViewById(R.id.euler);*/
        try {
            FileOutputStream out = openFileOutput("SensorData.txt", MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(out);
            BufferedWriter bw = new BufferedWriter(osw);
            inputStream = "num,0.16,num\n";
            bw.write(inputStream);
            bw.close();
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
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
            if(BluetoothDataForRotate.isMakeRotate()&&!isThreadStart){
                (new Thread(new inputData())).start();
                isThreadStart=true;
            }
            // 加速度センサと磁気センサからX,Y,Z軸に対する傾きを出す
            SensorManager.getRotationMatrix(matrix, null, accel, magnetic);
            SensorManager.getOrientation(matrix, attitude);

            //attitude[0]:azimuth  attitude[1]:pitch  attitude[2]:roll
            //座標系は X:pitch Y:azimuth Z:roll とする

            /*
            roll.setText(String.valueOf("Roll\n" + Integer.toString((int) (attitude[2] * Rad2Dec))));
            pitch.setText(String.valueOf("pitch\n" + Integer.toString((int) (attitude[1] * Rad2Dec))));
            azimuth.setText(String.valueOf("azimuth\n" + Integer.toString((int) (attitude[0] * Rad2Dec))));*/

            /*   BluetoothDataForRotate.javaテスト用コード
            strToTest=Float.toString(accel[0])+","+Float.toString(accel[1])+","+Float.toString(accel[2])+","+Float.toString(magnetic[0])+","+Float.toString(magnetic[1])+","+Float.toString(magnetic[2]);
            DataCast.stringToData(strToTest);
            attitude[2]=DataCast.getEuler("roll");
            attitude[1]=DataCast.getEuler("pitch");*/

/*
            eulerRad.setText(String.valueOf("Euler:\n("
                    + Integer.toString((int) (attitude[2] * Rad2Dec))
                    + "," + Integer.toString((int) (attitude[1] * Rad2Dec))
                    + "," + Integer.toString((int) (attitude[0] * Rad2Dec)) + ")"));*/
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

    public void inputText() {
        try {
            FileOutputStream out = openFileOutput("SensorData.txt", MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(out);
            BufferedWriter bw = new BufferedWriter(osw);
            if(BluetoothDataForRotate.isMakeRotate()) {
                rollEuler = BluetoothDataForRotate.getEuler("roll");
                pitchEuler = BluetoothDataForRotate.getEuler("pitch");
                gravity = BluetoothDataForRotate.getEuler("gravity");
            }
            inputStream = Integer.toString((int) (attitude[0] * Rad2Dec)) +
                    "," + Integer.toString((int) (attitude[1] * Rad2Dec)) +
                    "," + Integer.toString((int) (attitude[2] * Rad2Dec)) +
                    "," + Integer.toString((int) (rollEuler * Rad2Dec))+
                    "," + Integer.toString((int) (pitchEuler * Rad2Dec))+
                    "," + Integer.toString((int)gravity) + "\n";
            bw.write(inputStream);
            bw.close();
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public class inputData implements Runnable{
        @Override
        public void run() {
            while(true) {
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
