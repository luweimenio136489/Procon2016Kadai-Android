package nittcprocon.glathlete

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

/**
 * デバッグ用の動画ファイルを選択するActivity
 * わざわざ用意する必要があるのかやや疑問
 */

class FileChoosingActivity : AppCompatActivity() {
    private var mode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_choosing)

        mode = intent.getStringExtra("mode")

        // Intent.ACTION_PICKは理不尽な挙動をする
        // Intent.ACTION_GET_CONTENTはMarshmallow以上で権限の問題を引き起こす
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_CODE) { // OK
            if (resultCode == Activity.RESULT_OK) {
                val uri = data.data
                val intent: Intent
                if ("debug" == mode) { // デバッグモード
                    intent = Intent(this, VRActivity::class.java)
                } else if ("visual_adjustment" == mode) {
                    intent = Intent(this, VisualAdjustmentActivity::class.java)
                } else {
                    throw RuntimeException(mode!! + ": invalid mode")
                }
                intent.putExtra("uri", uri.toString())
                startActivity(intent)
            } else {
                finish()
            }
        } else {
            Log.e(TAG, "AIEEE")
        }
        finish()
    }

    companion object {
        private val TAG = "FileChoosingActivity"
        private val REQUEST_CODE = 1
    }
}
