package org.cornelldti.shout.speakout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.cornelldti.shout.R;

import java.io.Serializable;

/**
 * Created by Evan Welsh on 4/9/18.
 */

public final class FilterDialog extends DialogFragment {

    private Context mContext;
    private FilterSelectionListener mListener;
    private ListView mListView;

    @NonNull
    private FilterOption mCurrentFilterOption = FilterOption.ALL_REPORTS;

    public FilterDialog() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalArgumentException("No arguments passed.");
        }

        mListener = (FilterSelectionListener) getArguments().getSerializable("listener");
        getArguments().remove("listener");

        if (mListener == null) {
            throw new IllegalArgumentException("Callback listener can't be null.");
        }
    }

    private static final String ALL_REPORTS_OPTION = "All Reports";
    private static final String STORIES_ONLY_OPTION = "Stories Only";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int currentSelectionPos = mCurrentFilterOption.ordinal();

        @SuppressLint("InflateParams")
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.filter_dialog, null);

        mListView = customView.findViewById(R.id.filter_options_list);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setSelection(currentSelectionPos);

        mListView.setAdapter(new ArrayAdapter<>(
                mContext,
                android.R.layout.select_dialog_singlechoice,
                new String[]{
                        ALL_REPORTS_OPTION,
                        STORIES_ONLY_OPTION
                })
        );

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            final String currentOption = (String) mListView.getAdapter().getItem(position);

            switch (currentOption) {
                case ALL_REPORTS_OPTION:
                    mCurrentFilterOption = FilterOption.ALL_REPORTS;
                    break;
                case STORIES_ONLY_OPTION:
                    mCurrentFilterOption = FilterOption.ALL_REPORTS;
                    break;
            }
        });


        return new AlertDialog.Builder(mContext)
                .setTitle("Filter")
                .setView(customView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> mListener.onFilterOptionSelected(mCurrentFilterOption))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    public static FilterDialog construct(FilterSelectionListener listener, FilterOption currentFilterOption) {

        Bundle bundle = new Bundle();
        bundle.putString("current_filter", currentFilterOption == FilterOption.ALL_REPORTS ? ALL_REPORTS_OPTION : STORIES_ONLY_OPTION);
        bundle.putSerializable("listener", listener);

        FilterDialog dialog = new FilterDialog();
        dialog.setRetainInstance(true);
        dialog.setArguments(bundle);
        return dialog;
    }

    interface FilterSelectionListener extends Serializable {
        void onFilterOptionSelected(FilterOption mCurrentFilterOption);
    }
}