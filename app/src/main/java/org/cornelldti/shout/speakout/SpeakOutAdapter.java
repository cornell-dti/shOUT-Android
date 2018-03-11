package org.cornelldti.shout.speakout;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import org.cornelldti.shout.R;

import java.util.HashMap;
import java.util.Map;


/**
 * A wrapper around two individual Adapter's which either provide a stories-only view or an everything-view.
 *
 * @see SpeakOutAdapter#filter(boolean)
 * <p>
 * Created by Evan on 3/10/18
 */
@Deprecated
public class SpeakOutAdapter extends RecyclerView.Adapter<SpeakOutAdapter.ReportViewHolder> {

    private final FirebaseRecyclerAdapter<ApprovedReport, ReportViewHolder> adapter;
    private final StoriesAdapter storiesAdapter = new StoriesAdapter();

    /* The adapter currently wrapped by this class. */
    private RecyclerView.Adapter<SpeakOutAdapter.ReportViewHolder> currentAdapter;

    private boolean loading, canStopLoading;

    private final java.text.DateFormat dateFormatter, timeFormatter;

    private SpeakOutAdapter(Context context, FirebaseRecyclerOptions<ApprovedReport> options, boolean isLoading) {
        this.loading = isLoading;

        this.adapter = construct(options);
        this.currentAdapter = this.adapter;

        this.dateFormatter = DateFormat.getDateFormat(context);
        this.timeFormatter = DateFormat.getTimeFormat(context);
    }

    /**
     * Enables or disables stories filtering.
     *
     * @param stories - true to only show stories, false to show all reports.
     */
    public void filter(boolean stories) {
        if (stories) {
            this.currentAdapter = storiesAdapter;
        } else {
            this.currentAdapter = adapter;
        }

        notifyDataSetChanged();
    }

    /**
     * Refreshes the currently displayed reports.
     * NOTE: The data is already local... it is just being displayed.
     */
    public void refreshItems() {
        notifyDataSetChanged();
    }

    public void setIsLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean canStopLoading() {
        return canStopLoading;
    }

    /* ViewHolder Implementation */

    // TODO Create body-less version

    /**
     * An implementation of ViewHolder which represents a report.
     */
    public static class ReportViewHolder extends RecyclerView.ViewHolder {
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

    /* RecyclerView Adapter Implementation for Stories */

    /**
     * An implementation of the RecyclerView Adapter which filters and caches "stories" from
     * the current FirebaseRecyclerAdapter
     */
    private class StoriesAdapter extends RecyclerView.Adapter<SpeakOutAdapter.ReportViewHolder> {
        private SparseArray<ApprovedReport> stories = new SparseArray<>();

        private int numberOfStories;
        private boolean cached;

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
            return new ReportViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            ApprovedReport model = stories.get(position);

            holder.title.setText(model.getTitle());
            holder.body.setText(model.getBody());
            holder.location.setText(model.getLocation());
            holder.date.setText(dateFormatter.format(model.getTimestamp()));
            holder.time.setText(timeFormatter.format(model.getTimestamp()));
        }

        @Override
        public int getItemCount() {
            if (!cached) {
                int numberOfStories = 0;

                for (int i = 0; i < adapter.getItemCount(); i++) {
                    ApprovedReport report = adapter.getItem(i);

                    if (report.getHasBody()) {
                        stories.put(numberOfStories, report);
                        numberOfStories++;
                    }
                }

                this.numberOfStories = numberOfStories;
                this.cached = true;
            }

            return this.numberOfStories;
        }

        // TODO make sure this is always invalidated if needed
        private void invalidateCache() {
            cached = false;
        }

        private void wrap_notifyDataSetChanged() {
            invalidateCache();
            notifyDataSetChanged();
        }

        private void wrap_notifyItemChanged(int index) {
            invalidateCache();
            notifyItemChanged(index);
        }

        private void wrap_notifyItemInserted(int index) {
            invalidateCache();
            notifyItemInserted(index);
        }

        private void wrap_notifyItemRemoved(int index) {
            invalidateCache();
            notifyItemRemoved(index);
        }

