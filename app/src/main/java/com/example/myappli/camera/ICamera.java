package com.example.myappli.camera;

/**
 * 相机操作接口
 *
 * @author BC
 */
public interface ICamera {

    int TYPE_BACK = 0;
    int TYPE_FRONT = 1;

    String TAG = "ICamera";

    /**
     * 结果文件回调接口(针对5.0以上使用Camera2前后相片拍照,增加接口方法参数)
     */
    interface OnFileCallback {
        void onFile(String file, int mType);
    }

    /**
     * 开启摄像头
     */
    void open(int type);

    /**
     * 开启预览
     */
    void startPreview();

    /**
     * 停止预览
     */
    void stopPreview();

    /**
     * 对焦
     */
    void autoFocus();

    /**
     * 拍照(针对5.0以上使用Camera2前后相片拍照,增加接口方法)
     */
    void takePicture();

    /**
     *
     * @param callback
     */
    void takePicture(final OnFileCallback callback);

    /**
     * 设置监听相片文件(针对5.0以上使用Camera2前后相片拍照,增加接口方法)
     */
    void setOnFileCallBackListener(OnFileCallback callback);

    /**
     * 开始录像
     */
    void startRecordVideo();

    /**
     * 停止录像
     */
    void stopRecordVideo(OnFileCallback callback);

    /**
     * 关闭摄像头
     */
    void close();

}
