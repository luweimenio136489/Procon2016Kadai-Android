package nittcprocon.thetasremote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.os.AsyncTask;
import android.widget.ImageButton;

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
}

class ThetaS_Shutter {

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

    public static void option(String option_name, String option_value) {
        sendRequest("{\"name\": \"camera.setOptions\" ,\"parameters\": {\"sessionId\" :\"SID_0001\", \"options\": {\"" + option_name + "\": \"" + option_value + "\"}}}");
    }

    public static void sendRequest(final String payload) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = null;

                // リクエストボディを作る
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"), payload
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
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 返す
                return result;
            }

        }.execute();
    }
}
