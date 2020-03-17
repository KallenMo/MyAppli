package com.example.myappli;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    //    Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_MMS,    Manifest.permission.RECORD_AUDIO,
//    private String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_MMS, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

//    private String[] PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.CALL_PHONE,
//            Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_MMS,
//            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
//            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.CAMERA, Manifest.permission.INTERNET};

    //    String[] strings = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET};

    private Handler checkPermissionsHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAllPermissions();
        TextView textDialog = findViewById(R.id.text_dialog);
        textDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.setPackage("com.android.launcher3.tod");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                startActivity(intent);
//                finish();

                Intent intent = new Intent(MainActivity.this, RecordPicActivity.class);
                startActivity(intent);
//
//                final PermissionDialog permissionDialog = new PermissionDialog(MainActivity.this, PERMISSIONS);
//                permissionDialog.setYesOnclickListener("确定", new PermissionDialog.onYesOnclickListener() {
//                    @Override
//                    public void onYesClick() {
//                        permissionDialog.dismiss();
//
//                    }
//                });
//                permissionDialog.setNoOnclickListener("退出", new PermissionDialog.onNoOnclickListener() {
//                    @Override
//                    public void onNoClick() {
//                        permissionDialog.dismiss();
//
//                        finish();
//                    }
//                });
//                permissionDialog.setCanceledOnTouchOutside(false);
//                permissionDialog.setCancelable(false);
//                permissionDialog.show();

//                ComponentName cn = new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClock");
//                Intent intent = new Intent();
//                intent.setComponent(cn);
//                intent.putExtra("deskclock.select.tab",1);
//                intent.putExtra("isShorcuts",false);
//                startActivity(intent);
            }
        });

        initView();
    }



    private void initView() {

    }

    private void checkAllPermissions() {
        checkPermissionsHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivityPermissionsDispatcher.getMultiWithPermissionCheck(MainActivity.this);
            }
        }, 800);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.CALL_PHONE,
//            Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_MMS,
//            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
//            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA})
    public void getMulti() {
        Log.d(TAG, "申请所有权限成功");
        Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.CALL_PHONE,
//            Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_MMS,
//            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
//            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA})
    public void multiDenied() {
        Log.d(TAG, "申请所有权限拒绝");
        Toast.makeText(this, "权限拒绝!!", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.CALL_PHONE,
//            Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_MMS,
//            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
//            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA})
    public void multiNeverAsk() {
        Log.d(TAG, "拒绝权限，不在询问");
        Toast.makeText(this, "拒绝App需要相关权限，且不再询问，请到设置页面开启权限。", Toast.LENGTH_SHORT).show();
    }
}
