package com.example.myappli;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.myappli.camera.SingleCamera2;

import java.io.File;
import java.io.IOException;

public class RecordPicActivity extends AppCompatActivity  {

    private static final String TAG = RecordPicActivity.class.getSimpleName();

    protected File mDir;
    private SingleCamera2 singleCamera2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pic);
        MmsSender.getInstance().init(this);
        mDir = new File(getExternalCacheDir(), "RecordPic_img_tmp");;
        check(this,mDir);

        singleCamera2 = new SingleCamera2(this,mDir);
        //后置摄像头
        singleCamera2.openCamera2(0);
        initView();
    }

    /**
     * 检查文档路径是否存在
     *
     * @param context
     * @param dir
     */
    private void check(Context context, File dir) {
        if (context == null || dir == null) {
            throw new NullPointerException("Dir or context or surfaces cannot be empty");
        }
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                try {
                    throw new IOException("Dir creation failed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private void initView() {
        findViewById(R.id.tv_redio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MmsSender.getInstance().startUpRecordOrCamera();
            }
        });

        findViewById(R.id.tv_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleCamera2.takePicture();
            }
        });
    }


}
