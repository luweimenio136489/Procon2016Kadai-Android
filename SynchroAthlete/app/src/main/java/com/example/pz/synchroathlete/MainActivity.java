package com.example.pz.synchroathlete;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ボタンを設定
        Button buttonA = (Button)findViewById(R.id.buttonA);
        Button buttonB = (Button)findViewById(R.id.buttonB);

        // TextView の設定
        textView = (TextView) findViewById(R.id.textViewA);

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

        if (buttonB != null) {
            buttonB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Sdcard.class);
                    startActivity(intent);
                    textView.setText("startActivity done");
                }
            });
        }


    }
}

