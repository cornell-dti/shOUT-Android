package org.cornelldti.shout.speakout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.SparseArray;

import org.cornelldti.shout.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Evan Welsh on 4/9/18.
 */

public final class FilterDialogFragment extends AppCompatDialogFragment {

    private Context mContext;

    private Map<String, FilterOption> mStringToOption;
    private FilterSelectionListener mListener;

    @NonNull
    private FilterOption mCurrentFilterOption = FilterOption.ALL_REPORTS;

    private static final String CURRENT_FILTER_ARGUMENT = "current_filter", LISTENER_ARGUMENT = "listener";

    public FilterDialogFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        mStringToOption = new HashMap<>();

        for (FilterOption option : FilterOption.values()) {
            mStringToOption.put(getString(option.mResourceId), option);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        if (arguments == null) {
            throw new IllegalArgumentException("No arguments passed.");
        }

        mListener = (FilterSelectionListener) arguments.getSerializable(LISTENER_ARGUMENT);
        arguments.remove(LISTENER_ARGUMENT);

        if (mListener == null) {
            throw new IllegalArgumentException("Callback listener can't be null.");
        }

        String currentFilter = arguments.getString(CURRENT_FILTER_ARGUMENT);

        if (currentFilter != null) {
            if (mStringToOption.containsKey(currentFilter)) {
                mCurrentFilterOption = mStringToOption.get(currentFilter);
            }

            arguments.remove(CURRENT_FILTER_ARGUMENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int currentSelectionPos = mCurrentFilterOption.ordinal();

        // TODO this is a waste of time/resources ... do this earlier when we construct stringtooption

        List<String> singleChoiceItems = new ArrayList<>();

        for (FilterOption option : FilterOption.values()) {
            singleChoiceItems.add(getString(option.mResourceId));
        }

        return new AlertDialog.Builder(mContext, R.style.DialogStyle)
                .setTitle(R.string.filter_title)
                .setSingleChoiceItems(singleChoiceItems.toArray(new String[singleChoiceItems.size()]), currentSelectionPos, (dialog, position) -> {
                    final String currentOption = singleChoiceItems.get(position);

                    mCurrentFilterOption = mStringToOption.get(currentOption);
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> mListener.onFilterOptionSelected(mCurrentFilterOption))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    static FilterDialogFragment construct(@NonNull Context context, FilterSelectionListener listener, FilterOption currentFilterOption) {

        Bundle bundle = new Bundle();
        bundle.putString(CURRENT_FILTER_ARGUMENT, context.getString(currentFilterOption.mResourceId));
        bundle.putSerializable(LISTENER_ARGUMENT, listener);

        FilterDialogFragment dialog = new FilterDialogFragment();
        dialog.setRetainInstance(true);
        dialog.setArguments(bundle);
        return dialog;
    }

    interface FilterSelectionListener extends Serializable {
        void onFilterOptionSelected(FilterOption mCurrentFilterOption);
    }
}