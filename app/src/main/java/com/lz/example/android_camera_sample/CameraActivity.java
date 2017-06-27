package com.lz.example.android_camera_sample;

import android.Manifest;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.lz.example.android_camera_sample.camera.CameraPreview;
import com.lz.example.android_camera_sample.camera.open.OpenCameraInterface;

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
