package com.lz.example.android_camera_sample;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lz.example.android_camera_sample.camera.CameraPreview;
import com.lz.example.android_camera_sample.camera.open.OpenCameraInterface;

import java.io.ByteArrayOutputStream;
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
    private ImageView surfaceView;

    private static final String TAG = CameraActivity.class.getSimpleName();

    private int scanWindowMarginTopDp = 0;
    private static final int scanWindowMarginTopPx = 150;
    //bitmap 相对于预览窗口的上下间距之和
    private int bitmapMarginDp;
    private static final int bitmapMarginPx = 50;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_capture);
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        surfaceView = (ImageView) findViewById(R.id.result_surface_view);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        scanWindowMarginTopDp = dip2px(this, scanWindowMarginTopPx);
        layoutParams.setMargins(layoutParams.leftMargin, scanWindowMarginTopDp, layoutParams.rightMargin, layoutParams.bottomMargin);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();

            }
        });
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

                int previewWidth;
                int previewHeight;

                Camera.Size maxValue = sizes.get(0);
                for (int i = 0; i < sizes.size(); i++) {
                    Camera.Size value = sizes.get(i);
                    if (value.width > maxValue.width) {
                        maxValue = value;
                    }
                }
                previewWidth = maxValue.width;
                previewHeight = maxValue.height;

                Log.e(TAG + "previewSize", maxValue.width + ":" + maxValue.height);

                //小米手机预览大小大于屏幕宽度，导致预览图片左右充满屏幕
                if (maxValue.height > screenWidth) {
                    for (int i = 0; i < sizes.size(); i++) {
                        Camera.Size size = sizes.get(i);
                        if (size.height == screenWidth && size.width == screenHeight) {
                            previewWidth = screenHeight;
                            previewHeight = screenWidth;
                            break;
                        }
                    }
                }
                params.setPreviewSize(previewWidth, previewHeight);
                for (Camera.Size size : sizes) {
                    //Log.e(TAG + "PreviewSize", size.width + ":" + size.height);
                }
                //Camera.Size bestSize = Utils.getBestSize(mCamera, sizes, true, screenWidth, screenHeight);

                //Log.e(TAG + "bestPreviewSize", bestSize.width + ":" + bestSize.height);
                //params.setPreviewSize(bestSize.width, bestSize.height);
                Log.e(TAG + "oriPreviewSize", params.getPreviewSize().width + ":" + params.getPreviewSize().height);
                Log.e(TAG + "oriPreviewSize screen", screenWidth + ":" + screenHeight);
                //params.setPreviewSize(1920, 1080);
                //params.setPreviewSize(params.getPreviewSize().width, params.getPreviewSize().height);
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
                //params.setPictureSize(bestSize.width, bestSize.height);
            }
            Log.e(TAG + "oriPreviewSize picture", params.getPictureSize().width + ":" + params.getPictureSize().height);


            int displayOrientation = getDisplayOrientation();
            mCamera.setDisplayOrientation(displayOrientation);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setRotation(displayOrientation);
            mCamera.setParameters(parameters);

            mCamera.setParameters(params);
            mPreview = new CameraPreview(this, mCamera);
            mPreview.setPreviewHandler(previewHandler);
            mPreview.setPreviewCallBack(previewCallBack);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        } else {
            Toast.makeText(CameraActivity.this, "not support", Toast.LENGTH_SHORT).show();
        }
    }

    public int getDisplayOrientation() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        Log.e(TAG, rotation + "");
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    public interface PreviewCallBack {
        void onPreviewFrame(byte[] data, Camera camera);
    }

    private DisplayMetrics outMetrics = new DisplayMetrics();

    private boolean isLog = false;
    private PreviewCallBack previewCallBack = new PreviewCallBack() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Camera.Parameters parameters = mCamera.getParameters();
                    int width = parameters.getPreviewSize().width;
                    int height = parameters.getPreviewSize().height;
                    Log.e(TAG, width + ":" + height);
                    Log.e(TAG, System.currentTimeMillis() + "");

                    YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);

                    byte[] bytes = out.toByteArray();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    //options.inSampleSize = calculateInSampleSize(options,  width,height);
                    options.inSampleSize = 1;
                    Log.e(TAG, "inSampleSize:" + options.inSampleSize + "");
                    Log.e(TAG, "outMetrics: " + outMetrics.widthPixels + ":" + outMetrics.heightPixels);
                    options.inJustDecodeBounds = false;
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    Matrix matrix = new Matrix();
                    matrix.preRotate(getDisplayOrientation());

                    int w = bitmap.getWidth(); // 得到图片的宽，高
                    int h = bitmap.getHeight();

                    if (!isLog) {
                        Log.e("oriPreviewSize bitmap", w + ":" + h);
                        isLog = true;
                    }

                    int wh = w > h ? h : w;// 裁切后所取的正方形区域边长
                    int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
                    int retY = w > h ? 0 : (h - w) / 2;
                    bitmapMarginDp = dip2px(CameraActivity.this, bitmapMarginPx);
                    int newWidth = wh - bitmapMarginDp;
                    int newHeight = wh - bitmapMarginDp;
                    //matrix.postScale(scale, scale);'
                    int bitmapCutOffset;
                    if (scanWindowMarginTopDp > 0) {
                        //bitmapCutOffset = width / 2 - scanWindowMarginTopDp - (newHeight + bitmapMarginDp / 2) / 2;
                        bitmapCutOffset = width / 2 - scanWindowMarginTopDp - (newHeight) / 2;
                    } else {
                        bitmapCutOffset = 0;
                    }

                    final Bitmap newbitmap = Bitmap.createBitmap(bitmap, retX + bitmapMarginDp / 2 - bitmapCutOffset, retY + bitmapMarginDp / 2, newWidth, newHeight, matrix, false);
                    //final Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

                    Log.e(TAG, Thread.currentThread().getName());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                /*ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
                                layoutParams.height = newbitmap.getHeight();
                                layoutParams.width = newbitmap.getWidth();
                                surfaceView.setLayoutParams(layoutParams);*/
                                //surfaceView.drawResult(newbitmap);
                                surfaceView.setImageBitmap(newbitmap);
                                mPreview.setOneShotPreviewCallback();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        }
    };

    private Handler previewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == mPreview.previewMessage) {
                Camera.Parameters parameters = mCamera.getParameters();
                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;
                Log.e(TAG, width + ":" + height);
                byte[] data = (byte[]) msg.obj;
                YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
                byte[] bytes = out.toByteArray();
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inJustDecodeBounds = true;
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                Log.e(TAG, Thread.currentThread().getName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //surfaceView.drawResult(bitmap);
                    }
                });


            }
        }
    };

    private void decode(byte[] data, int width, int height) {
        /*long start = System.currentTimeMillis();
        Result rawResult = null;
        //add by lz start
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }*/
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

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

// Raw height and width of image

        final int height = options.outHeight;

        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;

            final int halfWidth = width / 2;
            // 保证压缩后的宽高都不小于要求的宽高。

            while ((halfHeight / inSampleSize) > reqHeight &&
                    (halfWidth / inSampleSize) > reqWidth) {

                inSampleSize *= 2;

            }

        }

        return inSampleSize;

    }
}
