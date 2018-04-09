package org.cornelldti.shout;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Evan Welsh on 4/3/18.
 */
public class OfflineFragment extends AppCompatDialogFragment {
    private RefreshCallback refreshCallback;

    interface RefreshCallback {
        void refresh();
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof RefreshCallback) {
            refreshCallback = (RefreshCallback) context;
        }

        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.offline_layout, container, false);

        Button button = view.findViewById(R.id.offline_refresh_button);

        button.setOnClickListener(l -> {
            if (refreshCallback != null) {
                refreshCallback.refresh();
            }
        });

        return view;
    }

}
