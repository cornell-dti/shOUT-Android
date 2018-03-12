package org.cornelldti.shout.speakout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.R;

/**
 * An optimized and reloaded version of the original SpeakOutAdapter
 */

public class SpeakOutAdapterV2 extends RecyclerView.Adapter<SpeakOutAdapter.ReportViewHolder> implements DataLoadedCallback {
    private FirestoreRecyclerAdapter<ApprovedReport, SpeakOutAdapter.ReportViewHolder> storiesAdapter, allAdapter;

    private SpeakOutAdapterV2() {
    }

    public static final int FILTER_STORIES = 0;
    public static final int FILTER_NONE = -1;

    public boolean filterStories = false;

    private FirestoreRecyclerAdapter<ApprovedReport, SpeakOutAdapter.ReportViewHolder> adapter() {
        return filterStories ? storiesAdapter : allAdapter;
    }

    public void filter(int flag) {
        switch (flag) {
            case FILTER_NONE:
                filterStories = false;
                break;
            case FILTER_STORIES:
                filterStories = true;
                break;
            default:
                throw new RuntimeException("Unknown filter passed.");
        }

        refreshItems();
    }

    public void refreshItems() {
        adapter().startListening();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpeakOutAdapter.ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return adapter().onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull SpeakOutAdapter.ReportViewHolder holder, int position) {
        adapter().bindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return adapter().getItemCount();
    }

    static SpeakOutAdapterV2 construct(Fragment fragment, Query stories, Query all, Context context) {
        FirestoreRecyclerOptions<ApprovedReport> storiesOptions = new FirestoreRecyclerOptions.Builder<ApprovedReport>()
                .setQuery(stories, ApprovedReport.class)
                .setLifecycleOwner(fragment)
                .build();
        FirestoreRecyclerOptions<ApprovedReport> allOptions = new FirestoreRecyclerOptions.Builder<ApprovedReport>()
                .setQuery(all, ApprovedReport.class)
                .setLifecycleOwner(fragment)
                .build();
        SpeakOutAdapterV2 adapter = new SpeakOutAdapterV2();
        adapter.storiesAdapter = new InternalAdapter(context, adapter, storiesOptions);
        adapter.allAdapter = new InternalAdapter(context, adapter, allOptions);
        return adapter;
    }

    @Override
    public void dataLoaded() {
        refreshItems();
    }


    private static class InternalAdapter extends FirestoreRecyclerAdapter<ApprovedReport, SpeakOutAdapter.ReportViewHolder> {

        private final java.text.DateFormat dateFormatter, timeFormatter;
        private DataLoadedCallback callback;

        /**
         * Create storiesAdapter new RecyclerView adapter that listens to storiesAdapter Firestore Query.  See {@link
         * FirestoreRecyclerOptions} for configuration options.
         *
         * @param options
         */
        private InternalAdapter(Context context, DataLoadedCallback callback, @NonNull FirestoreRecyclerOptions<ApprovedReport> options) {
            super(options);

            this.callback = callback;

            this.dateFormatter = DateFormat.getDateFormat(context);
            this.timeFormatter = DateFormat.getTimeFormat(context);
        }

        @NonNull
        @Override
        public SpeakOutAdapter.ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
            return new SpeakOutAdapter.ReportViewHolder(v);

        }

        @Override
        protected void onBindViewHolder(@NonNull SpeakOutAdapter.ReportViewHolder holder, int position, @NonNull ApprovedReport model) {
            holder.title.setText(model.getTitle());
            holder.body.setText(model.getBody());
            holder.location.setText(model.getLocation());
            holder.date.setText(dateFormatter.format(model.getTimestamp()));
            holder.time.setText(timeFormatter.format(model.getTimestamp()));
        }

        @Override
        public void onChildChanged(@NonNull ChangeEventType type,
                                   @NonNull DocumentSnapshot snapshot,
                                   int newIndex,
                                   int oldIndex) {
            super.onChildChanged(type, snapshot, newIndex, oldIndex);
        }

        @Override
        public void onDataChanged() {
            this.callback.dataLoaded();
            super.onDataChanged();
        }
    }
}