        private void wrap_notifyItemMoved(int oldIndex, int newIndex) {
            invalidateCache();
            notifyItemMoved(oldIndex, newIndex);
        }
    }

    /* STATIC CONSTRUCTOR */

    // TODO Handle Firebase query internally

    /**
     * Creates a new SpeakOutAdapter given a valid context, fragment, and Firebase query.
     *
     * @param context  - The context to create the adapter within. Determines the date/time formatting.
     * @param fragment - The fragment to bind the Firebase listeners' lifecycle to.
     * @param query    - The query to get a list of ApprovedReports.
     * @return - A new SpeakOutAdapter instance.
     */
    static SpeakOutAdapter construct(Context context, Fragment fragment, Query query) {
        FirebaseRecyclerOptions<ApprovedReport> options = new FirebaseRecyclerOptions.Builder<ApprovedReport>()
                .setQuery(query, ApprovedReport.class)
                .setLifecycleOwner(fragment)
                .build();
        // TODO Don't hard code isLoading
        return new SpeakOutAdapter(context, options, true);
    }

    /* WRAPPER METHODS */

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        currentAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return currentAdapter.getItemCount();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return currentAdapter.onCreateViewHolder(parent, viewType);
    }

    /* UTILITY METHODS */

    /**
     * Creates a FirebaseRecyclerAdapter which handles all data-syncing for reports and
     * forwards the created FirebaseRecyclerAdapter's data update events to the base class
     * and stories adapter.
     *
     * @param options - The Firebase options to use.
     * @return - A new FirebaseRecyclerAdapter implementation.
     */
    private FirebaseRecyclerAdapter<ApprovedReport, ReportViewHolder> construct(FirebaseRecyclerOptions<ApprovedReport> options) {
        return new FirebaseRecyclerAdapter<ApprovedReport, ReportViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReportViewHolder holder, int position, @NonNull ApprovedReport model) {
                holder.title.setText(model.getTitle());
                holder.body.setText(model.getBody());
                holder.location.setText(model.getLocation());
                holder.date.setText(dateFormatter.format(model.getTimestamp()));
                holder.time.setText(timeFormatter.format(model.getTimestamp()));
            }

            @NonNull
            @Override
            public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
                return new ReportViewHolder(v);
            }

            @Override
            public void onChildChanged(@NonNull ChangeEventType type,
                                       @NonNull DataSnapshot snapshot,
                                       int newIndex,
                                       int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);

                if (loading) {
                    switch (type) {
                        case ADDED:
                            // TODO Decide whether pull or auto is better UX
                            // TODO Also, switching tabs will auto-refresh right now
                            SpeakOutAdapter.this.notifyItemInserted(newIndex);
                            canStopLoading = true;
                            break;
                        case CHANGED:
                            SpeakOutAdapter.this.notifyItemChanged(newIndex);
                            canStopLoading = true;
                            break;
                        case REMOVED:
                            SpeakOutAdapter.this.notifyItemRemoved(newIndex);
                            canStopLoading = true;
                            break;
                        case MOVED:
                            SpeakOutAdapter.this.notifyItemMoved(oldIndex, newIndex);
                            canStopLoading = true;
                            break;
                        default:
                            throw new IllegalStateException("Incomplete case statement");
                    }
                }

                switch (type) {
                    case ADDED:
                        storiesAdapter.wrap_notifyItemInserted(newIndex);
                        break;
                    case CHANGED:
                        storiesAdapter.wrap_notifyItemChanged(newIndex);
                        break;
                    case REMOVED:
                        storiesAdapter.wrap_notifyItemRemoved(newIndex);
                        break;
                    case MOVED:
                        storiesAdapter.wrap_notifyItemMoved(oldIndex, newIndex);
                        break;
                    default:
                        throw new IllegalStateException("Incomplete case statement");
                }
            }

            @Override
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void stopListening() {
                super.stopListening();
                storiesAdapter.wrap_notifyDataSetChanged();
                SpeakOutAdapter.this.notifyDataSetChanged();
            }
        };
    }

}
