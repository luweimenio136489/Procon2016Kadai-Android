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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ThetaS_Shutter thetas_shutter = new ThetaS_Shutter();
        thetas_shutter.connect();

        ImageButton shutter = (ImageButton)findViewById(R.id.shutter);
        assert shutter != null;
        shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                thetas_shutter.shutter();

            }
        });

        ImageButton modevideo = (ImageButton)findViewById(R.id.modevideo);
        assert modevideo != null;
        modevideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                thetas_shutter.mode(true);

            }
        });

        ImageButton modecamera = (ImageButton)findViewById(R.id.modecamera);
        assert  modecamera != null;
        modecamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                thetas_shutter.mode(false);

            }
        });

        ImageButton rec = (ImageButton)findViewById(R.id.rec);
        assert rec != null;
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                thetas_shutter.startcapture();

            }
        });

        ImageButton stop = (ImageButton)findViewById(R.id.stop);
        assert stop != null;
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                thetas_shutter.stopcapture();

            }
        });


    }




}

class ThetaS_Shutter{

    void connect(){
        run("{\"name\": \"camera.startSession\" ,\"parameters\": {}}");
    }

    void shutter(){
        run("{\"name\": \"camera.takePicture\" ,\"parameters\": {\"sessionId\" :\"SID_0001\"}}");
    }

    void startcapture(){
        run("{\"name\": \"camera._startCapture\" ,\"parameters\": {\"sessionId\" :\"SID_0001\"}}");
    }

    void stopcapture(){
        run("{\"name\": \"camera._stopCapture\" ,\"parameters\": {\"sessionId\" :\"SID_0001\"}}");
    }

    void mode(boolean mode){
        String value;
        if (mode == true){
            value = "_video";
        }else {
            value = "image";
        }

        option("captureMode", value);
    }

    public static void option(String option_name, String option_value){
        run("{\"name\": \"camera.setOptions\" ,\"parameters\": {\"sessionId\" :\"SID_0001\", \"options\": {\"" + option_name + "\": \"" + option_value + "\"}}}");
    }

    public static void run(final String payload){
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


