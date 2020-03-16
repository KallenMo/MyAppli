package com.example.myappli.util;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * 音频录制工具
 *
 * @author BC
 */
public class AudioRecordUtil {

    private final static int SATAUS_IDLE = 0;
    private final static int SATAUS_RECORD = 1;
    private static final String TAG = AudioRecordUtil.class.getSimpleName();

    private MediaRecorder mMediaRecorder;

    private int mStatus = SATAUS_IDLE;

    private int mAudioSource;
    private int mAudioEncoder;
    private int mOutputFormat;

    private String mPath;

    private File mDir;

    /**
     * 默认配置
     *
     * @throws IOException 路径异常
     */
    public AudioRecordUtil(File dir) {
        init(MediaRecorder.AudioSource.MIC, MediaRecorder.AudioEncoder.AAC, MediaRecorder.OutputFormat.AAC_ADTS, dir);
    }

    /**
     * 初始化
     *
     * @param audioSource  音频源
     * @param encoder      音频编码
     * @param outputFormat 输出格式
     * @param dir          路径
     * @throws IOException 路径异常
     */
    public AudioRecordUtil(int audioSource, int encoder, int outputFormat, File dir) throws IOException {
        init(audioSource, encoder, outputFormat, dir);
    }

    private void init(int audioSource, int encoder, int outputFormat, File dir) {
        mAudioSource = audioSource;
        mAudioEncoder = encoder;
        mOutputFormat = outputFormat;
        check(dir);
    }

    /**
     * 检查文档路径是否存在
     * @param dir
     */
    private void check(File dir) {
        if (dir == null) {
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
        mDir = dir;
    }

    /**
     * 开始录制
     */
    public void start() {
        if (mStatus == SATAUS_RECORD) {
            return;
        }
        try {
            createFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(mAudioSource);
        mMediaRecorder.setOutputFormat(mOutputFormat);
        mMediaRecorder.setAudioEncoder(mAudioEncoder);
        mMediaRecorder.setOutputFile(mPath);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mMediaRecorder.start();
            mStatus = SATAUS_RECORD;
        } catch (IllegalStateException e) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mStatus = SATAUS_IDLE;
        }
    }

    private void createFile() throws IOException {
        final String p = "sos_audio_";
        final String s = ".aac";
        File tempFile = File.createTempFile(p, s, mDir);
        mPath = tempFile.getAbsolutePath();
        if (mDir == null) {
            mDir = tempFile.getParentFile();
        }
    }

    /**
     * 停止录制
     */
    public String stop() {
        if (mStatus == SATAUS_IDLE) {
            return "";
        }
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (IllegalStateException e) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mStatus = SATAUS_IDLE;
        return mPath;
    }

    /**
     * 清空音频缓存
     */
    public void clear() {
        if (mDir == null) {
            return;
        }
        final File root = mDir;
        del(root);
    }

    private void del(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                del(f);
            }
        } else {
            file.delete();
        }
    }

}
