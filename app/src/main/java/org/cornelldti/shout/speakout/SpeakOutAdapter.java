package org.cornelldti.shout.speakout;

import android.arch.core.util.Function;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.support.annotation.NonNull;
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

/**
 * An optimized and reloaded version of the original SpeakOutAdapter
 */

public class SpeakOutAdapter extends RecyclerView.Adapter<SpeakOutAdapter.ReportViewHolder> implements DataLoadedCallback {
    private final CollectionReference mFirestore;
    private final AdapterChangedCallback mAdapterChangedCallback;
    private final DataLoadedCallback mDataLoadedCallback;
    private Context mContext;
    private InternalAdapter mAdapter;
    private BiConsumer<SpeakOutAdapter, ReportViewHolder> mClickListener;

    private static final int DEFAULT_LIMIT = 20;

    private static final Function<CollectionReference, Query> ALL_QUERY = ref -> {
        return ref.orderBy(Report.TIMESTAMP, Query.Direction.DESCENDING);
    };

    private static final Function<CollectionReference, Query> STORIES_QUERY = ref -> {
        return ref
                .whereEqualTo(Report.HAS_BODY, true)
                .orderBy(Report.TIMESTAMP, Query.Direction.DESCENDING);
    };


    private SpeakOutAdapter(Context context, AdapterChangedCallback adapterChangedCallback, DataLoadedCallback callback, BiConsumer<SpeakOutAdapter, ReportViewHolder> clickListener) {
        mContext = context;
        mDataLoadedCallback = callback;
        mClickListener = clickListener;
        mAdapterChangedCallback = adapterChangedCallback;
        mFirestore = FirebaseFirestore.getInstance().collection(ShoutFirestore.REPORTS_COLLECTION);

        mAdapter = constructAdapter(ALL_QUERY.apply(mFirestore));
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


    private InternalAdapter adapter() {
        return mAdapter;
    }

    /**
     * Switches the utilized adapter, effectively "filtering" the recyclerview.
     *
     * @param flag - Either FILTER_STORIES or FILTER_NONE.
     */
    public void filter(FilterOption flag) {
        mAdapterChangedCallback.adapterChanged();

        switch (flag) {
            case ALL_REPORTS:
                mAdapter = constructAdapter(ALL_QUERY.apply(mFirestore));
                break;
            case STORIES_ONLY:
                mAdapter = constructAdapter(STORIES_QUERY.apply(mFirestore));
                break;
            default:
                throw new RuntimeException("Unknown filter passed.");
        }
    }

    /**
     * Refreshes the items currently being displayed.
     * Always called when the adapter changes.
     */
    public void refreshItems(Consumer<Boolean> refreshComplete) {
        adapter().refresh(refreshComplete);
    }

    /**
     * Gets the document id for the position in the recycler view.
     *
     * @param position
     * @return
     */
    public String getDocumentId(int position) {
        return adapter().mPosToId.get(position);
    }

    /**
     * Callback for when a sub-adapter has loaded its data.
     */
    @Override
    public void dataLoaded() {
        mDataLoadedCallback.dataLoaded();

        notifyDataSetChanged();
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

    private InternalAdapter constructAdapter(Query query) {
        return new InternalAdapter(
                mContext,
                reportViewHolder -> mClickListener.apply(this, reportViewHolder),
                this,
                query
        );
    }

    /**
     * Constructs a SpeakOutAdapter
     *
     * @param fragment - The fragment to bind the firebase data listening lifecycle to.
     * @param context  - The context to determine date/time formatting within.
     * @return - A new SpeakOutAdapter
     */
    static SpeakOutAdapter construct(AdapterChangedCallback adapterChangedCallback, DataLoadedCallback callback, BiConsumer<SpeakOutAdapter, ReportViewHolder> clickListener, Context context) {
        return new SpeakOutAdapter(context, adapterChangedCallback, callback, clickListener);
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

    // TODO clean this up

    private static class InternalAdapter extends RecyclerView.Adapter<ReportViewHolder> implements LifecycleObserver {

        private static final String TAG = "InternalAdapterSpeakOut";

        private final Query mBaseQuery;
        private Query mCurrentQuery;
        private SparseArray<String> mPosToId = new SparseArray<>();

        private final java.text.DateFormat mDateFormatter, mTimeFormatter;
        private DataLoadedCallback mDataLoadedCallback;
        private Consumer<ReportViewHolder> mOnItemClickListener;

        private SparseArray<Report> mReports = new SparseArray<>();
        private DocumentSnapshot mLastVisible;


        /**
         * Create storiesAdapter new RecyclerView adapter that listens to storiesAdapter Firestore Query.  See {@link
         * FirestoreRecyclerOptions} for configuration options.
         *
         * @param context            - The context date/time formatting is decided within.
         * @param dataLoadedCallback - The mDataLoadedCallback to call when initial data has been loaded.
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

                        mPosToId.put(position, document.getId());
                        mReports.put(position++, report);


                        mLastVisible = document;
                    }

                    dataLoadedCallback.dataLoaded();
                }
            });

            mDataLoadedCallback = dataLoadedCallback;
            mOnItemClickListener = onItemClickListener;

            mDateFormatter = DateFormat.getDateFormat(context);
            mTimeFormatter = DateFormat.getTimeFormat(context);
        }

        void refresh() {
            refresh((success) -> {
            });
        }

        void refresh(@NonNull Consumer<Boolean> refreshComplete) {
            if (mBaseQuery == null || mLastVisible == null) {
                refreshComplete.apply(false);
                return;
            }

            mCurrentQuery = mBaseQuery.endAt(mLastVisible);

            SparseArray<Report> reports = new SparseArray<>();

            mCurrentQuery.addSnapshotListener((documentSnapshots, e) -> {
                if (e == null) {
                    int position = 0;

                    List<DocumentSnapshot> documents = documentSnapshots.getDocuments();

                    for (DocumentSnapshot document : documents) {
                        Report report = document.toObject(Report.class);
                        mPosToId.put(position, document.getId());
                        reports.put(position++, report);
                    }

                    mReports = reports;
                    this.mLastVisible = documents.get(documents.size() - 1);

                    refreshComplete.apply(true);

                    mDataLoadedCallback.dataLoaded();

                    notifyDataSetChanged();
                } else {
                    refreshComplete.apply(false);
                }
            });
        }

        public void loadMore(int amount) {
            if (mBaseQuery == null || mLastVisible == null) {
                return;
            }

            mCurrentQuery = mBaseQuery.limit(amount + 1).startAt(mLastVisible);

            mCurrentQuery.addSnapshotListener((documentSnapshots, e) -> {
                if (e == null) {
                    int position = mReports.size() - 1;

                    List<DocumentSnapshot> documents = documentSnapshots.getDocuments();

                    if (documents.get(0).equals(mLastVisible)) {
                        for (DocumentSnapshot document : documents) {
                            Report report = document.toObject(Report.class);
                            mPosToId.put(position, document.getId());
                            mReports.put(position++, report);

                            mLastVisible = document;
                        }

                        notifyItemRangeChanged(position, amount + 1);

                        mDataLoadedCallback.dataLoaded();
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

            viewHolder.date.setText(mDateFormatter.format(model.getTimestamp()));
            viewHolder.time.setText(mTimeFormatter.format(model.getTimestamp()));
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View feedItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);

            final ReportViewHolder holder = new ReportViewHolder(feedItemView);

            feedItemView.setOnClickListener(view -> mOnItemClickListener.apply(holder));

            return holder;
        }
    }
}

