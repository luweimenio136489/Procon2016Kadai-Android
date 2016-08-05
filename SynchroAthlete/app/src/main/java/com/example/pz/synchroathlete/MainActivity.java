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
        Button buttonA = (Button)findViewById(R.id.buttonA);
        Button buttonB = (Button)findViewById(R.id.buttonB);

        // TextView の設定
        textView = (TextView) findViewById(R.id.textViewA);

        //SDカードへのpathを準備
        final File file = new File(Environment.getExternalStorageDirectory() + "/SynchroAthlete/test.csv");
        file.getParentFile().mkdir();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("write hedder infomations here.\r\n".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        textView.setText("done");

        // リスナーをボタンに登録
        if (buttonA != null) {
            buttonA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     // flagがtrueの時
                    if (flag) {
                        textView.setText("Hello");
                        flag = false;
                    }
                    // flagがfalseの時
                    else {
                        textView.setText("World");
                        flag = true;
                    }
                }
            });
        }


        buttonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file,true);
                    fos.write("1,2,3,4,5,6\r\n".getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == 100) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,"Hello",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                //拒否された時
            }
        }
    }

}

