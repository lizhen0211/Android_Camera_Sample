package com.lz.example.android_camera_sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by lz on 2018/4/12.
 */

public class ResultSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Canvas canvas;

    private SurfaceHolder holder;

    public ResultSurfaceView(Context context) {
        this(context, null);
    }

    public ResultSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResultSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void drawResult(Bitmap bitmap) {
        canvas = holder.lockCanvas();
        canvas.drawBitmap(bitmap, 100, 0, new Paint());
        Paint paint = new Paint();
        /*paint.setColor(getResources().getColor(R.color.colorAccent));
        canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);*/
        holder.unlockCanvasAndPost(canvas);
    }
}
