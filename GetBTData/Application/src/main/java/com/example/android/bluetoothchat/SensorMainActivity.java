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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 不要なコードは消す！
 * 簡潔に見やすくする！
 */
public class SensorMainActivity extends SampleActivityBase implements SensorEventListener, View.OnClickListener {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    private SensorManager sensor_manager;
    public static double Rad2Dec = (double) 180 / Math.PI;
    float[] matrix = new float[9];
    float[] accel = new float[3];
    float[] magnetic = new float[3];
    float rollEuler, pitchEuler, gravity;
    static float[] attitude = new float[3];
    static String inputStream;
    long startTime;
    boolean writeState = true;

    private static String settionID = "SID_0001";
    public static final long SLEEP_TIME_SECONDS = 120;

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
        ((Button) findViewById(R.id.stopWritingButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.clearlog)).setOnClickListener(this);
        ((Button) findViewById(R.id.reconnect)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.rec)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.modevideo)).setOnClickListener(this);
        autoRefreshSettion();

    }

    public void autoRefreshSettion() {
        refleshSession();
        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // マルチスレッドにしたい処理 ここから
                    try {
                        Thread.sleep(1000 * SLEEP_TIME_SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            refleshSession();
                        }
                    });
                }
                // マルチスレッドにしたい処理 ここまで
            }
        }).start();
    }

    /**
     * リクエスト送信
     *
     * @param args
     */
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
        switch (item.getItemId()) {
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

    /**
     * Create a chain of targets that will receive log data
     */
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
                Toast.makeText(SensorMainActivity.this, "Please allow permission", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(SensorMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    /**
     * htmlタグを使ってログを履く
     *
     * @param text
     * @param isclear
     */
    public void logHTML(String text, boolean isclear) {
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        if (isclear) {
            logFragment.getLogView().setText("");
            logFragment.getLogView().append(Html.fromHtml("<br>" + text));
        } else {
            logFragment.getLogView().append(Html.fromHtml("<br>" + text));
        }
    }


    public void refleshSession() {
        logHTML("<font color=\"Green\">" + ThetaRequest.getConnectRequest() + "</font>", false);
        sendRequest(ThetaRequest.getConnectRequest());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rec:
                //コード長すぎ。
                if (BluetoothChatFragment.BT_CONNECT == 1) {
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

                    sendRequest(ThetaRequest.getStartcaptureRequest(settionID));
                    Log.d("State", "データ書き込み中");
                    final float[] initAttitude = new float[3];
                    float initRoll = 0;
                    float initPitch = 0;
                    final float initGravity;
                    initAttitude[0] = attitude[0];
                    initAttitude[1] = attitude[1];
                    initAttitude[2] = attitude[2];
                    if (BluetoothDataForRotate.rollCheck())
                        initRoll = BluetoothDataForRotate.getEuler("roll");
                    if (BluetoothDataForRotate.pitchCheck())
                        initPitch = BluetoothDataForRotate.getEuler("pitch");
                    initGravity = (float) Math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]);
                    startTime = System.currentTimeMillis();
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
                } else Log.d("State", "Not BT Connect");

            case R.id.stopWritingButton:
                Log.d("State", "ファイル書き込み,撮影停止");
                sendRequest(ThetaRequest.getStopcaptureRequest(settionID));
                writeState = false;
                break;

            case R.id.modevideo:
                logHTML(ThetaRequest.getModeRequest(true, settionID), false);
                sendRequest(ThetaRequest.getModeRequest(true, settionID));
                break;
            case R.id.clearlog:
                logHTML("", true);
                break;
            case R.id.reconnect:
                refleshSession();
                break;
        }
    }

    public class SendRequestAsync extends AsyncTask<Void, Void, String> {
        String payload_;
        Context context_;

        public SendRequestAsync(Context context, String payload) {
            context_ = context;
            payload_ = payload;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            // リクエストボディを作る
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), payload_
            );

            // リクエストオブジェクトを作って
            Request request = new Request.Builder()
                    .url("http://192.168.1.1/osc/commands/execute")
                    .post(requestBody)
                    .build();
            // クライアントオブジェクトを作って
            OkHttpClient client = new OkHttpClient();
            // リクエストして結果を受け取って
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
            // 返す
//                return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (isSessionId(s)) {
                    JSONObject jsonObject = new JSONObject(s);
                    logHTML("<font color=\"Green\">" + s + "</font>", false);
                    settionID = jsonObject.getString("sessionId");
                    logHTML("セッション更新", false);
                } else if (isInvalidSessionId(s)) {
                    logHTML("<font color=\"Red\">" + s + "</font>", false);
                    logHTML("<font color=\"Red\">" + "セッションの更新します" + "</font>", false);
                    refleshSession();
                } else if (s.contains("CATCH ERROR::")) {
                    logHTML("<font color=\"Red\">" + s + "</font>", false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            Toast.makeText(context_, s, Toast.LENGTH_SHORT).show();
//            Log.i("result code ", "CODE:" + s);
        }


        public boolean isInvalidSessionId(String data) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (jsonObject.getJSONObject("error").getString("code") == "invalidSessionId")
                    return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        public boolean isSessionId(String data) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (jsonObject.getString("sessionId") != null)
                    return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }
    }
}