package com.example.android.bluetoothchat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by tukitan on 16/08/10.
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