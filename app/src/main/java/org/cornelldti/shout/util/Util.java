package org.cornelldti.shout.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.cornelldti.shout.util.function.Producer;

/**
 * Created by Evan Welsh on 4/2/18.
 */
public class Util {

    @NonNull
    public static <T> T getValueOrDefault(T value, @NonNull Producer<T> defaultValue) {
        if (value == null) {
            return defaultValue.get();
        }

        return value;
    }

    @NonNull
    public static <T> T getValueOrDefault(T value, @NonNull T defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    @NonNull
    public static <T extends CharSequence> T getValueOrDefault(T value, @NonNull T defaultValue) {
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }

        return value;
    }
}
