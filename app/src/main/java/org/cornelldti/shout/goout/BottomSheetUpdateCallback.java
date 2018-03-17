package org.cornelldti.shout.goout;

import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Evan Welsh on 3/17/18.
 */

public interface BottomSheetUpdateCallback {

    void update(LinearLayout bottomSheet,
                BottomSheetBehavior behavior,
                RecyclerView reportsView,
                TextView addressTextView,
                TextView numberOfReportsTextView
    );
}
