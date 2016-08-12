package nittcprocon.thetasremote;

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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String settionID = "SID_0001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendRequest(TheataRequest.getConnectRequest());
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
                sendRequest(TheataRequest.getShutterRequest(settionID));
                break;
            case R.id.modevideo:
                sendRequest(TheataRequest.getModeRequest(true, settionID));
                break;
            case R.id.modecamera:
                sendRequest(TheataRequest.getModeRequest(false, settionID));
                break;
            case R.id.rec:
                sendRequest(TheataRequest.getStartcaptureRequest(settionID));
                break;
            case R.id.stop:
                sendRequest(TheataRequest.getStopcaptureRequest(settionID));
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

        /**
         * CODE:{"name":"camera.startSession","state":"done","results":{ "sessionId":"SID_0002","timeout":180}}
         * CODE:{"name":"camera.setOptions","state":"error","error":{"code":"invalidSessionId","message":"The sessionId is invalid."}}
         *
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (isSessionId(s)) {
                    JSONObject jsonObject = new JSONObject(s);
                    settionID = jsonObject.getString("sessionId");
                } else if (isInvalidSessionId(s)) {
                    refleshSession();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Toast.makeText(context_, s, Toast.LENGTH_SHORT).show();
            Log.i("result code ", "CODE:" + s);
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

    private class timeKeeper extends Thread {
        public static final long SLEEP_TIME_SECONDS = 120;

        public timeKeeper() {
            try {
                Thread.sleep(1000 * SLEEP_TIME_SECONDS);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void refleshSession() {
        sendRequest(TheataRequest.getConnectRequest());
    }


}