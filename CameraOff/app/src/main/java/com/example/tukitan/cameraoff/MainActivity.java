package com.example.tukitan.cameraoff;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    DevicePolicyManager mDPM;
    ComponentName mLockReciever;
    boolean mAdminActive;
    Button DisableCamera,AbleCamera;
    TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // DevicePolicyManagerインスタンス生成
        mDPM = (DevicePolicyManager)getSystemService(this.DEVICE_POLICY_SERVICE);
        mLockReciever = new ComponentName(this, LockReciever.class);
        DisableCamera = (Button)findViewById(R.id.button);
        AbleCamera = (Button)findViewById(R.id.button2);
        status = (TextView)findViewById(R.id.textView2);
        // デバイス管理者権限を有効にする画面を呼び出す
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                mLockReciever);
        startActivityForResult(intent, 1);

        // デバイスの管理者権限の状態(有効/無効)を取得
        mAdminActive = mDPM.isAdminActive(mLockReciever);

        DisableCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDPM.setCameraDisabled(mLockReciever,true);
                setStatus(true);
            }
        });
        AbleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDPM.setCameraDisabled(mLockReciever,false);
                setStatus(false);
            }
        });


        if (mAdminActive) {
            // デバイスの管理者権限がアクティブの場合はカメラ無効
            mDPM.setCameraDisabled(mLockReciever, true);
        }
    }

    private void setStatus(boolean type){
        if(type) status.setText("現在の状態:カメラが無効化されています");
        else status.setText("現在の状態：カメラが有効です");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("DeviceAdminSample", "Administration enabled!");
                } else {
                    Log.i("DeviceAdminSample", "Administration enable FAILED!");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mAdminActive) {
            // デバイスの管理者権限がアクティブの場合はカメラ無効
            mDPM.setCameraDisabled(mLockReciever, false);
        }
    }
}
