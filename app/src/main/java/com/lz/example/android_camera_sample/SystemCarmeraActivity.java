package com.lz.example.android_camera_sample;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class SystemCarmeraActivity extends CheckPermissionsActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_carmera);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    public void openSystemCarmeraClick(View view) {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Notice that the startActivityForResult() method is protected by a condition that calls resolveActivity(),
        // which returns the first activity component that can handle the intent.
        // Performing this check is important because if you call startActivityForResult() using an intent that no app can handle,
        // your app will crash.
        // So as long as the result is not null, it's safe to use the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    protected void onPermissionDeny() {
        super.onPermissionDeny();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        Toast.makeText(SystemCarmeraActivity.this, "缺少必要的权限", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String[] getNeedPermissions() {
        String[] needPermissions = {Manifest.permission.CAMERA};
        return needPermissions;
    }
}
