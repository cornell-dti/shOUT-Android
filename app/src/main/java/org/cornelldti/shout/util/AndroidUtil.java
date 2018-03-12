package org.cornelldti.shout.util;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Evan Welsh on 3/11/18.
 */

public class AndroidUtil {

    public static void hideSoftKeyboard(Activity activity, IBinder windowToken) {
        if (activity != null) {
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (manager != null) {
                manager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

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
