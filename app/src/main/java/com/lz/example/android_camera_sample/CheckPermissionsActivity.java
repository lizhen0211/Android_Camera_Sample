package com.lz.example.android_camera_sample;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lz on 2017/6/27.
 */

public class CheckPermissionsActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * 需要进行检测的权限数组
     */
/*    protected String[] needPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE
    };*/

    private boolean isNeedCheck = true;

    private static final int PERMISSON_REQUESTCODE = 0;

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck) {
            if (checkPermissions(getNeedPermissions())) {
                //requestLocation();
                onPermissionAllow();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(grantResults)) {
                //showMissingPermissionDialog();
                isNeedCheck = false;
                onPermissionDeny();
            } else {
                //call method that need permissions
                //requestLocation();
                onPermissionAllow();
            }
        }
    }

    protected void onPermissionAllow() {
    }

    protected void onPermissionDeny() {
    }

    protected String[] getNeedPermissions() {
        return new String[]{};
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param permissions
     * @since 2.5.0
     */
    private boolean checkPermissions(String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissonList.toArray(
                            new String[needRequestPermissonList.size()]),
                    PERMISSON_REQUESTCODE);
            return false;
        }
        return true;
    }

    /**
     * 获取权限需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissonList.add(perm);
            } else {
                //To help find situations where the user might need an explanation,
                // Android provides a utiltity method, shouldShowRequestPermissionRationale().
                // This method returns true if the app has requested this permission previously
                // and the user denied the request.

                //Note: If the user turned down the permission request in the past and
                // chose the Don't ask again option in the permission request system dialog,
                // this method returns false. The method also returns false
                // if a device policy prohibits the app from having that permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this, perm)) {
                    needRequestPermissonList.add(perm);
                }
            }
        }
        return needRequestPermissonList;
    }
}
