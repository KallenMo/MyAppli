package com.example.myappli.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.Surface;

import java.io.File;
import java.util.List;

/**
 * @author BC
 */
@TargetApi(Build.VERSION_CODES.M)
class CameraImpl23 extends CameraImplNew21 implements ICamera {
    CameraImpl23(Context applicationContext, File dir, final List<Surface> surfaces) {
        super(applicationContext, dir, surfaces);
    }
}
