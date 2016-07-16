package com.example.pz.synchroathlete;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Sdcard extends AppCompatActivity {

    public static final int REQUEST_CODE_SECONDARY_STORAGE_ACCESS_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_SECONDARY_STORAGE_ACCESS_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SECONDARY_STORAGE_ACCESS_PERMISSION:

                    // 取得した URI に恒久的にアクセスできるようにするための処理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        getContentResolver().takePersistableUriPermission(data.getData(),
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }

                    // URI から SD カードのディレクトリツリーを取得
                    DocumentFile sdDirectoryTree = DocumentFile.fromTreeUri(this, data.getData());

                    OutputStream toOutputStream = null;
                    try {
                        toOutputStream = getContentResolver().openOutputStream(sdDirectoryTree.createFile("text/plain", "test.txt").getUri());
                        toOutputStream.write("1,2,3,4,5,6,7,8,9".getBytes());
                    } catch (Exception e) {
                    } finally {
                        try {
                            toOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
/*
                    // 本体メモリに入っている source.jpg というファイルを SD カードにコピーする
                    InputStream fromInputStream = null;
                    OutputStream toOutputStream = null;
                    try {
                        DocumentFile source = DocumentFile.fromFile(new File(Environment
                                .getExternalStorageDirectory().getAbsolutePath() + File.separator + "source.jpg"));

                        if (!source.exists()) {
                            Toast.makeText(MainActivity.this, "'source.jpg' not found in primary storage", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String copiedFileName = "copied.jpg";
                        String copiedMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                MimeTypeMap.getFileExtensionFromUrl(copiedFileName));

                        fromInputStream = getContentResolver().openInputStream(source.getUri());

                        // ファイル名は拡張子を除いたものを利用する
                        // メソッド側で適切な拡張子を用意するとドキュメントにあるため
                        toOutputStream = getContentResolver().openOutputStream(secondaryStorageRootDirectoryTree
                                .createFile(copiedMimeType, copiedFileName.replace(".jpg", "")).getUri());

                        int DEFAULT_BUFFER_SIZE = 1024 * 4;
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int n;
                        while (-1 != (n = fromInputStream.read(buffer))) {
                            toOutputStream.write(buffer, 0, n);
                        }

                        Toast.makeText(MainActivity.this, "Succeed to create 'copied.jpg'", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fromInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            toOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                    */
            }
        }
    }

}