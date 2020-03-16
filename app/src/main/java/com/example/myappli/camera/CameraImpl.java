package com.example.myappli.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.example.myappli.util.CloseUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author BC
 */
class CameraImpl implements ICamera {

    private final SurfaceHolder mSurfaceHolder;
    private final SurfaceTexture mSurfaceTexture;

    private boolean isSurfaceHolder;

    private final File mDir;

    private Camera mCamera;

    private boolean isStartPreview = false;

    private MediaRecorder mMediaRecorder;

    private boolean isStartRecord = false;

    private boolean isTakePicture = false;

    private int mCID = -1;

    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success && isStartPreview) {
//                mCamera.autoFocus(this);
            }
        }
    };

    CameraImpl(File dir, final View view) {
        mDir = dir;
        if (view instanceof SurfaceView) {
            mSurfaceHolder = ((SurfaceView) view).getHolder();
            mSurfaceTexture = null;
            isSurfaceHolder = true;
        } else {
            mSurfaceHolder = null;
            mSurfaceTexture = ((TextureView) view).getSurfaceTexture();
            isSurfaceHolder = false;
        }
    }

    @Override
    public void open(int type) {
        int cameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameras; i++) {
            Camera.getCameraInfo(i, info);
            if (type == 0 && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCID = i;
                break;
            } else if (type == 1 && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCID = i;
                break;
            }
        }
        if (mCID == -1) {
            throw new RuntimeException("Can't find camera");
        }
        mCamera = Camera.open(mCID);
        configCamera();
        try {
            if (isSurfaceHolder) {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } else {
                mCamera.setPreviewTexture(mSurfaceTexture);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void configCamera() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setPictureFormat(ImageFormat.JPEG);

        List<Camera.Size> pSizes = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pSizes) {
            if (1f * size.width / size.height == 4f / 3) {
                parameters.setPictureSize(size.width, size.height);
                break;
            }
        }

        List<Camera.Size> pvSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : pvSizes) {
            if (1f * size.width / size.height == 4f / 3) {
                parameters.setPreviewSize(size.width, size.height);
                break;
            }
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(info.orientation);
    }

    @Override
    public void startPreview() {
        isTakePicture = false;
        if (!isStartPreview) {
            mCamera.lock();
            isStartPreview = true;
            mCamera.startPreview();
        }
    }

    @Override
    public void stopPreview() {
        if (isStartPreview) {
            isStartPreview = false;
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.unlock();
        }
    }

    @Override
    public void autoFocus() {
        mCamera.autoFocus(mAutoFocusCallback);
    }

    @Override
    public void takePicture() {

    }

    @Override
    public void setOnFileCallBackListener(OnFileCallback callback) {

    }

    @Override
    public void takePicture(final OnFileCallback callback) {
        if (isTakePicture) {
            return;
        }
        isTakePicture = true;
        isStartPreview = false;
        mCamera.cancelAutoFocus();
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                startPreview();
                FileOutputStream out = null;
                try {
                    File file = File.createTempFile("img_", ".jpg", mDir);
                    out = new FileOutputStream(file);
                    if (callback != null) {
                        callback.onFile(file.getAbsolutePath(),mCID);
                    }

                    Matrix matrix = new Matrix();
                    matrix.setRotate(90);

                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    bitmap1.compress(Bitmap.CompressFormat.JPEG, 75, out);
                    bitmap1.recycle();
                    bitmap.recycle();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    CloseUtil.close(out);
                }
            }
        });
    }

    private String mVideoFile;

    @Override
    public void startRecordVideo() {
        if (!isStartRecord) {
            isStartRecord = true;
            mCamera.unlock();
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            mMediaRecorder.setOrientationHint(90);
            try {
                mVideoFile = File.createTempFile("video_", ".mp4", mDir).getAbsolutePath();
                mMediaRecorder.setOutputFile(mVideoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaRecorder.setPreviewDisplay(isSurfaceHolder ? mSurfaceHolder.getSurface() : new Surface(mSurfaceTexture));
            try {
                mMediaRecorder.prepare();
                mMediaRecorder.start();
            } catch (IOException | IllegalAccessError e) {
                e.printStackTrace();
                isStartRecord = false;
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }
    }

    @Override
    public void stopRecordVideo(OnFileCallback callback) {
        if (isStartRecord) {
            isStartRecord = false;
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mCamera.lock();
            }
        }
        callback.onFile(mVideoFile,mCID);
        isStartPreview = false;
        mCamera.cancelAutoFocus();
        startPreview();
    }

    @Override
    public void close() {
        mCamera.release();
        mCamera = null;
    }

}
