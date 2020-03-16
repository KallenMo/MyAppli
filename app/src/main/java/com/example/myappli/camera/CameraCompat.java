package com.example.myappli.camera;

import android.content.Context;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class CameraCompat {

    private ICamera mCamera;

    /**
     * 创建相机工具
     *
     * @param context 上下文
     * @param dir     保存路径，PS：路径，不是文件
     * @param views   预览用Surface, sdk < 21 : SurfaceView, sdk >= 21 : SurfaceView or TextureView
     * @throws NullPointerException
     * @throws IOException
     */
    public CameraCompat(Context context, final File dir, final List<? extends View> views) {
        check(context, dir, views);
        final boolean isSurfaceView = checkViewType(views);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<Surface> surfaces = null;
            if (views != null && !views.isEmpty()) {
               surfaces = conversion(views, isSurfaceView);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCamera = new CameraImpl23(context.getApplicationContext(), dir, surfaces);
            } else {
                mCamera = new CameraImpl21(context.getApplicationContext(), dir, surfaces);
            }
        } else {
            mCamera = new CameraImpl(dir, views.get(0));
        }
    }

    /**
     * 检查文档路径是否存在
     * @param context
     * @param dir
     * @param views
     */
    private void check(Context context, File dir, List<? extends View> views) {
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

    /**
     * 检查传入View的类型,是否SurfaceView
     * @param views
     * @return
     * @throws IllegalStateException
     */
    private boolean checkViewType(List<? extends View> views) throws IllegalStateException {
        if (views == null) {
            return false;
        }
        final View view = views.get(0);
        if (!(view instanceof SurfaceView) && !(view instanceof TextureView)) {
            throw new IllegalStateException("views cannot SurfaceView or TextureView");
        }
        return view instanceof SurfaceView;
    }

    private List<Surface> conversion(List<? extends View> views, boolean isSurfaceView) {
        final List<Surface> surfaces = new ArrayList<>();
        for (int i = 0; i < views.size(); i++) {
            final View item = views.get(0);
            if (isSurfaceView) {
                surfaces.add(((SurfaceView) item).getHolder().getSurface());
            } else {
                surfaces.add(new Surface(((TextureView) item).getSurfaceTexture()));
            }
        }
        return surfaces;
    }

    /**
     * 开启摄像头
     *
     * @param type 摄像头类型，0：后，1：前
     * @return 开启结果
     */
    public boolean open(int type) {
        mCamera.open(type);
        return true;
    }

    /**
     * 开启预览
     */
    public void startPreview() {
        mCamera.startPreview();
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        mCamera.stopPreview();
    }

    /**
     * 对焦
     */
    public void autoFocus() {
        mCamera.autoFocus();
    }
    /**
     * 启动拍照
     */
    public void takePicture() {
        mCamera.takePicture();
    }

    /**
     * 拍照
     */
    public void takePicture(ICamera.OnFileCallback callback) {
        mCamera.takePicture(callback);
    }

    /**
     * 监听拍照完毕后,通过mImageReader.setOnImageAvailableListener监听回传相片file的文件
     * @param callback
     */
    public void setOnFileCallbackListener(ICamera.OnFileCallback callback) {
        mCamera.setOnFileCallBackListener(callback);
    }

    /**
     * 开始录像
     */
    public void startRecordVideo() {
        mCamera.startRecordVideo();
    }

    /**
     * 停止录像
     */
    public void stopRecordVideo(ICamera.OnFileCallback callback) {
        mCamera.stopRecordVideo(callback);
    }

    /**
     * 关闭摄像头
     */
    public void close() {
        mCamera.close();
    }

}
