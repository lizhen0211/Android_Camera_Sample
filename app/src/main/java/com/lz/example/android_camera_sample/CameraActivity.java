package com.lz.example.android_camera_sample;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.lz.example.android_camera_sample.camera.CameraPreview;
import com.lz.example.android_camera_sample.camera.open.OpenCameraInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lz on 2017/6/27.
 */

public class CameraActivity extends CheckPermissionsActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    private static final String TAG = CameraActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_capture);
        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    protected void onPermissionAllow() {
        if (OpenCameraInterface.checkCameraHardware(this)) {
            // Create an instance of Camera
            mCamera = OpenCameraInterface.open();
            // Create our Preview view and set it as the content of our activity.

            // set Camera parameters
            Camera.Parameters params = mCamera.getParameters();

            if (params.getMaxNumMeteringAreas() > 0) { // check that metering areas are supported
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();

                Rect areaRect1 = new Rect(-100, -100, 100, 100);    // specify an area in center of image
                meteringAreas.add(new Camera.Area(areaRect1, 600)); // set weight to 60%
                Rect areaRect2 = new Rect(800, -1000, 1000, -800);  // specify an area in upper right of image
                meteringAreas.add(new Camera.Area(areaRect2, 400)); // set weight to 40%
                //params.setMeteringAreas(meteringAreas);
            }

            //设置对焦区域
            if (params.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> areas = new ArrayList<Camera.Area>();
                int left = -300;
                int top = -300;
                int right = 300;
                int bottom = 300;

                left = left < -1000 ? -1000 : left;
                top = top < -1000 ? -1000 : top;
                right = right > 1000 ? 1000 : right;
                bottom = bottom > 1000 ? 1000 : bottom;
                areas.add(new Camera.Area(new Rect(left, top, right, bottom), 1000));
                //params.setFocusAreas(areas);
            }

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                // Autofocus mode is supported
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();
            //设置预览图片大小
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            if (sizes.size() > 0) {
                /*Camera.Size maxValue = sizes.get(0);
                for (int i = 0; i < sizes.size(); i++) {
                    Camera.Size value = sizes.get(i);
                    if (value.width > maxValue.width) {
                        maxValue = value;
                    }
                }
                Log.e(TAG + "previewSize", maxValue.width + ":" + maxValue.height);*/
                for (Camera.Size size : sizes) {
                    Log.e(TAG + "PreviewSize", size.width + ":" + size.height);
                }
                //Camera.Size bestSize = Utils.getBestSize(mCamera, sizes, true, screenWidth, screenHeight);
                Log.e(TAG + "oriPreviewSize", params.getPreviewSize().width + ":" + params.getPreviewSize().height);
                //Log.e(TAG + "bestPreviewSize", bestSize.width + ":" + bestSize.height);
                //params.setPreviewSize(bestSize.width, bestSize.height);
                //params.setPreviewSize(1920, 1080);
                params.setPreviewSize(params.getPreviewSize().width,params.getPreviewSize().height);
            }

            //设置图片质量
            params.setJpegQuality(100);
            //设置图片大小
            List<Camera.Size> supportedPictureSizes = params.getSupportedPictureSizes();
            if (supportedPictureSizes.size() > 0) {
                for (Camera.Size size : supportedPictureSizes) {
                    //Log.e(TAG + "PictureSize", size.width + ":" + size.height);
                }
                Camera.Size bestSize = Utils.getBestSize(mCamera, supportedPictureSizes, true, screenWidth, screenHeight);
                //Log.e(TAG + "bestPictureSize", bestSize.width + ":" + bestSize.height);
                params.setPictureSize(bestSize.width, bestSize.height);
            }

            mCamera.setParameters(params);
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        } else {
            Toast.makeText(CameraActivity.this, "not support", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPermissionDeny() {
        super.onPermissionDeny();
        Toast.makeText(CameraActivity.this, "缺少必要的权限", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String[] getNeedPermissions() {
        String[] needPermissions = {Manifest.permission.CAMERA,
                Manifest.permission.VIBRATE};
        return needPermissions;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }*/

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density; // 设备的密度
        return (int) (dipValue * scale + 0.5f);
    }
}
