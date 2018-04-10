package org.cornelldti.shout.speakout;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.R;
import org.cornelldti.shout.ShoutFirestore;
import org.cornelldti.shout.util.function.BiConsumer;
import org.cornelldti.shout.util.function.Consumer;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An optimized and reloaded version of the original SpeakOutAdapter
 */

public class SpeakOutAdapter extends RecyclerView.Adapter<SpeakOutAdapter.ReportViewHolder> implements DataLoadedCallback {
    private InternalAdapter storiesAdapter, allAdapter;
    private boolean filterStories = false; // TODO Support any filter.

    private SpeakOutAdapter() {
    }

    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean isLoading;
    private boolean isAllLoaded = false; // todo

    public RecyclerView.OnScrollListener listener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

                lastVisibleItem = llm.findLastVisibleItemPosition();
                totalItemCount = llm.getItemCount();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold) && !isAllLoaded) {

                    adapter().loadMore(visibleThreshold);

                    isLoading = true;
                }
            }
        };
    }

    private static final int DEFAULT_LIMIT = 20;


    private InternalAdapter adapter() {
        return filterStories ? storiesAdapter : allAdapter;
    }

    /**
     * Switches the utilized adapter, effectively "filtering" the recyclerview.
     *
     * @param flag - Either FILTER_STORIES or FILTER_NONE.
     */
    public void filter(FilterOption flag) {
        switch (flag) {
            case ALL_REPORTS:
                filterStories = false;
                break;
            case STORIES_ONLY:
                filterStories = true;
                break;
            default:
                throw new RuntimeException("Unknown filter passed.");
        }

        notifyDataSetChanged();
    }

    /**
     * Refreshes the items currently being displayed.
     * Always called when the adapter changes.
     */
    public void refreshItems(Consumer<Boolean> refreshComplete) {
        adapter().refresh(refreshComplete);
    }

    public String getId(int position) {
        return adapter().posToId.get(position);
    }

    /**
     * Callback for when a sub-adapter has loaded its data.
     */
    @Override
    public void dataLoaded() {
        notifyDataSetChanged();
    }

    /* Wrapper methods around the current adapter (stories or all) */

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       /* if (viewType == 2) {
            View loadMoreItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_load_more_item, parent, false);

            final LoadMoreViewHolder loadMoreViewHolder = new LoadMoreViewHolder(loadMoreItemView);

            loadMoreItemView.setOnClickListener(view -> {

            });

            return loadMoreViewHolder;
        } else {*/
        return adapter().onCreateViewHolder(parent, viewType);
        // TODO
        //}
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        //if (holder instanceof ReportViewHolder) {
        adapter().bindViewHolder(holder, position);
        //}
    }

    @Override
    public int getItemCount() {
        return adapter().getItemCount();
    }

