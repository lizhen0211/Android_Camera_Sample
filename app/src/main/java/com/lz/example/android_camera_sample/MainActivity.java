package com.lz.example.android_camera_sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onExistCameraClick(View view) {
        Intent intent = new Intent(MainActivity.this, SystemCarmeraActivity.class);
        startActivity(intent);
    }

    public void onBuildingCameraAppClick(View view) {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }
}
