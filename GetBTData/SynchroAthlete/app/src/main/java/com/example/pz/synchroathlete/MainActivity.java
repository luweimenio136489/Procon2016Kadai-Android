package com.example.pz.synchroathlete;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import static android.content.Context.MODE_APPEND;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private boolean flag = false;
    private BufferedWriter bw = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //パーミッションの許諾
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        // ボタンを設定
        Button startWriting = (Button)findViewById(R.id.startWritingButton);

        //SDカードへのpathを準備
        final File file = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/test.csv");
        file.getParentFile().mkdir();
        try {
            //最初の一行目に"write hedder infomations here"と書く
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("write hedder infomations here.\r\n".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        startWriting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //約60回/1秒で動き続ける
                while (true) {

                    String getData = "ここにデータを受け取ったデータを書いて下さい"+"\r\n";
                    FileOutputStream fos = null;
                    try {

                        fos = new FileOutputStream(file, true);
                        fos.write(getData.getBytes());
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
        });
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //パーミッションの確認
        if (requestCode == 100) {

            //許可をくれるまでパーミッション許可のアラートダイアログを出し続ける
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,"Please allow permission",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

}

