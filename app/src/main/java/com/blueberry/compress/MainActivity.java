package com.blueberry.compress;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    //测试图片的存位置
    private String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "test.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        findViewById(R.id.btn_compress)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        v.setClickable(false);
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {

                                compressTest();
                                testJpeg();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                v.setClickable(true);
                                Toast.makeText(MainActivity.this, "压缩完成", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }.execute();
                    }
                });

    }

    /**
     * 对比压缩出的同等质量的图片，使用哈夫曼算法的话，压缩的更小
     */
    private void compressTest() {
        File file = new File(picPath);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Log.i(TAG, "compressTest:: file=" + file.getAbsolutePath());
        ImageCompress.nativeCompressBitmap(bitmap, 20, "/sdcard/hfresult.jpg", true);

        try {

            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, new FileOutputStream("sdcard/result.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void testJpeg() {
        try {
            int quality = 50;
            InputStream in = getResources().getAssets().open("big.jpg");
            Bitmap bit = BitmapFactory.decodeStream(in);
            File dirFile = getExternalCacheDir();
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File originalFile = new File(dirFile, "original.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(originalFile);
            bit.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
            File jpegTrueFile = new File(dirFile, "jpegtrue.jpg");
            File jpegFalseFile = new File(dirFile, "jpegfalse.jpg");
            ImageCompress.nativeCompressBitmap(bit, quality, jpegTrueFile.getAbsolutePath(), true);
            ImageCompress.nativeCompressBitmap(bit, quality, jpegFalseFile.getAbsolutePath(), false);

            Log.d(TAG, "testJpeg:: 压缩图片完成 ...");
            Log.d(TAG, "testJpeg:: jpegTrueFile=" + jpegTrueFile.getAbsolutePath());
            Log.d(TAG, "testJpeg:: jpegFalseFile=" + jpegFalseFile.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 6.0 权限申请
     */
    private void checkPermission() {
        if (checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            new Thread(new WritePictureRunnable()).start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new Thread(new WritePictureRunnable()).start();
            }
        }
    }


    class WritePictureRunnable implements Runnable {

        @Override
        public void run() {
            writePic();
        }

        private void writePic() {
            File dstFile = new File(picPath);
            AssetManager assetManager = getAssets();
            InputStream inputStream = null;
            OutputStream out = null;
            try {
                inputStream = assetManager.open("big.jpg");
                out = new FileOutputStream(dstFile);
                int len = -1;
                byte[] bytes = new byte[8096];
                while ((len = inputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
