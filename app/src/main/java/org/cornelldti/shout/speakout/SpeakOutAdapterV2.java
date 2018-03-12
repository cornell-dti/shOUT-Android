package org.cornelldti.shout.speakout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.R;

/**
 * An optimized and reloaded version of the original SpeakOutAdapter
 */

public class SpeakOutAdapterV2 extends RecyclerView.Adapter<SpeakOutAdapterV2.ReportViewHolder> implements DataLoadedCallback {
    private FirestoreRecyclerAdapter<ApprovedReport, ReportViewHolder> storiesAdapter, allAdapter;
    private boolean filterStories = false; // TODO Support any filter.

    private SpeakOutAdapterV2() {
    }

    /* Constants for filtering... */

    public static final int FILTER_STORIES = 0;
    public static final int FILTER_NONE = -1;


    private FirestoreRecyclerAdapter<ApprovedReport, ReportViewHolder> adapter() {
        return filterStories ? storiesAdapter : allAdapter;
    }

    /**
     * Switches the utilized adapter, effectively "filtering" the recyclerview.
     *
     * @param flag - Either FILTER_STORIES or FILTER_NONE.
     */
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

    /**
     * Refreshes the items currently being displayed.
     * Always called when the adapter changes.
     */
    public void refreshItems() {
        adapter().startListening(); // TODO check if this call is actually helpful.
        notifyDataSetChanged();
    }

    /**
     * Callback for when a sub-adapter has loaded its data.
     */
    @Override
    public void dataLoaded() {
        refreshItems();
    }

    /* Wrapper methods around the current adapter (stories or all) */

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return adapter().onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        adapter().bindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return adapter().getItemCount();
    }

    /**
     * Constructs a SpeakOutAdapterV2
     *
     * @param fragment - The fragment to bind the firebase data listening lifecycle to.
     * @param stories  - The query to retrieve stories.
     * @param all      - The query to retrieve all reports.
     * @param context  - The context to determine date/time formatting within.
     * @return - A new SpeakOutAdapterV2
     */
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


    /**
     * An implementation of ViewHolder which represents a report.
     */
    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, date, time, location;

        ReportViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.feed_item_title);
            body = v.findViewById(R.id.feed_item_body);
            date = v.findViewById(R.id.feed_item_date);
            time = v.findViewById(R.id.feed_item_time);
            location = v.findViewById(R.id.feed_item_location);
        }
    }

    private static class InternalAdapter extends FirestoreRecyclerAdapter<ApprovedReport, ReportViewHolder> {

        private final java.text.DateFormat dateFormatter, timeFormatter;
        private DataLoadedCallback callback;

        /**
         * Create storiesAdapter new RecyclerView adapter that listens to storiesAdapter Firestore Query.  See {@link
         * FirestoreRecyclerOptions} for configuration options.
         *
         * @param context  - The context date/time formatting is decided within.
         * @param callback - The callback to call when initial data has been loaded.
         * @param options  - The options to construct this Firestore adapter with.
         */
        private InternalAdapter(Context context, DataLoadedCallback callback, @NonNull FirestoreRecyclerOptions<ApprovedReport> options) {
            super(options);

            this.callback = callback;

            this.dateFormatter = DateFormat.getDateFormat(context);
            this.timeFormatter = DateFormat.getTimeFormat(context);
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
            return new ReportViewHolder(v);

        }

        @Override
        protected void onBindViewHolder(@NonNull ReportViewHolder holder, int position, @NonNull ApprovedReport model) {
            holder.title.setText(model.getTitle());

            String body = model.getBody();

            if (!TextUtils.isEmpty(body)) {
                holder.body.setText(body);
                holder.body.setVisibility(View.VISIBLE);
            } else {
                holder.body.setVisibility(View.GONE);
            }

            String location = model.getLocation();

            if (!TextUtils.isEmpty(location)) {
                holder.location.setText(location);
                holder.location.setVisibility(View.VISIBLE);
            } else {
                holder.location.setVisibility(View.GONE);
            }

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

