package com.lz.example.android_camera_sample.camera;

/**
 * Created by lz on 2017/6/27.
 */

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.lz.example.android_camera_sample.CameraActivity;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private Handler previewHandler;

    private CameraActivity.PreviewCallBack previewCallBack;

    public static final int previewMessage = 1;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setOneShotPreviewCallback(this);
            //通过SurfaceView显示取景画面
            mCamera.setPreviewDisplay(holder);
            //开始预览
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        //releaseCamera();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setOneShotPreviewCallback(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    public void setPreviewHandler(Handler previewHandler) {
        this.previewHandler = previewHandler;
    }

    public void setPreviewCallBack(CameraActivity.PreviewCallBack previewCallBack) {
        this.previewCallBack = previewCallBack;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //Log.e(TAG, data.toString());
        if (previewHandler != null) {
            /*Message message = previewHandler.obtainMessage(previewMessage, data);
            message.sendToTarget();*/
        }
        if (previewCallBack != null) {
            previewCallBack.onPreviewFrame(data, camera);
        }
    }

    public void setOneShotPreviewCallback(){
        mCamera.setOneShotPreviewCallback(this);
    }
}