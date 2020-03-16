package com.example.myappli;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.example.myappli.camera.CameraCompat;
import com.example.myappli.camera.ICamera;
import com.example.myappli.util.AudioRecordUtil;
import com.example.myappli.util.CloseUtil;
import com.example.myappli.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Description: 彩信发送者实体:主要针对android 5.0以上,使用Camera2的api实现简易模式在SOS紧急页面拨打号码后,根据用户先前配置判断,前后置相机拍照的图片,以及定位,进行彩信发送.
 * Detail:
 * Create Time: 2019/11/6
 *
 * @author kallen
 * @version 1.0
 * @see ...
 * History:
 * @since Since
 */
public class MmsSender {
    private static final String TAG = MmsSender.class.getSimpleName();
    private String fullMessage;
    private static MmsSender mmsSender;
    private AudioRecordUtil mAudioRecordUtil;
    private File PATH,AUDIO_PATH;
    private Context mContext;
    private CameraCompat cameraCompat;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public static MmsSender getInstance() {
        if (mmsSender == null) {
            mmsSender = new MmsSender();
            return mmsSender;
        }
        return mmsSender;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void init(Context context) {
        this.mContext = context;
        PATH = new File(context.getExternalCacheDir(), "myApp_img_tmp");
        AUDIO_PATH  = new File(context.getExternalCacheDir(), "myApp_audio_tmp");
    }

    /**
     * 启动录音,拍照(包括两者或者其中一者启动的方法)
     */
    public void startUpRecordOrCamera() {

//        if (model.isSendRecord()) {
            startBackgroundThread();
            mAudioRecordUtil = new AudioRecordUtil(AUDIO_PATH);
            mAudioRecordUtil.clear();
            mAudioRecordUtil.start();
            //添加验证 防止内存泄漏报错
            if (mHandlerThread != null && mHandler != null) {
                //进行5秒录音,录音完成后,进行前后相机拍照.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        andAudioMsg();
//                        if (SosModel.getSettings().isSendPic()) {
//                            //进行前后相机拍照,将前后图片添加到MMS发送库的msg
                            andPictureMsg();
//                        } else {
//                            transaction.sendNewMessage(msg, Transaction.NO_THREAD_ID);
//                        }
                        stopBackgroundThread();
                    }
                }, 5000);
            }
//        } else if (!model.isSendRecord() && SosModel.getSettings().isSendPic()) {
//            andPictureMsg();
//        }
    }

    /**
     * 启动录音后台线程
     */
    private void startBackgroundThread() {
        if (mHandlerThread == null && mHandler == null) {
            mHandlerThread = new HandlerThread("AudioRecord");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }
    }

    /**
     * 停止录音后台线程
     */
    private void stopBackgroundThread() {
        if (mHandlerThread == null) {
            return;
        }
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.quit();
            mHandlerThread.interrupt();
            mHandlerThread = null;
            mHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 添加相册信息到MMS发送库的msg对象
     */
    private void andPictureMsg() {
        cameraCompat = new CameraCompat(mContext, PATH, null);
        cameraCompat.open(ICamera.TYPE_BACK);
        cameraCompat.setOnFileCallbackListener(new ICamera.OnFileCallback() {
            @Override
            public void onFile(String file, int mType) {
                if (!TextUtils.isEmpty(file)) {
                    Bitmap bitmapPicPath1 = BitmapFactory.decodeFile(file);
                    Bitmap comp = FileUtil.comp(bitmapPicPath1, 720, 1280);
                    Log.d(TAG, "压缩runmbitmapPicPath1: " + comp.getByteCount());
//                    msg.addImage(comp);
//                    //返回前置摄像头,代表前后置摄像头拍照完成.
//                    if (mType == 1) {
//                        Log.d(TAG, "彩信已发送: ");
//                        transaction.sendNewMessage(msg, Transaction.NO_THREAD_ID);
//                    }
                }
            }
        });
        //启动拍照,当后置拍照完成后,会启动前置摄像头进行拍照
        cameraCompat.takePicture();

    }

    /**
     * 添加录音信息到MMS发送库的msg对象
     */
    private void andAudioMsg() {
        String mAudioPath = mAudioRecordUtil.stop();
        Log.d(TAG, "音频mAudioPath: " + mAudioPath);
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(mAudioPath, "rw");
            byte[] src = new byte[(int) file.length()];
            file.read(src);
//            msg.addAudio(src);
            Log.d(TAG, "录音:完成!!!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage() + "  录音");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage() + "  录音IOException");
        } finally {
            if (mAudioRecordUtil != null) {
                mAudioRecordUtil.stop();
            }
            CloseUtil.close(file);
        }
    }

    /**
     * 停止录音,拍照,回收垃圾对象
     */
    public void stopRecordOrCamera() {
        if (cameraCompat != null) {
            cameraCompat.close();
        }
        if (mAudioRecordUtil != null) {
            mAudioRecordUtil.stop();
        }
    }

}
