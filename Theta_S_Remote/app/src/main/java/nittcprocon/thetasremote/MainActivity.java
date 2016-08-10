package nittcprocon.thetasremote;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.AsyncTask;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ThetaS_Shutter thetaS_shutter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thetaS_shutter = new ThetaS_Shutter();
        thetaS_shutter.connect();
        ((ImageButton) findViewById(R.id.shutter)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.modevideo)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.modecamera)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.rec)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.stop)).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shutter:
                thetaS_shutter.shutter();
                break;
            case R.id.modevideo:
                thetaS_shutter.mode(true);
                break;
            case R.id.modecamera:
                thetaS_shutter.mode(false);
                break;
            case R.id.rec:
                thetaS_shutter.startcapture();
                break;
            case R.id.stop:
                thetaS_shutter.stopcapture();
                break;
        }
    }


    public void sendRequest(String args) {
        SendRequestAsync asyncTask = new SendRequestAsync(getApplicationContext(), args);
        asyncTask.execute();
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
            Toast.makeText(context_, s, Toast.LENGTH_SHORT).show();
            Log.i("result code ", "CODE:" + s);
        }

    }

    public class ThetaS_Shutter {

        /**
         * セッションを開始。セッションIDを発行する。
         */
        void connect() {
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