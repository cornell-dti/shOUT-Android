package org.cornelldti.shout.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * A basic utility class for common methods needed to manage Android layouts.
 * <p>
 * Created by Evan Welsh 2/28/18
 */

public class LayoutUtil {

    // TODO no magic strings

    /**
     * Calculates the height of the status bar.
     * Used to add padding for views that draw behind the status bar.
     *
     * @param context - The context to calculate within
     * @return int - the resulting height in pixels.
     */
    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 0;
        }

        int result = -1;

        Resources res = context.getResources();

        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }

    /**
     * Converts dp units to pixels.
     *
     * @param resources - The resources object to pull dimension sizes from.
     * @param dp        - The dp dimension
     * @return int - The resulting pixels dimension
     */
    public static int getPixelsFromDp(Resources resources, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
