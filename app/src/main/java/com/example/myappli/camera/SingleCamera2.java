package com.example.myappli.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.example.myappli.util.CloseUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Detail:
 * Create Time: 2020/3/16
 *
 * @author kallen
 * @version 1.0
 * @see ...
 * History:
 * @since Since
 */
public class SingleCamera2 implements ImageReader.OnImageAvailableListener {
    private static final String TAG = SingleCamera2.class.getSimpleName();
    protected int mType = 0;
    protected String mCID = null;
    private ImageReader mImageReader;
    protected Size mPicSize = new Size(1920, 1080);
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraSession;
    private CaptureRequest mPreviewRequest;
    public Context mContext;
    protected File mDir;

    public SingleCamera2(Context context, File dir) {
        this.mContext = context;
        this.mDir = dir;
    }

    private void initCamera2(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null) {
                String[] cameraIdList = cameraManager.getCameraIdList();
                for (String cameraId : cameraIdList) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                    if (mType == 0 && cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                        mCID = cameraId;
                        break;
                    } else if (mType == 1 && cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        mCID = cameraId;
                        break;
                    }
                }
            }

            //实例化一个ImageReader对象
            mImageReader = ImageReader.newInstance(mPicSize.getWidth(), mPicSize.getHeight(), ImageFormat.JPEG, 2);
            //给imageReader对象设置监听
            mImageReader.setOnImageAvailableListener(this, mHandler);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "初始化摄像头:" + e.toString());
        }
    }

    private void startBackgroundThread() {
        if (mHandlerThread == null && mHandler == null) {
            mHandlerThread = new HandlerThread("camera");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }
    }

    /**
     * 启动相机拍照
     */
    @SuppressLint("MissingPermission")
    public void takePicture() {
        if (mCameraDevice == null) {
            CameraManager cm = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            try {
                cm.openCamera(mCID, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        //启动相机成功
                        mCameraDevice = camera;
                        //创建与相机连接的Session并开始拍照
                        createSessionAndPhotographs();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        if (camera != null) {
                            camera.close();
                            mCameraDevice = null;
                        }
                        Log.e(TAG, "onDisconnected: 相机驱动错误");
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        onDisconnected(camera);
                    }
                }, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            //创建与相机连接的Session并开始拍照
            createSessionAndPhotographs();
        }
    }


    /**
     * 创建与相机连接的Session,并启动拍照
     */
    private void createSessionAndPhotographs() {
        try {
            List<Surface> list = new ArrayList<>();
            //添加Surface
            list.add(mImageReader.getSurface());
            mCameraDevice.createCaptureSession(list, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    //连接成功
                    mCameraSession = session;
                    //启动后置拍照
                    if (mType == 1) {
                        startFrontCameraForPicture();
                    } else {
                        startRearCameraForPicture();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    //连接失败
                    if (mCameraDevice != null) {
                        mCameraDevice.close();
                        mCameraDevice = null;
                    }
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            Log.e("Camera2", e.getMessage());
        }
    }

    /**
     * 启动前置摄像头拍照
     */
    private void startFrontCameraForPicture() {
        try {
            CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //添加目标Surface
            request.addTarget(mImageReader.getSurface());
            // 自动对焦
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            request.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
//            int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
//            request.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation)+rORIENTATIONS);
            request.set(CaptureRequest.JPEG_ORIENTATION, 270);
            request.set(CaptureRequest.JPEG_QUALITY, (byte) 90);
            mPreviewRequest = request.build();
            //显示预览
//            mCameraSession.setRepeatingRequest(mPreviewRequest, null, mHandler);
            mCameraSession.capture(mPreviewRequest, new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    if (mCameraSession != null) {
                        mCameraSession.close();
                        mCameraSession = null;
                    }
                    if (mCameraDevice != null) {
                        mCameraDevice.close();
                        mCameraDevice = null;
                    }
                    Log.d("mo", "onCaptureCompleted: 前置拍照完成!!!!");
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动后置摄像头拍照
     */
    private void startRearCameraForPicture() {
        try {
            CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //添加目标Surface
            request.addTarget(mImageReader.getSurface());
            // 自动对焦
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            request.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
//            int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
//            request.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation)+rORIENTATIONS);
            request.set(CaptureRequest.JPEG_ORIENTATION, 90);
            request.set(CaptureRequest.JPEG_QUALITY, (byte) 90);
            mPreviewRequest = request.build();
            //显示预览
//            mCameraSession.setRepeatingRequest(mPreviewRequest, null, mHandler);
            mCameraSession.capture(mPreviewRequest, new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    if (mCameraSession != null) {
                        mCameraSession.close();
                        mCameraSession = null;
                    }
                    if (mCameraDevice != null) {
                        mCameraDevice.close();
                        mCameraDevice = null;
                    }
                    Log.d("mo", "onCaptureCompleted: 后置拍照完成!!!!");
//                    //后置摄像拍照后,启动前置摄像头
//                    openCa(1);
//                    //拍照
//                    takePicture();
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void openCamera2(int type) {
        mType = type;
        startBackgroundThread();
        initCamera2(mContext);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireNextImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        final int remaining = buffer.remaining();
        byte[] src = new byte[remaining];
        buffer.get(src);
        image.close();

        RandomAccessFile randomAccessFile = null;
        try {
            File file = File.createTempFile("img_", ".jpg", mDir);
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.write(src);
            randomAccessFile.close();
//            mOnFileCallback.onFile(file.getPath(),mType);
            Log.d(TAG, "相片路径onImageAvailable: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(randomAccessFile);
        }

    }
}
