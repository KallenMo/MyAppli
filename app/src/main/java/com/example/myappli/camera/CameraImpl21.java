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
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
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
 * @author BC
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CameraImpl21 implements ICamera, ImageReader.OnImageAvailableListener {

    public final static int RETRY_MAX = 3;

    protected final List<Surface> mSurfaces;

    protected final File mDir;

    protected final Context mAppContext;

    CameraImpl21(Context applicationContext, File dir, final List<Surface> surfaces) {
        mAppContext = applicationContext;
        mDir = dir;
        mSurfaces = surfaces;
    }

    protected HandlerThread mHandlerThread;
    protected Handler mHandler;

    protected CameraDevice mCameraDevice;
    protected CameraCaptureSession mCameraSession;
    protected CaptureRequest mPerviewRequest;

    protected ImageReader mImageReader;

    protected Size mPicSize = new Size(1920, 1080);
    protected Size mVideoSize = new Size(1920, 1080);

    protected int mRetry = 0;

    protected MediaRecorder mMediaRecorder;
    protected String mVideoFile;

    protected String mCID = null;
    protected int mType = 0;

    @SuppressLint("MissingPermission")
    @Override
    public void open(int type) {
        mType = type;
        startBackgroundThread();
        init();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void startPreview() {
        if (mCameraDevice == null) {
            CameraManager cm = (CameraManager) mAppContext.getSystemService(Context.CAMERA_SERVICE);
            try {
                cm.openCamera(mCID, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        mCameraDevice = camera;
                        startPreview();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        CameraImpl21.this.onError(camera);
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

    @Override
    public void autoFocus() {

    }

    @Override
    public void takePicture() {

    }

    @Override
    public void takePicture(OnFileCallback callback) {
        mOnFileCallback = callback;
        try {
            CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            request.set(CaptureRequest.JPEG_ORIENTATION, 90);
            request.set(CaptureRequest.JPEG_QUALITY, (byte) 80);
            request.addTarget(mImageReader.getSurface());
            CaptureRequest build = request.build();
            mCameraSession.capture(build, null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOnFileCallBackListener(OnFileCallback callback) {

    }

    @Override
    public void startRecordVideo() {
        try {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            mMediaRecorder.setOrientationHint(90);
            mVideoFile = File.createTempFile("video_", ".mp4", mDir).getAbsolutePath();
            mMediaRecorder.setOutputFile(mVideoFile);
            mMediaRecorder.prepare();
            final CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            for (Surface surface : mSurfaces) {
                builder.addTarget(surface);
            }
            builder.addTarget(mMediaRecorder.getSurface());

            try {
                List<Surface> list = new ArrayList<>(mSurfaces);
                list.add(mMediaRecorder.getSurface());
                mCameraDevice.createCaptureSession(list, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try {
                            session.setRepeatingRequest(builder.build(), null, mHandler);
                            mMediaRecorder.start();
                        } catch (CameraAccessException | IllegalStateException e) {
                            e.printStackTrace();
                            if (e instanceof IllegalStateException) {
                                mMediaRecorder.release();
                                mMediaRecorder = null;
                            }
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        session.close();
                        onError(mCameraDevice);
                    }
                }, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } catch (IOException | CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopRecordVideo(OnFileCallback callback) {
        try {
            mMediaRecorder.stop();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            mMediaRecorder.reset();
        }
        callback.onFile(mVideoFile,mType);
        startPreview();
    }

    @Override
    public void close() {
        if (mCameraSession != null) {
            mCameraSession.close();
            mCameraSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        if (mHandlerThread == null && mHandler == null) {
            mHandlerThread = new HandlerThread("camera");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        if (mHandlerThread == null) {
            return;
        }
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
            mHandlerThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        CameraManager cm = (CameraManager) mAppContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] idList = cm.getCameraIdList();

            for (String id : idList) {
                CameraCharacteristics characteristics = cm.getCameraCharacteristics(id);
                if (mType == 0 && characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    mCID = id;
                    initInfo(characteristics);
                    break;
                } else if (mType == 1 && characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    mCID = id;
                    initInfo(characteristics);
                    break;
                }
            }
            mImageReader = ImageReader.newInstance(mPicSize.getWidth(), mPicSize.getHeight(), ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(this, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void initInfo(CameraCharacteristics characteristics) {
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
        for (Size size : sizes) {
            if (1f * size.getWidth() / size.getHeight() == 4f / 3) {
                mPicSize = size;
                break;
            }
        }
        Size[] v_sizes = map.getOutputSizes(ImageFormat.JPEG);
        for (Size size : v_sizes) {
            if (1f * size.getWidth() / size.getHeight() == 4f / 3) {
                mVideoSize = size;
                break;
            }
        }
    }

    private void createPreviewSession() {
        try {
            List<Surface> list = new ArrayList<>(mSurfaces);
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
            Log.e("Camera2",e.getMessage());
        }
    }

    private void preview() {
        try {
            CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface surface : mSurfaces) {
                request.addTarget(surface);
            }
            mPerviewRequest = request.build();
            mCameraSession.setRepeatingRequest(mPerviewRequest, null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected OnFileCallback mOnFileCallback;

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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(randomAccessFile);
        }
    }

    private void onError(CameraDevice camera) {
        if (camera != null) {
            camera.close();
            mCameraDevice = null;
        }
        if (mRetry++ < RETRY_MAX) {
            open(mType);
            startPreview();
        }
    }

}
