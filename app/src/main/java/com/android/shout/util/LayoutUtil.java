package com.android.shout.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * A basic utility class for common methods needed to manage Android layouts.
 * <p>
 * Created by Evan Welsh 2/28/18
 */

public class LayoutUtil {

    // todo no magic strings
    public static int getStatusBarHeight(Context context) {
        int result = -1;

        Resources res = context.getResources();

        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }

    public static int getPixelsFromDp(Resources res, int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, res.getDisplayMetrics());
    }
}