//
    //  @Override
    //public int getItemViewType(int position) {
    //  return 1;
    //}

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

        Query stories = ref.whereEqualTo(Report.HAS_BODY, true).orderBy(Report.TIMESTAMP, Query.Direction.DESCENDING);
        Query all = ref.orderBy(Report.TIMESTAMP, Query.Direction.DESCENDING);


        SpeakOutAdapter adapter = new SpeakOutAdapter();
        adapter.storiesAdapter = new InternalAdapter(
                context,
                reportViewHolder -> clickListener.apply(adapter, reportViewHolder),
                adapter,
                stories
        );

        adapter.allAdapter = new InternalAdapter(
                context,
                reportViewHolder -> clickListener.apply(adapter, reportViewHolder),
                adapter,
                all
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

    private static class InternalAdapter extends RecyclerView.Adapter<ReportViewHolder> implements LifecycleObserver {

        private static final String TAG = "InternalAdapter-SpeakOut";


        private final Query mBaseQuery;
        private Query mCurrentQuery;
        private SparseArray<String> posToId = new SparseArray<>();

        private final java.text.DateFormat dateFormatter, timeFormatter;
        private DataLoadedCallback dataLoadedCallback;
        private Consumer<ReportViewHolder> onItemClickListener;

        private SparseArray<Report> mReports = new SparseArray<>();
        private DocumentSnapshot lastVisible;


        /**
         * Create storiesAdapter new RecyclerView adapter that listens to storiesAdapter Firestore Query.  See {@link
         * FirestoreRecyclerOptions} for configuration options.
         *
         * @param context            - The context date/time formatting is decided within.
         * @param dataLoadedCallback - The dataLoadedCallback to call when initial data has been loaded.
         * @param options            - The options to construct this Firestore adapter with.
         */
        private InternalAdapter(Context context, Consumer<ReportViewHolder> onItemClickListener, @NonNull DataLoadedCallback dataLoadedCallback, @NonNull Query query) {
            mBaseQuery = query;

            mCurrentQuery = mBaseQuery.limit(DEFAULT_LIMIT);

            mCurrentQuery.addSnapshotListener((documentSnapshots, e) -> {
                if (e == null) {
                    int position = 0;

                    for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                        Report report = document.toObject(Report.class);
                        posToId.put(position, document.getId());
                        mReports.put(position++, report);


                        lastVisible = document;
                    }

                    dataLoadedCallback.dataLoaded();
                }
            });

            this.dataLoadedCallback = dataLoadedCallback;
            this.onItemClickListener = onItemClickListener;

            this.dateFormatter = DateFormat.getDateFormat(context);
            this.timeFormatter = DateFormat.getTimeFormat(context);
        }

        public void refresh() {
            refresh((success) -> {
            });
        }

        public void refresh(@NonNull Consumer<Boolean> refreshComplete) {
            mCurrentQuery = mBaseQuery.endAt(lastVisible);

            SparseArray<Report> reports = new SparseArray<>();
            AtomicReference<DocumentSnapshot> lastVisible = new AtomicReference<>();

            mCurrentQuery.addSnapshotListener((documentSnapshots, e) -> {
                if (e == null) {
                    int position = 0;

                    List<DocumentSnapshot> documents = documentSnapshots.getDocuments();

                    for (DocumentSnapshot document : documents) {
                        Report report = document.toObject(Report.class);
                        posToId.put(position, document.getId());
                        reports.put(position++, report);
                    }

                    mReports = reports;
                    this.lastVisible = documents.get(documents.size() - 1);

                    refreshComplete.apply(true);

                    dataLoadedCallback.dataLoaded();

                    notifyDataSetChanged();
                } else {
                    refreshComplete.apply(false);
                }
            });
        }

        public void loadMore(int amount) {
            mCurrentQuery = mBaseQuery.limit(amount + 1).startAt(lastVisible);

            mCurrentQuery.addSnapshotListener((documentSnapshots, e) -> {
                if (e == null) {
                    int position = mReports.size() - 1;

                    List<DocumentSnapshot> documents = documentSnapshots.getDocuments();

                    if (documents.get(0).equals(lastVisible)) {
                        for (DocumentSnapshot document : documents) {
                            Report report = document.toObject(Report.class);
                            posToId.put(position, document.getId());
                            mReports.put(position++, report);

                            lastVisible = document;
                        }

                        notifyItemRangeChanged(position, amount + 1);

                        dataLoadedCallback.dataLoaded();
                    } else {
                        // TODO print error
                        refresh();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mReports.size();
        }


        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder viewHolder, int position) {
            Report model = mReports.get(position);

            viewHolder.report = model;

            viewHolder.title.setText(model.getTitle());

            String body = model.getBody();

            if (!TextUtils.isEmpty(body)) {
                viewHolder.body.setText(body);
                viewHolder.body.setVisibility(View.VISIBLE);
            } else {
                viewHolder.body.setVisibility(View.GONE);
            }

            String location = model.getLocation();

            if (!TextUtils.isEmpty(location)) {
                viewHolder.location.setText(location);
                viewHolder.location.setVisibility(View.VISIBLE);
            } else {
                viewHolder.location.setVisibility(View.GONE);
            }

            viewHolder.date.setText(dateFormatter.format(model.getTimestamp()));
            viewHolder.time.setText(timeFormatter.format(model.getTimestamp()));
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View feedItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);

            final ReportViewHolder holder = new ReportViewHolder(feedItemView);

            feedItemView.setOnClickListener(view -> onItemClickListener.apply(holder));

            return holder;
        }
    }
}

