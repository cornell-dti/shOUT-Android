package org.cornelldti.shout.goout;

import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Evan Welsh on 3/17/18.
 */

public interface BottomSheetUpdateCallback {

    void update(LinearLayout bottomSheet,
                BottomSheetBehavior behavior,
                View mBottomSheetShadow, RecyclerView reportsView,
                TextView addressTextView,
                TextView numberOfReportsTextView
    );
}
