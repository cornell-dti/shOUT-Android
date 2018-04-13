package org.cornelldti.shout.goout;

import android.content.Context;
import android.support.annotation.NonNull;
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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.cornelldti.shout.R;
import org.cornelldti.shout.ShoutFirestore;
import org.cornelldti.shout.speakout.Report;
import org.cornelldti.shout.util.function.BiConsumer;
import org.cornelldti.shout.util.function.Consumer;

import java.util.ArrayList;
import java.util.List;

/**
 * An optimized and reloaded version of the original SpeakOutAdapter
 */

public class GoOutRecentReportsAdapter extends RecyclerView.Adapter<GoOutRecentReportsAdapter.ReportViewHolder> {

    // TODO cleanup internal classes, etc.

    private Consumer<ReportViewHolder> mClickListener;
    private Reports reports = new Reports();

    private class Reports {

        private final List<Report> list = new ArrayList<>();
        private final SparseArray<String> posToId = new SparseArray<>();

        private int intendedSize = 0;

        private CollectionReference ref;

        private Reports() {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            ref = firestore.collection(ShoutFirestore.REPORTS_COLLECTION);
        }

        private void addByKey(String key) {
            intendedSize++;

            ref.document(key).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    Report report = snapshot.toObject(Report.class);
                    list.add(report);
                    int index = list.size() - 1;
                    posToId.put(index, key);
                    notifyItemInserted(index);
                }
            });
        }

    }

    private GoOutRecentReportsAdapter(BiConsumer<GoOutRecentReportsAdapter, ReportViewHolder> clickListener) {
        mClickListener = (viewHolder) -> clickListener.apply(this, viewHolder);
    }


    /* Wrapper methods around the current adapter (stories or all) */


    /**
     * Constructs a GoOutRecentReportsAdapter
     *
     * @param fragment - The fragment to bind the firebase data listening lifecycle to.
     * @param context  - The context to determine date/time formatting within.
     * @return - A new GoOutRecentReportsAdapter
     */
    static GoOutRecentReportsAdapter construct(GoOutFragment fragment, BiConsumer<GoOutRecentReportsAdapter, ReportViewHolder> clickListener, LatLng location, Context context, double radius) {
        GeoFire geofire = fragment.geofire();

        GoOutRecentReportsAdapter adapter = new GoOutRecentReportsAdapter(clickListener);
        adapter.mDateFormatter = DateFormat.getDateFormat(context); // todo
        adapter.mTimeFormatter = DateFormat.getTimeFormat(context);


        GeoQuery query = geofire.queryAtLocation(new GeoLocation(location.latitude, location.longitude), radius); // todo radius?
        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                adapter.reports.addByKey(key);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                adapter.notifyDataSetChanged();
                query.removeAllListeners();

                if (adapter.mItemCountCallback != null) {
                    adapter.mItemCountCallback.apply(adapter.reports.intendedSize);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

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

    private java.text.DateFormat mDateFormatter;
    private java.text.DateFormat mTimeFormatter;
    private Consumer<Integer> mItemCountCallback;

    public GoOutRecentReportsAdapter withItemCountCallback(Consumer<Integer> itemCountCallback) {
        mItemCountCallback = itemCountCallback;
        return this;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);

        final ReportViewHolder holder = new ReportViewHolder(v);

        v.setOnClickListener(view -> mClickListener.apply(holder));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report model = reports.list.get(position);

        holder.report = model; // TODO double check this addition

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

        holder.date.setText(mDateFormatter.format(model.getTimestamp()));
        holder.time.setText(mTimeFormatter.format(model.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return reports.list.size();
    }

    public String getId(int position) {
        return reports.posToId.get(position);
    }

}

