package org.cornelldti.shout.util;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Various utilities for managing Android quirks.
 * <p>
 * Created by Evan Welsh on 3/11/18.
 */

public class AndroidUtil {

    /**
     * Hides the soft keyboard input when we are leaving a view.
     * Because apparently Android doesn't...
     *
     * @param activity
     * @param windowToken
     */
    public static void hideSoftKeyboard(Activity activity, IBinder windowToken) {
        if (activity != null) {
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (manager != null) {
                manager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * Retrieves a valid Context instance from the given view (preference) or fragment (secondary preference).
     *
     * @param view
     * @param fragment
     * @return a Context instance
     * @see Context
     */
    public static Context getContext(View view, Fragment fragment) {
        Context context;

        if (view != null && (context = view.getContext()) != null) {
            return context;
        }

        if (fragment != null) {
            if ((context = fragment.getContext()) != null) {
                return context;
            } else if ((context = fragment.getActivity()) != null) {
                return context;
            }
        }

        return null;
    }
}
