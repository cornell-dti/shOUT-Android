package org.cornelldti.shout.speakout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.Page;
import org.cornelldti.shout.R;
import org.cornelldti.shout.ReportViewDialog;
import org.cornelldti.shout.ShoutFirestore;
import org.cornelldti.shout.ShoutRealtimeDatabase;
import org.cornelldti.shout.util.function.BiConsumer;
import org.cornelldti.shout.util.function.Consumer;

/**
 * An optimized and reloaded version of the original SpeakOutAdapter
 */

public class SpeakOutAdapter extends RecyclerView.Adapter<SpeakOutAdapter.ReportViewHolder> implements DataLoadedCallback {
    private InternalAdapter storiesAdapter, allAdapter;
    private boolean filterStories = false; // TODO Support any filter.

    private SpeakOutAdapter() {
    }

    /* Constants for filtering... */

    public static final int FILTER_STORIES = 0;
    public static final int FILTER_NONE = -1;


    private FirestoreRecyclerAdapter<Report, ReportViewHolder> adapter() {
        return filterStories ? storiesAdapter : allAdapter;
    }

    private InternalAdapter internalAdapter() {
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

    public String getId(int position) {
        return internalAdapter().posToId.get(position);
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
     * Constructs a SpeakOutAdapter
     *
     * @param fragment - The fragment to bind the firebase data listening lifecycle to.
     * @param context  - The context to determine date/time formatting within.
     * @return - A new SpeakOutAdapter
     */
    static SpeakOutAdapter construct(Fragment fragment, BiConsumer<SpeakOutAdapter, ReportViewHolder> clickListener, Context context) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference ref = firestore.collection(ShoutFirestore.REPORTS_COLLECTION);

        Query stories = ref.whereEqualTo(Report.HAS_BODY, true).orderBy(Report.TIMESTAMP, Query.Direction.DESCENDING).limit(100);
        Query all = ref.orderBy(Report.TIMESTAMP, Query.Direction.DESCENDING).limit(100);

        FirestoreRecyclerOptions<Report> storiesOptions = new FirestoreRecyclerOptions.Builder<Report>()
                .setQuery(stories, Report.class)
                .setLifecycleOwner(fragment)
                .build();
        FirestoreRecyclerOptions<Report> allOptions = new FirestoreRecyclerOptions.Builder<Report>()
                .setQuery(all, Report.class)
                .setLifecycleOwner(fragment)
                .build();

        SpeakOutAdapter adapter = new SpeakOutAdapter();
        adapter.storiesAdapter = new InternalAdapter(
                context,
                reportViewHolder -> clickListener.apply(adapter, reportViewHolder),
                adapter,
                storiesOptions
        );

        adapter.allAdapter = new InternalAdapter(
                context,
                reportViewHolder -> clickListener.apply(adapter, reportViewHolder),
                adapter,
                allOptions
        );

        return adapter;
    }


    /**
     * An implementation of ViewHolder which represents a report.
     */
    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, date, time, location;

        transient Report report;

        ReportViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.feed_item_title);
            body = v.findViewById(R.id.feed_item_body);
            date = v.findViewById(R.id.feed_item_date);
            time = v.findViewById(R.id.feed_item_time);
            location = v.findViewById(R.id.feed_item_location);
        }
    }

    private static class InternalAdapter extends FirestoreRecyclerAdapter<Report, ReportViewHolder> {

        private SparseArray<String> posToId = new SparseArray<>();

        private final java.text.DateFormat dateFormatter, timeFormatter;
        private DataLoadedCallback dataLoadedCallback;
        private Consumer<ReportViewHolder> onItemClickListener;

        /**
         * Create storiesAdapter new RecyclerView adapter that listens to storiesAdapter Firestore Query.  See {@link
         * FirestoreRecyclerOptions} for configuration options.
         *
         * @param context            - The context date/time formatting is decided within.
         * @param dataLoadedCallback - The dataLoadedCallback to call when initial data has been loaded.
         * @param options            - The options to construct this Firestore adapter with.
         */
        private InternalAdapter(Context context, Consumer<ReportViewHolder> onItemClickListener, DataLoadedCallback dataLoadedCallback, @NonNull FirestoreRecyclerOptions<Report> options) {
            super(options);

            this.dataLoadedCallback = dataLoadedCallback;
            this.onItemClickListener = onItemClickListener;

            this.dateFormatter = DateFormat.getDateFormat(context);
            this.timeFormatter = DateFormat.getTimeFormat(context);
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View feedItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);

            final ReportViewHolder holder = new ReportViewHolder(feedItemView);

            feedItemView.setOnClickListener(view -> onItemClickListener.apply(holder));

            return holder;
        }

        @Override
        protected void onBindViewHolder(@NonNull ReportViewHolder holder, int position, @NonNull Report model) {
            holder.report = model;

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
            posToId.put(newIndex, snapshot.getId());

            super.onChildChanged(type, snapshot, newIndex, oldIndex);
        }

        @Override
        public void onDataChanged() {
            this.dataLoadedCallback.dataLoaded();
            super.onDataChanged();
        }
    }
}

