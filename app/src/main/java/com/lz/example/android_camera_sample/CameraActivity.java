package com.lz.example.android_camera_sample;

import android.Manifest;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.lz.example.android_camera_sample.camera.CameraPreview;
import com.lz.example.android_camera_sample.camera.open.OpenCameraInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lz on 2017/6/27.
 */

public class CameraActivity extends CheckPermissionsActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPermissionAllow() {
        if (OpenCameraInterface.checkCameraHardware(this)) {
            // Create an instance of Camera
            mCamera = OpenCameraInterface.open();
            // Create our Preview view and set it as the content of our activity.

            // set Camera parameters
            Camera.Parameters params = mCamera.getParameters();

            if (params.getMaxNumMeteringAreas() > 0){ // check that metering areas are supported
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();

                Rect areaRect1 = new Rect(-100, -100, 100, 100);    // specify an area in center of image
                meteringAreas.add(new Camera.Area(areaRect1, 600)); // set weight to 60%
                Rect areaRect2 = new Rect(800, -1000, 1000, -800);  // specify an area in upper right of image
                meteringAreas.add(new Camera.Area(areaRect2, 400)); // set weight to 40%
                params.setMeteringAreas(meteringAreas);
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
}
