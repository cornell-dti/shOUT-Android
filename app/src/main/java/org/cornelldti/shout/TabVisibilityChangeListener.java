package org.cornelldti.shout;

import android.os.Bundle;

/**
 * Created by Evan Welsh on 3/23/18.
 */

public interface TabVisibilityChangeListener {

    void onDisplayed(Bundle bundle);

    void onRemoved();
}
