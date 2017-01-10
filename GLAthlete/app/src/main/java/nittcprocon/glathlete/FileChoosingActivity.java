package nittcprocon.glathlete;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * デバッグ用の動画ファイルを選択するActivity
 * わざわざ用意する必要があるのかやや疑問
 */

public class FileChoosingActivity extends AppCompatActivity {
    private static final String TAG = "FileChoosingActivity";
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_choosing);

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) { // OK
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Intent intent = new Intent(this, VRActivity.class);
                intent.putExtra("uri", uri.toString());
                startActivity(intent);
            } else {
                finish();
            }
        } else {
            Log.e(TAG, "AIEEE");
        }
        finish();
    }
}
