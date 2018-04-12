package com.lz.example.android_camera_sample;

/**
 *  Color Picker by Juan Martn
 *  Copyright (C) 2010 nauj27.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
//package com.nauj27.android.colorpicker;

import java.util.Iterator;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.View;
import android.widget.ImageView;

/**
 * @author nauj27
 *
 */
class Utils {
    /**
     * Return the supported picture size that best fits on the device screen.
     * @param camera the camera to instantiate new Size objects
     * @param supportedPictureSizes list of supported sizes
     * @param preview if the supported size is for preview image
     * @param displayWidth the width of the physical display
     * @param displayHeight the height of the physical display
     * @return nearest Camera.Size to device screen
     */
    public static Camera.Size getBestSize(
            Camera camera,
            List<Size> supportedSizes,
            boolean preview,
            int displayWidth,
            int displayHeight) {

        final int PREVIEW_SIZE_WIDTH_EMULATOR = 176;
        final int PREVIEW_SIZE_HEIGHT_EMULATOR = 144;
        final int PICTURE_SIZE_WIDTH_EMULATOR = 213;
        final int PICTURE_SIZE_HEIGHT_EMULATOR = 350;

        double temporalDiff = 0;
        double diff = Integer.MAX_VALUE;

        Camera.Size size = null;
        Camera.Size supportedSize = null;

        if (supportedSizes == null) {
            if (isAndroidEmulator(android.os.Build.MODEL)) {
                if (preview) {
                    size = camera.new Size(
                            PREVIEW_SIZE_WIDTH_EMULATOR,
                            PREVIEW_SIZE_HEIGHT_EMULATOR);
                } else {
                    size = camera.new Size(
                            PICTURE_SIZE_WIDTH_EMULATOR,
                            PICTURE_SIZE_HEIGHT_EMULATOR);
                }
            }
        } else {
            Iterator<Size> iterator = supportedSizes.iterator();
            while (iterator.hasNext()) {
                supportedSize = iterator.next();
                temporalDiff = Math.sqrt(
                        Math.pow(supportedSize.width - displayWidth, 2) +
                                Math.pow(supportedSize.height - displayHeight, 2));

                if (temporalDiff < diff) {
                    diff = temporalDiff;
                    size = supportedSize;
                }
            }

        }

        return size;
    }

    /**
     * Returns if the model is from android sdk emulator.
     * @param model the current model
     * @return boolean value indicating if is the android sdk emulator
     */
    public static boolean isAndroidEmulator(String model) {

        return (model.compareToIgnoreCase("sdk") == 0);
    }

}

