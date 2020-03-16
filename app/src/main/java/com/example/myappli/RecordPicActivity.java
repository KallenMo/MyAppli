package com.example.myappli;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class RecordPicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pic);
        MmsSender.getInstance().init(this);

        initView();
    }

    private void initView() {
        findViewById(R.id.tv_redio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MmsSender.getInstance().startUpRecordOrCamera();
            }
        });
    }
}
