package com.example.tukitan.SensorSaver;

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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
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
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager sensor_manager;
    public static double Rad2Dec = (double) 180 / Math.PI;
    private float[] accel = new float[3];
    private float[] magnetic = new float[3];
    private float[] gyro = new float[3];
    private double[] attitude = new double[2];
    private int playCount = 0;
    double gravity;
    File file, dataFile;
    String soundFile;
    boolean isInited = false;
    private static boolean whiteState = false;
    long startTime;
    //TextView sValue,aValue;
    private LogView logView; //ログ
    private static String settionID = "SID_0000"; //http通信側のsessionID
    public static final long SLEEP_TIME_SECONDS = 120; ///sessionIDの更新間隔
    private double[] initalizeAttitude = new double[2];

    View surface;
    Calendar calender;

    private static final int SAMPLE_RATE = 22050;
    private static final int BITRATE = 128000;


    private MediaRecorder mediarecorder; //録音用のメディアレコーダークラス


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_sensor_value);
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 100);
        // ボタンを設定
        ((ImageButton) findViewById(R.id.startButton)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.stopButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.initButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.reconnect)).setOnClickListener(this);
        surface = findViewById(R.id.isConnect);
        /*sValue = (TextView) findViewById(R.id.sensorValue);
        aValue = (TextView) findViewById(R.id.attitudeVale);*/

        /*LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        logView = logFragment.getLogView();*/

        calender = Calendar.getInstance();
        autoRefreshSettion();

    }
    /**
     * セッションの自動更新
     * SLEEP_TIME_SESSIONで秒設定
     */
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
    /**
     * セッションのリフレッシュ
     */
    public void refleshSession() {
        //logHTML("<font color=\"Green\">" + ThetaRequest.getConnectRequest() + "</font>", false);
        sendRequest(ThetaRequest.getConnectRequest());
    }
    /**
     * htmlタグを使ってログを履く
     *
     */
    /*public void logHTML(String text, boolean isclear) {

        if (isclear) {
            logView.setText("");
            logView.append(Html.fromHtml("<br>" + text));
        } else {
            logView.append(Html.fromHtml("<br>" + text));
        }
    }*/
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
                attitude = getAttitude(accel);
                gravity = getGravity(accel,attitude);
                /*String viewString = String.format("X axis:%.2f  ", accel[0]) + String.format("Y axis:%.2f  ", accel[2])
                        + String.format("Z axis:%.2f", accel[1]);
                String attitudeString = String.format("X axis:%.3f ,Z axis:%.3f ,gravity:%.3f",attitude[0]*Rad2Dec,attitude[1]*Rad2Dec,gravity);
                sValue.setText(viewString);
                aValue.setText(attitudeString);*/
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
                    sendRequest(ThetaRequest.getStartcaptureRequest(settionID));
                    whiteState = true;
                    writeFileInit();
                    startTime = System.currentTimeMillis();
                    (new Thread(new inputData())).start();
                    Toast.makeText(this, "書き込みを開始しました", Toast.LENGTH_SHORT).show();
                    startMediaRecord();
                    isInited = false;
                } else Toast.makeText(this, "先に初期化をしてください", Toast.LENGTH_SHORT).show();
                break;

            case R.id.initButton:
                Toast.makeText(this, "初期化しました", Toast.LENGTH_SHORT).show();
                //logHTML(ThetaRequest.getModeRequest(true, settionID), false);
                sendRequest(ThetaRequest.getModeRequest(true, settionID));
                initalizeAttitude[0] = attitude[0];
                initalizeAttitude[1] = attitude[1];
                isInited = true;
                break;

            case R.id.stopButton:
                sendRequest(ThetaRequest.getStopcaptureRequest(settionID));
                whiteState = false;
                Toast.makeText(this, "書き込みを停止しました", Toast.LENGTH_SHORT).show();
                stopRecord();
                break;

            case R.id.reconnect:
                refleshSession();
                break;

        }
    }

    // データをストレージへ出力
    synchronized public void inputText() {
        String[] str = new String[2];
        for(int i=0;i<2;i++){
            str[i] = String.format("%.4f",attitude[i] - initalizeAttitude[i]);
        }
        String now = "_" + (calender.get(Calendar.MONTH) +1) + "_" + calender.get(Calendar.DAY_OF_MONTH) + "_"
                + calender.get(Calendar.HOUR_OF_DAY) + "_" +calender.get(Calendar.MINUTE);
        file = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/attitude" + now + ".txt");
        dataFile = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/sensorData" + now + ".txt");

        String getData = System.currentTimeMillis() - startTime + "," +
                String.format("%.5f",accel[0]) + "," + String.format("%.5f",accel[1]) + "," + String.format("%.5f",accel[2]) + "," +
                String.format("%.5f",magnetic[0]) + "," + String.format("%.5f",magnetic[1]) + "," + String.format("%.5f",magnetic[2]) + "," +
                String.format("%.5f",gyro[0]) + "," + String.format("%.5f",gyro[1]) + "," + String.format("%.5f",gyro[2]) + "\r\n";
        ;
        String outputAttitude = System.currentTimeMillis() -startTime + "," +
                str[0] + "," + str[1] + "," + String.format("%.4f",gravity) + "\r\n";
        FileOutputStream fos = null;
        FileOutputStream fos2 = null;
        try {
            fos = new FileOutputStream(file, true);
            fos.write(outputAttitude.getBytes());
            fos.close();
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
            //最初の一行目に"write hedder infomations here"と書く
            /*
             * fos:傾きを記録するファイル
             * fos2:9軸センサのデータをそのまま保存するファイル
             */
            String now = "_" + (calender.get(Calendar.MONTH) + 1) + "_" + calender.get(Calendar.DAY_OF_MONTH) + "_"
                    + calender.get(Calendar.HOUR_OF_DAY) + "_" +calender.get(Calendar.MINUTE);
            file = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/attitude" + now + ".txt");
            dataFile = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/sensorData" + now + ".txt");

            //SDカードへのpathを準備
            file.getParentFile().mkdir();

            FileOutputStream fos = new FileOutputStream(file);
            FileOutputStream fos2 = new FileOutputStream(dataFile);
            fos.write("DataNum,0.016,offset.\r\n".getBytes());
            fos2.write("Time,Xaccel,Yaccel,Zaccel,Xmagnetic,Ymagnetic,Zmegnetic,Xgyro,Ygyro,Zgyro .\r\n".getBytes());
            fos.close();
            fos2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMediaRecord(){
        try{
            String now = "_" + (calender.get(Calendar.MONTH) + 1) + "_" + calender.get(Calendar.DAY_OF_MONTH) + "_"
                    + calender.get(Calendar.HOUR_OF_DAY) + "_" +calender.get(Calendar.MINUTE);
            String filePath = Environment.getExternalStorageDirectory() + "/SynchroAthlete/soundData"
                    + now +".wav"; //録音用のファイルパス
            File mediafile = new File(filePath);
            if(mediafile.exists()) {
                //ファイルが存在する場合は削除する
                mediafile.delete();
            }
            mediafile = null;
            mediarecorder = new MediaRecorder();
            //音声のサンプリング周波数
            mediarecorder.setAudioSamplingRate(SAMPLE_RATE);
            //マイクからの音声を録音する
            mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //ファイルへの出力フォーマット DEFAULTにするとwavが扱えるはず
            mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            //音声のエンコーダーも合わせてdefaultにする
            mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            //エンコーディングのビットレートを指定
            mediarecorder.setAudioEncodingBitRate(BITRATE);
            //ファイルの保存先を指定
            mediarecorder.setOutputFile(filePath);
            //録音の準備をする
            mediarecorder.prepare();
            //録音開始
            mediarecorder.start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void stopRecord(){
        if(mediarecorder == null){
            Toast.makeText(MainActivity.this, "mediarecorder = null", Toast.LENGTH_SHORT).show();
        }else{
            try{
                //録音停止
                mediarecorder.stop();
                mediarecorder.reset();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // データを出力するスレッド
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
    /**
     * HTTP通信を行うクラス
     */
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
                return "CATCH ERROR::" + e.getMessage();
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
                    //logHTML("<font color=\"Green\">" + s + "</font>", false);
                    settionID = jsonObject.getJSONObject("results").getString("sessionId");
                    //logHTML("セッション更新", false);
                    surface.setBackgroundColor(Color.GREEN);
                } else if (isInvalidSessionId(s)) {
                    //logHTML("<font color=\"Red\">" + s + "</font>", false);
                    //logHTML("<font color=\"Red\">" + "セッションの更新します" + "</font>", false);
                    refleshSession();
                } else if (s.contains("CATCH ERROR")) {
                    //logHTML("<font color=\"Red\">" + s + "</font>", false);
                    surface.setBackgroundColor(Color.RED);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * 無効なセッションIDが含まれているか
         *
         * @param data
         * @return
         */
        public boolean isInvalidSessionId(String data) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (jsonObject.getJSONObject("error").getString("code") == "invalidSessionId")
                    return true;
            } catch (JSONException e) {
                return false;
            }
            return false;
        }

        /**
         * セッションIDが含まれているかどうか
         *
         * @param data
         * @return
         */
        public boolean isSessionId(String data) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (jsonObject.getJSONObject("results").getString("sessionId") != null)
                    return true;
            } catch (JSONException e) {
                return false;
            }
            return false;
        }
    }
}
