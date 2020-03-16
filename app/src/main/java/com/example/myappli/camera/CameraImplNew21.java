package com.example.myappli.camera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
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
 * Description:主要针对android 5.0以上,使用Camera2的api实现简易模式在SOS紧急页面拨打号码后,根据用户先前配置判断,前后置相机拍照的图片,进行彩信发送.
 * Detail:
 * @author kallen
 * Create Time: 2019/11/6
 * @version 1.0
 * @since  Since
 * @see ...
 * History:
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CameraImplNew21 implements ICamera, ImageReader.OnImageAvailableListener {

    private Context mContext;
    protected int mType = 0;
    protected String mCID = null;
    protected Size mPicSize = new Size(1920, 1080);
//    protected Size mPicSize = new Size(720, 1280);
//    protected Size mPicSize = new Size(480, 800);
    private ImageReader mImageReader;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    protected File mDir;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraSession;
    protected List<Surface> mSurfaces;
    private CaptureRequest mPreviewRequest;
    protected OnFileCallback mOnFileCallback;

    public CameraImplNew21(Context context, File dir, List<Surface> surfaces) {
        this.mContext = context;
        this.mDir = dir;
    }

    private void initCamera2() {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            //获取可用的相机列表
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String cameraId : cameraIdList) {
                //获取该相机的CameraCharacteristics，它保存的相机相关的属性
                try {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                    if (mType == 0 && cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                        mCID = cameraId;
//                    initInfo(cameraCharacteristics);
                        break;
                    } else if (mType == 1 && cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        mCID = cameraId;
//                    initInfo(cameraCharacteristics);
                        break;
                    }
                }catch (Exception e ){
                    Log.e(TAG,"初始化摄像头:"+e.toString());
                }

            }
            //实例化一个ImageReader对象
            mImageReader = ImageReader.newInstance(mPicSize.getWidth(), mPicSize.getHeight(), ImageFormat.JPEG, 2);
            //给imageReader对象设置监听
            mImageReader.setOnImageAvailableListener(this, mHandler);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"初始化摄像头失败:"+e.toString());
        }

    }

    private void startBackgroundThread() {
        if (mHandlerThread == null && mHandler == null) {
            mHandlerThread = new HandlerThread("camera");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }
    }

    private void initInfo(CameraCharacteristics characteristics) {
        //获取相机支持的流的参数的集合
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //获取输出格式为ImageFormat.JPEG支持的所有尺寸
        Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
        for (Size size : sizes) {
            //遍历尺寸,判断宽高比是否符合条件
            if (1f * size.getWidth() / size.getHeight() == 4f / 3) {
                mPicSize = size;
                break;
            }
        }
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
            final File file = File.createTempFile("img_", ".jpg", mDir);
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.write(src);
            randomAccessFile.close();
            mOnFileCallback.onFile(file.getPath(),mType);
            Log.d(TAG, "相片路径onImageAvailable: "+file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(randomAccessFile);
        }
    }


    /**
     * 打开相机
     *
     * @param type
     */
    @Override
    public void open(int type) {
        mType = type;
        startBackgroundThread();
        initCamera2();
    }

    /**
     * 开始预览
     */
    @SuppressLint("MissingPermission")
    @Override
    public void startPreview() {
        if (mCameraDevice == null) {
            CameraManager cm = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            try {
                cm.openCamera(mCID, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        mCameraDevice = camera;
                        createPreviewSession();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        CameraImplNew21.this.onError(camera);
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
            createPreviewSession();
        }
    }

    /**
     * 创建与相机连接的Session
     */
    private void createPreviewSession() {
        try {
            List<Surface> list = new ArrayList<>();
            list.add(mImageReader.getSurface());
            mCameraDevice.createCaptureSession(list, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraSession = session;
                    preview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    onError(mCameraDevice);
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            Log.e("Camera2", e.getMessage());
        }
    }

    /**
     * 预览
     */
    private void preview() {
        try {
            CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface surface : mSurfaces) {
                request.addTarget(surface);
            }
            mPreviewRequest = request.build();
            mCameraSession.setRepeatingRequest(mPreviewRequest, null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
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
                    onError(mCameraDevice);
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            Log.e("Camera2", e.getMessage());
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
                    //后置摄像拍照后,启动前置摄像头
                    open(1);
                    //拍照
                    takePicture();
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
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
     * 停止预览
     */
    @Override
    public void stopPreview() {
        try {
            if (mCameraSession != null) {
                mCameraSession.stopRepeating();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自动对焦
     */
    @Override
    public void autoFocus() {

    }

    /**
     * 启动相机拍照
     */
    @SuppressLint("MissingPermission")
    @Override
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
                        CameraImplNew21.this.onError(camera);
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

    @Override
    public void takePicture(OnFileCallback callback) {

    }

    /**
     * 设置监听拍照完毕后,通过mImageReader.setOnImageAvailableListener监听回传相片file的文件
     * @param callback
     */
    @Override
    public void setOnFileCallBackListener(OnFileCallback callback) {
        this.mOnFileCallback = callback;
    }


    @Override
    public void startRecordVideo() {

    }

    @Override
    public void stopRecordVideo(OnFileCallback callback) {

    }

    /**
     * 关闭相机
     */
    @Override
    public void close() {
        //关闭android与相机连接的Session
        if (mCameraSession != null) {
            mCameraSession.close();
            mCameraSession = null;
        }
        //关闭相机设备
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        //关闭线程
        stopBackgroundThread();
    }

    /**
     * 停止后台线程
     */
    private void stopBackgroundThread() {
        if (mHandlerThread == null) {
            return;
        }
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.quit();
            mHandlerThread = null;
            mHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        }
    }

    private void onError(CameraDevice camera) {
        if (camera != null) {
            camera.close();
            mCameraDevice = null;
        }
//        if (mRetry++ < RETRY_MAX) {
//            open(mType);
//            startPerview();
//        }
    }

}
