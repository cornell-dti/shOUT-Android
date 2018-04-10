package org.cornelldti.shout.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A basic utility class for common methods needed to manage Android layouts.
 * <p>
 * Created by Evan Welsh 2/28/18
 */

public class LayoutUtil {
    private static final String TAG = "LayoutUtil";

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

    public static <T extends View> T getChild(ViewGroup parent, int[] id) {
        int children = parent.getChildCount();
        ViewGroup currentViewGroup = parent;

        for (int depth = 0, prevDepth = 0; depth < id.length; ) {
            for (int i = 0; i < children; i++) {
                View view = currentViewGroup.getChildAt(i);

                if (id[depth] == view.getId()) {
                    if (depth == id.length - 1) {
                        return (T) view;
                    } else if (view instanceof ViewGroup) {
                        currentViewGroup = (ViewGroup) view;
                        children = currentViewGroup.getChildCount();
                    } else {
                        Log.e(TAG, "Only the last id present can be a non-ViewGroup");

                        return null;
                    }

                    prevDepth = depth;
                    depth++;

                    break;
                }
            }

            if (prevDepth == depth) {
                return null; // infinite loop
            }
        }

        return null;
    }
}
